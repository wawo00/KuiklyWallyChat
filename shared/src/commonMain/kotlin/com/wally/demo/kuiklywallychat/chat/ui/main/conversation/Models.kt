package com.wally.demo.timsdk.ui.main.conversation.logic

import com.tencent.kuikly.compose.foundation.ExperimentalFoundationApi
import com.tencent.kuikly.compose.foundation.lazy.LazyListState
import com.tencent.kuikly.compose.ui.unit.IntOffset
import com.tencent.kuikly.compose.ui.unit.IntSize
import com.wally.demo.kuiklywallychat.chat.base.model.ServerConnectState
import com.wally.demo.kuiklywallychat.chat.base.model.WallyConversation


data class ConversationPageViewState @OptIn(ExperimentalFoundationApi::class) constructor(
    var listState: LazyListState= LazyListState(),
    val serverConnectState: ServerConnectState=ServerConnectState.Idle,
    val conversationList: List<WallyConversation> = emptyList(),
    val scrollTrigger: Int = 0,
    val isLoading:Boolean = false,
    val errorMessage: String? = null,
    val onDeleteConversation:(conversation:WallyConversation)-> Unit,
    val onPinConversation:(conversation: WallyConversation, bool: Boolean)-> Unit,
    val unReadTotalNum: Long=0
)

data class ConversationMenuState(
    val conversation: WallyConversation,
    val itemPosition: IntOffset,
    val itemSize: IntSize,
)