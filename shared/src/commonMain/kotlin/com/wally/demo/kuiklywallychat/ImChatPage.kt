package com.wally.demo.kuiklywallychat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.base.BasePager
import com.wally.demo.kuiklywallychat.chat.im.MediaPickerModule
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.im.ImEvent
import com.wally.demo.kuiklywallychat.chat.im.ImJsonCodec
import com.wally.demo.kuiklywallychat.chat.im.ImModule
import com.wally.demo.kuiklywallychat.chat.ui.chat.ChatController
import com.wally.demo.kuiklywallychat.chat.ui.chat.ChatScreen

@Page("ChatPage", supportInLocal = true)
class ImChatPage : BasePager() {

    /**
     * Controller 必须是 Compose State。
     *
     * willInit() 执行 setContent 时 Controller 还没有创建，
     * created() 中创建后，mutableStateOf 会触发页面重组。
     */
    private var chatController:
            ChatController? by mutableStateOf(null)

    private var pageError:
            String? by mutableStateOf(null)

    override fun willInit() {
        super.willInit()

        setContent {
            val controller = chatController
            val error = pageError

            when {
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = error)
                    }
                }

                controller == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    ChatScreen(
                        chatPageViewState =
                            controller.chatPageViewState,
                        chatPageBottomBarViewState =
                            controller.bottomBarViewState,
                        loadMessageViewState =
                            controller.loadMessageViewState,
                        onBackClick = ::closeCurrentPage
                    )
                }
            }
        }
    }

    override fun created() {
        super.created()

        /*
         * 获取 MainPage 通过 openPage() 传递过来的参数。
         */
        val params = pageData.params

        val conversationId =
            params.optString("conversationId")
                .trim()

        val chatType =
            params.optString("chatType")
                .trim()

        val conversationName =
            params.optString("conversationName")
                .trim()

        Utils.logToNative(
            "打开聊天页：" +
                    "conversationId=$conversationId, " +
                    "chatType=$chatType, " +
                    "conversationName=$conversationName",
        )

        if (conversationId.isEmpty()) {
            pageError = "conversationId 不能为空"
            return
        }

        /*
         * 根据路由参数构建 Chat。
         *
         * chatType 的值来自：
         * conversation.type.toBridgeValue()
         */
        val chat =
            when (chatType) {
                "c2c" -> {
                    Chat.C2C(
                        id = conversationId,
                    )
                }

                "group" -> {
                    Chat.Group(
                        id = conversationId,
                    )
                }

                else -> {
                    pageError =
                        "不支持的会话类型：$chatType"
                    return
                }
            }

        /*
         * created() 中可以安全获取 Module。
         */
        val gateway =
            acquireModule<ImModule>(
                ImModule.MODULE_NAME,
            )

        val mediaPickerGateway= acquireModule<MediaPickerModule>(
            MediaPickerModule.MODULE_NAME,
        )

        /*
         * 将 Chat 和会话名称传递给 Controller。
         *
         * Controller 创建后，chatController 状态发生变化，
         * willInit() 中的 setContent 会自动重组，
         * 然后调用 ChatScreen。
         */
        chatController =
            ChatController(
                navigator = this,
                gateway = gateway,
                mediaPickerGateway = mediaPickerGateway,
                chat = chat,
                initialTitle = conversationName
            ).also { controller ->
                controller.start()
            }
    }

    override fun onReceivePagerEvent(
        pagerEvent: String,
        eventData: JSONObject,
    ) {
        super.onReceivePagerEvent(
            pagerEvent,
            eventData,
        )

        if (pagerEvent != ImEvent.MessageReceived) {
            return
        }

        val messageJson =
            eventData.optJSONObject("message")
                ?: return

        val message =
            runCatching {
                ImJsonCodec.decodeMessage(
                    messageJson,
                )
            }.getOrElse { throwable ->
                Utils.logToNative(
                    "解析新消息失败：" +
                            throwable.message,
                )
                return
            }

        chatController?.onReceiveMessage(message)
    }

    override fun pageWillDestroy() {
        chatController?.stop()
        chatController = null
        super.pageWillDestroy()
    }
}