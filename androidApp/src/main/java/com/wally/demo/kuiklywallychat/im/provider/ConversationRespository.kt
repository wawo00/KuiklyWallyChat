package com.wally.demo.timsdk.provider

import com.tencent.imsdk.conversation.Conversation
import com.tencent.imsdk.v2.V2TIMCallback
import com.tencent.imsdk.v2.V2TIMConversation
import com.tencent.imsdk.v2.V2TIMConversationListener
import com.tencent.imsdk.v2.V2TIMConversationResult
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.wally.demo.kuiklywallychat.chat.base.model.ActionResult
import com.wally.demo.kuiklywallychat.chat.base.model.ConversationType
import com.wally.demo.kuiklywallychat.chat.base.model.WallyConversation
import com.wally.demo.kuiklywallychat.chat.im.ImEvent
import com.wally.demo.kuiklywallychat.im.ImEventBus
import com.wally.demo.kuiklywallychat.im.ImRuntime.AppCoroutineScope
import com.wally.demo.kuiklywallychat.im.account.NativeResult
import com.wally.demo.timsdk.base.proxy.Converters
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @author Wally(25054984)
 * @since 2026/7/7
 * @email wanlei@haier.com
 * @desciption 对话内容提供者
 *  via:https://trtc.io/zh/document/48323?product=chat&menulabel=core%20sdk&platform=android
 */
class ConversationRespository(var eventBus: ImEventBus) {

    // list数据
    var mConversationListFlow = MutableSharedFlow<List<WallyConversation>>()
    var mConversationList = mutableListOf<WallyConversation>()

    // 未读数据
    val totalUnReadMsgCountFlow = MutableStateFlow<Long>(0)


    private val conversationComparator = Comparator<WallyConversation> { o1, o2 ->
        val o1Timestamp = o1.lastMessage.detail.milliseconds
        val o2Timestamp = o2.lastMessage.detail.milliseconds
        when {
            o1.isPinned && o2.isPinned -> o2Timestamp.compareTo(o1Timestamp)
            o1.isPinned -> -1
            o2.isPinned -> 1
            else -> o2Timestamp.compareTo(o1Timestamp)
        }
    }


    var refreshJob: Job? = null

    init {
        V2TIMManager.getConversationManager().addConversationListener(object : V2TIMConversationListener() {
            override fun onTotalUnreadMessageCountChanged(totalUnreadCount: Long) {
                notifyUnreadCountChanged(totalUnreadCount)
            }

            override fun onConversationChanged(conversationList: List<V2TIMConversation?>?) {
                notifyConversationChanged(reason = "onConversationChanged")
            }

            override fun onConversationDeleted(conversationIDList: List<String?>?) {
                notifyConversationChanged(
                    reason = "onConversationDeleted",
                )
            }

            override fun onNewConversation(conversationList: List<V2TIMConversation?>?) {
                notifyConversationChanged(
                    reason = "onNewConversation",
                )
            }
        })


    }

    private fun notifyConversationChanged(reason: String) {
        eventBus.emit(
            name = ImEvent.ConversationsChanged,
            data = mapOf(
                "reason" to reason,
            )
        )
    }
    private fun notifyUnreadCountChanged(totalUnreadCount:Long) {
        eventBus.emit(
            name = ImEvent.UnreadCountChanged,
            data = mapOf(
                "reason" to ImEvent.UnreadCountChanged,
                "totalUnreadCount" to totalUnreadCount
            )
        )
    }

    // curd操作+pin曹组
    // 比较器


    fun refreshTotalUnreadMessageCount() {
        V2TIMManager.getConversationManager()
            .getTotalUnreadMessageCount(object : V2TIMValueCallback<Long> {
                override fun onSuccess(totalUnreadCount: Long) {
                    AppCoroutineScope.launch {
                        totalUnReadMsgCountFlow.emit(value = totalUnreadCount)
                    }
                }

                override fun onError(code: Int, desc: String?) {
                    AppCoroutineScope.launch {
                        totalUnReadMsgCountFlow.emit(value = 0)
                    }
                }
            })
    }

    suspend fun pinConversation(conversationId: String, pin: Boolean): ActionResult {
        // 从mConversationList中遍历找到conversationId对应的实例作为参数传入
        var conversation: WallyConversation? = mConversationList.firstOrNull { it.id == conversationId }
        if (conversation == null) {
            return ActionResult.Fail(code = -1, msg = "Conversation not found")
        }
        return suspendCancellableCoroutine { continuation ->
            V2TIMManager.getConversationManager().pinConversation(
                Converters.getConversationKey(conversation = conversation),
                pin,
                object : V2TIMCallback {
                    override fun onSuccess() {
                        continuation.resume(value = ActionResult.Success)
                    }

                    override fun onError(code: Int, desc: String?) {
                        continuation.resume(
                            value = ActionResult.Fail(
                                code = code,
                                msg = desc
                            )
                        )
                    }
                }
            )
        }
    }

    suspend fun deleteC2CConversation(id: String): ActionResult {
        return Converters.deleteC2CConversation(id)
    }

    suspend fun deleteGroupConversation(groupId: String): ActionResult {
        return Converters.deleteGroupConversation(groupId = groupId)
    }

    private suspend fun dispatchConversationList(conversationList: List<WallyConversation>) {
        this@ConversationRespository.mConversationListFlow.emit(value = conversationList)
    }


    suspend fun loadConversation(): NativeResult<List<WallyConversation>> {
        return getConversationListOrigin()
    }

//    fun refreshList(reason: String) {
//        Log.i(TAG, "refreshList cause : ${reason} ")
//        refreshJob?.cancel()
//        refreshJob = AppCoroutineScope.launch {
//            val conversationList = getConversationListOrigin()
//            dispatchConversationList(conversationList = conversationList)
//        }
//    }

    private suspend fun getConversationListOrigin(): NativeResult<List<WallyConversation>> {
        var nextStep = 0L
        val conversationList = mutableListOf<WallyConversation>()
        while (true) {
            val pair = getConversationList(nextStep = nextStep)
            conversationList.addAll(elements = pair.first)
            nextStep = pair.second
            if (nextStep <= 0) {
                break
            }
        }
        mConversationList = conversationList
        return NativeResult.Success(conversationList)
    }

    private suspend fun getConversationList(nextStep: Long): Pair<List<WallyConversation>, Long> {
        return suspendCancellableCoroutine { continuation ->
            V2TIMManager.getConversationManager().getConversationList(
                nextStep,
                100,
                object : V2TIMValueCallback<V2TIMConversationResult> {
                    override fun onSuccess(result: V2TIMConversationResult) {
                        val convertersList = result.conversationList.filter { conversation ->
                            !conversation.userID.isNullOrBlank() || !conversation.groupID.isNullOrBlank()
                        }
                        continuation.resume(
                            value = Pair(
                                first = convertConversation(convertersList = convertersList),
                                second = if (result.isFinished) {
                                    0
                                } else {
                                    result.nextSeq
                                }
                            )
                        )
                    }

                    override fun onError(code: Int, desc: String?) {
                        continuation.resume(value = Pair(first = emptyList(), second = 0))
                    }
                }
            )
        }
    }

    private fun convertConversation(convertersList: List<V2TIMConversation>?): List<WallyConversation> {
        return convertersList?.mapNotNull { conversation ->
            convertConversation(conversation = conversation)
        }?.sortedWith(conversationComparator) ?: emptyList()
    }

    private fun convertConversation(conversation: V2TIMConversation): WallyConversation? {
        val lastConversationMessage = conversation.lastMessage ?: return null
        val name = conversation.showName?.trim() ?: ""
        val avatarUrl = conversation.faceUrl ?: ""
        val unreadMessageCount = conversation.unreadCount.toLong()
        val lastMessage = Converters.convertMessage(timMessage = lastConversationMessage)
        val isPinned = conversation.isPinned
        return when (conversation.type) {
            V2TIMConversation.V2TIM_C2C -> {
                WallyConversation(
                    id = conversation.userID ?: "",
                    name = name,
                    avatarUrl = avatarUrl,
                    unreadMessageCount = unreadMessageCount,
                    lastMessage = lastMessage,
                    isPinned = isPinned,
                    type = ConversationType.C2C
                )
            }

            V2TIMConversation.V2TIM_GROUP -> {
                WallyConversation(
                    id = conversation.groupID ?: "",
                    name = name,
                    avatarUrl = avatarUrl,
                    unreadMessageCount = unreadMessageCount,
                    lastMessage = lastMessage,
                    isPinned = isPinned,
                    type = ConversationType.Group
                )
            }

            else -> {
                null
            }
        }
    }


}