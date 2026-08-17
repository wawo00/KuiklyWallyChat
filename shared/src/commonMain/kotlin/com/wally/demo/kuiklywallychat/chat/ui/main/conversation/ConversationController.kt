package com.wally.demo.kuiklywallychat.chat.ui.main.conversation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.chat.base.BaseController
import com.wally.demo.kuiklywallychat.chat.base.PageNavigator
import com.wally.demo.kuiklywallychat.chat.base.model.ConversationType
import com.wally.demo.kuiklywallychat.chat.base.model.WallyConversation
import com.wally.demo.kuiklywallychat.chat.im.ConversationGateway
import com.wally.demo.kuiklywallychat.chat.im.ImResult
import com.wally.demo.kuiklywallychat.ext.Toast
import com.wally.demo.timsdk.ui.main.conversation.logic.ConversationPageViewState

class ConversationController(navigator: PageNavigator, private val gateway: ConversationGateway) : BaseController(navigator) {
    var state by mutableStateOf(ConversationPageViewState(onDeleteConversation = ::deleteConversation, onPinConversation = ::pinConversation))
        private set

    override fun start() {
        super.start()
        loadConversations()
    }

    private fun loadConversations() {
        state = state.copy(
            isLoading = true,
            errorMessage = null,
        )

        gateway.loadConversations { result ->
            when (result) {
                is ImResult.Success -> {
                    Utils.logToNative("conversion请求成.size is ${result.data.size}")
                    state = state.copy(
                        conversationList = result.data,
                        isLoading = false
                    )
                }

                is ImResult.Failure -> {
                    state = state.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }

    }

    fun onConversationChanged(reason: String?) {
        Utils.logToNative("loadConversations casuse : onConversationChanged")
        loadConversations()

    }

    fun onConversationUnReadNum(totalUnreadCount: Long) {
        Utils.logToNative("loadConversations casuse : onConversationUnReadNum")
        // 刷新未读数
        state = state.copy(unReadTotalNum = totalUnreadCount)
        loadConversations()

    }

    fun deleteConversation(conversation: WallyConversation) {

        if (conversation.type == ConversationType.C2C) {
            gateway.deleteC2CConversation(
                conversationId = conversation.id,
            ) { result ->
                resultExec(result)
            }
        }

        if (conversation.type == ConversationType.Group) {
            gateway.deleteGroupConversation(
                conversationId = conversation.id,
            ) { result ->
                resultExec(result)
            }
        }
    }

    private fun resultExec(result: ImResult<Unit>) {
        if (result is ImResult.Failure) {
            "操作失败：${result.message}".Toast()
        }
        if (result is ImResult.Success) {
            "操作成功".Toast()
        }
    }

    private fun pinConversation(conversation: WallyConversation, pin: Boolean) {
        gateway.pinConversation(conversation.id, pin) {
            resultExec(it)
        }
    }
}