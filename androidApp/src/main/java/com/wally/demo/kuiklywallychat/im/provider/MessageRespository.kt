package com.wally.demo.timsdk.provider

import android.R.id.message
import android.graphics.BitmapFactory
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener
import com.tencent.imsdk.v2.V2TIMCallback
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMMessage
import com.tencent.imsdk.v2.V2TIMSendCallback
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.wally.demo.kuiklywallychat.R
import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.base.model.ImageMessage
import com.wally.demo.kuiklywallychat.chat.base.model.LoadMessageResult
import com.wally.demo.kuiklywallychat.chat.base.model.Message
import com.wally.demo.kuiklywallychat.chat.base.model.MessageDetail
import com.wally.demo.kuiklywallychat.chat.base.model.MessageState
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.SystemMessage
import com.wally.demo.kuiklywallychat.chat.base.model.TextMessage
import com.wally.demo.kuiklywallychat.chat.base.model.TimeMessage
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeMap
import com.wally.demo.kuiklywallychat.chat.im.ImEvent
import com.wally.demo.kuiklywallychat.ext.log
import com.wally.demo.kuiklywallychat.im.ImEventBus
import com.wally.demo.kuiklywallychat.im.ImRuntime.AppCoroutineScope
import com.wally.demo.kuiklywallychat.im.tools.StringResources.getString
import com.wally.demo.timsdk.base.proxy.Converters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.SoftReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

/**
 * @author Wally(25054984)
 * @since 2026/7/9
 * @email wanlei@haier.com
 * @desciption 消息的内容提供者，之前wallychat中的messageProvider
 */
class MessageRespository(var eventBus: ImEventBus) {
    interface WallyMessageListener {
        fun onReceiveMessage(message: Message)
    }

    val msgListenerMap = mutableMapOf<String, SoftReference<WallyMessageListener>>()

    private val nativeMessageCache = mutableMapOf<String, V2TIMMessage>() //主动维护下标，用于分页


    init {
        // 追加回调，用于获得新消息
        V2TIMManager.getMessageManager().addAdvancedMsgListener(object : V2TIMAdvancedMsgListener() {

            //获得新的消息
            override fun onRecvNewMessage(msg: V2TIMMessage) {
                val partId = msg.groupID ?: msg.userID ?: ""
//                var listener = msgListenerMap[partId]
                var message = Converters.convertMessage(timMessage = msg)
//                val actualListener = listener?.get()
//                if (actualListener != null) {
//
//                    actualListener.onReceiveMessage(message)
//                } else {
//                    msgListenerMap.remove(partId)
//                }
                eventBus.run {
                    emit(
                        name = ImEvent.MessageReceived,
                        data = mapOf(
                            "chatId" to partId,
                            "message" to message.toBridgeMap(),
                        ),
                    )
                }
            }
        })
    }

    suspend fun getHistoryMessage(chat: Chat, lastMsg: Message?): LoadMessageResult {
        "MessageRespository call getHistoryMessage".log()
        val chatId = chat.id
        val count = 60
        val lastNativeMessage = lastMsg
            ?.detail
            ?.nativeMessageId
            ?.takeIf { it.isNotBlank() }
            ?.let { nativeMessageCache[it] }

        return suspendCancellableCoroutine { coroutine ->

            val callback = object : V2TIMValueCallback<List<V2TIMMessage>> {
                override fun onSuccess(list: List<V2TIMMessage>) {
                    "MessageRespository call getHistoryMessage success".log()
                    list.forEach { timMessage ->
                        val nativeId = timMessage.msgID.orEmpty()
                        if (nativeId.isNotBlank()) {
                            nativeMessageCache[nativeId] = timMessage
                        }
                    }
                    coroutine.resume(
                        value = LoadMessageResult.Success(
                            messageList = Converters.convertMessage(messageList = list),
                            isLoadFinished = list.size < count
                        )
                    )
                }

                override fun onError(errorCode: Int, errorMsg: String?) {
                    "MessageRespository call getHistoryMessage fail: ${errorMsg}".log()
                    coroutine.resume(value = LoadMessageResult.Failed(reason = errorMsg ?: "Unknown Error"))
                }

            }

            when (chat) {
                is Chat.C2C -> {
                    V2TIMManager.getMessageManager().getC2CHistoryMessageList(
                        chatId,
                        count,
                        lastNativeMessage,
                        callback
                    )

                }

                is Chat.Group -> {
                    V2TIMManager.getMessageManager().getGroupHistoryMessageList(
                        chatId,
                        count,
                        lastNativeMessage,
                        callback
                    )
                }
            }
        }

    }


    //取消所有未读
    suspend fun cleanUnreadMessageCount(chat: Chat) {
        return suspendCancellableCoroutine { continuation ->
            V2TIMManager.getConversationManager().cleanConversationUnreadMessageCount(
                Converters.getConversationKey(chat = chat),
                0,
                0,
                object : V2TIMCallback {
                    override fun onSuccess() {
                        continuation.resume(Unit)
                    }

                    override fun onError(code: Int, desc: String?) {
                        continuation.resumeWithException(RuntimeException("Clean unread failed: $code, $desc"))
                    }
                }
            )

        }

    }


    //=======================用于发送消息=====================


    suspend fun sendText(chat: Chat, text: String): Channel<Message> {
        val localTempMessage =
            TextMessage(
                messageDetail = generatePreSendMessageDetail(),
                text = text
            )
        val createdMessage = V2TIMManager.getMessageManager().createTextMessage(text)
        return sendMessage(
            chat = chat,
            timMessage = createdMessage,
            localTempMessage = localTempMessage
        )
    }

    private suspend fun sendMessage(
        chat: Chat,
        timMessage: V2TIMMessage,
        localTempMessage: Message,
    ): Channel<Message> {
        val messageChannel = Channel<Message>(capacity = 2)
        val c2cId: String
        val groupId: String
        when (chat) {
            is Chat.C2C -> {
                c2cId = chat.id
                groupId = ""
            }

            is Chat.Group -> {
                c2cId = ""
                groupId = chat.id
            }
        }

        messageChannel.send(localTempMessage)
        V2TIMManager.getMessageManager().sendMessage(
            timMessage,
            c2cId,
            groupId,
            V2TIMMessage.V2TIM_PRIORITY_HIGH,
            false,
            null,
            object : V2TIMSendCallback<V2TIMMessage> {
                override fun onSuccess(messsage: V2TIMMessage) {
                    AppCoroutineScope.launch {
                        val convertMessage = Converters.convertMessage(timMessage = messsage)
                        messageChannel.send(element = convertMessage)
                        messageChannel.close()
                    }
                }

                override fun onError(code: Int, desc: String?) {
                    AppCoroutineScope.launch {
                        messageChannel.send(
                            element = localTempMessage.resetToFailed(
                                failReason = getString(
                                    resId = R.string.error_load_message,
                                    code,
                                    desc ?: ""
                                )
                            )
                        )
                        messageChannel.close()
                    }
                }

                override fun onProgress(progress: Int) {

                }
            })
        return messageChannel
    }


    suspend fun sendImage(chat: Chat, imagePath: String): Channel<Message> {
        return withContext(context = Dispatchers.Default) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)

            /*
            * SDK 发送仍使用真实文件路径 imagePath。
            * UI 显示使用带 file:// scheme 的 localImageUrl。
             */
            val localImageUrl =
                if (imagePath.startsWith("file://")) {
                    imagePath
                } else {
                    "file://$imagePath"
                }

            val localTempMessage = ImageMessage(
                messageDetail = generatePreSendMessageDetail(),
                original = ImageMessage.ImageElement(
                    width = options.outWidth,
                    height = options.outHeight,
                    url = localImageUrl
                ),
                large = null,
                thumb = null
            )


            /*
             * 这里必须继续传裸文件路径，不能传 localImageUrl。
             * IM SDK createImageMessage() 需要操作系统文件路径。
             */
            val createdMessage = V2TIMManager.getMessageManager().createImageMessage(imagePath)
            sendMessage(
                chat = chat,
                timMessage = createdMessage,
                localTempMessage = localTempMessage
            )
        }
    }

    private suspend fun generatePreSendMessageDetail(): MessageDetail {
        return MessageDetail(
            msgId = generateMessageId(),
            milliseconds = generateMessageTimestamp(),
            state = MessageState.Sending,
            sender = Converters.getSelfProfile() ?: PersonProfile.Empty,
            isOwnMessage = true
        )
    }

    private fun generateMessageId(): String {
        return (System.currentTimeMillis() + Random.nextInt(from = 1024, until = 2048)).toString()
    }

    private fun generateMessageTimestamp(): Long {
        return V2TIMManager.getInstance().serverTime * 1000L
    }


}

private fun Message.resetToFailed(failReason: String): Message {
    val failedState = MessageState.Failed(reason = failReason)
    return when (this) {
        is TextMessage -> {
            this.copy(messageDetail = this.detail.copy(state = failedState))
        }

        is ImageMessage -> {
            this.copy(messageDetail = this.detail.copy(state = failedState))
        }

        is TimeMessage -> {
            throw IllegalArgumentException()
        }

        is SystemMessage -> {
            throw IllegalArgumentException()
        }
    }
}
