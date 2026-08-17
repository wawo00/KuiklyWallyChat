package com.wally.demo.kuiklywallychat.chat.ui.chat

import com.tencent.kuikly.compose.foundation.lazy.LazyListState
import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.base.model.Message
import kotlinx.coroutines.flow.Flow


data class ChatPageViewState(
    val chat: Chat,
    val listState: LazyListState,
    // 有当用户正盯着“最新”的区域看时，新消息来了才自动帮他顶上去；如果他已经翻到后面去看旧消息了，说明他现在不想被新消息打扰，所以就不自动滚动了。
    /**
     * 实时收到新消息、发送新消息时使用的滚动事件。
     */
    val scrollToLatestMessageFlow: Flow<Long>,

    /**
     * 首次加载最近消息完成后的滚动请求。
     *
     * 使用 State 而不是 SharedFlow，避免 UI 尚未开始 collect 时事件丢失。
     * 0 表示还没有发出过首次滚动请求。
     */
    val initialScrollToLatestRequestId: Long,
    val topBarTitle: String,
    val messageList: List<Message>,
    val onClickAvatar: (message: Message) -> Unit,
    val onClickMessage: (message: Message) -> Unit,
    val onMoreClick: () -> Unit,

)


data class LoadMessageViewState(
    /**
     * 是否正在首次加载最近消息。
     *
     * 这个状态不能传给下拉刷新组件，因为首次加载并不是加载历史消息。
     */
    val isInitialLoading: Boolean,

    /**
     * 是否正在由用户下拉加载更早的历史消息。
     *
     * 只有这个状态可以控制 pullToRefreshItem。
     */
    val isRefreshing: Boolean,

    /**
     * 是否已经没有更多历史消息。
     */
    val isRefreshFinished: Boolean,

    /**
     * 用户触发下拉加载历史消息。
     */
    val onExecuteRefresh: () -> Unit,
)

data class ChatPageBottomBarViewState(
    val inputSelector: InputSelectorType,
    // 偷懒了，viewstate中是不允许带有回调的，这会导致状态跟ui回调强绑定
    val onInputSelectChanged: (InputSelectorType)-> Unit,
    val onSendTextMessage: (text: String) -> Unit,
    val onTakePhoto: () -> Unit,
    val onAlbumClick: () -> Unit,
//    val onSendImageMessage: (imageUri: Uri) -> Unit
)


// 上传的消息内容
//
enum class InputSelectorType {
    None,
    Emoji,
    Picture;
}
