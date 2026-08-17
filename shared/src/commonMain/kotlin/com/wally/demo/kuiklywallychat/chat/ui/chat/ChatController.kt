package com.wally.demo.kuiklywallychat.chat.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.ExperimentalFoundationApi
import com.tencent.kuikly.compose.foundation.lazy.LazyListState
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.chat.base.BaseController
import com.wally.demo.kuiklywallychat.chat.base.PageNavigator
import com.wally.demo.kuiklywallychat.chat.base.PageNavigatorData
import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.base.model.ImageMessage
import com.wally.demo.kuiklywallychat.chat.base.model.LoadMessageResult
import com.wally.demo.kuiklywallychat.chat.base.model.Message
import com.wally.demo.kuiklywallychat.chat.base.model.MessageState
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.SystemMessage
import com.wally.demo.kuiklywallychat.chat.base.model.TextMessage
import com.wally.demo.kuiklywallychat.chat.base.model.TimeMessage
import com.wally.demo.kuiklywallychat.chat.im.ChatGateway
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_GroupId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_Preview_Img_ImgList
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_Preview_Img_InitPos
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_ShowStartChat
import com.wally.demo.kuiklywallychat.chat.im.ImResult
import com.wally.demo.kuiklywallychat.chat.im.ImagePickResult
import com.wally.demo.kuiklywallychat.chat.im.ImagePickSource
import com.wally.demo.kuiklywallychat.chat.im.MediaPickerGateway
import com.wally.demo.kuiklywallychat.ext.Toast
import com.wally.demo.kuiklywallychat.ext.logNative
import kotlinx.coroutines.flow.MutableSharedFlow

class ChatController(
    navigator: PageNavigator,
    private val gateway: ChatGateway,
    private val mediaPickerGateway: MediaPickerGateway,
    val chat: Chat,
    initialTitle: String = "",
) : BaseController(navigator) {
    //区分刚进来拉取最近消息和拉取历史消息
    private enum class MessageLoadType {
        Initial,
        History,
    }


    /**
     * 两条消息间隔超过一分钟时，插入时间消息。
     */
    private val messageMinInterval =
        60 * 1000L

    /**
     * 内部消息顺序：
     *
     * index = 0：最新消息
     * lastIndex：最旧消息
     *
     * ChatScreen 中会通过 asReversed() 转换成显示顺序。
     */
    private val allMessage =
        mutableListOf<Message>()

    /**
     * 图片预览列表。
     */
    private val imageUrlList = mutableListOf<String>()

    /**
     * Controller 向 ChatScreen 发送滚动命令。
     *
     * extraBufferCapacity = 1，允许使用 tryEmit()，
     * 不需要在 Controller 中额外创建 CoroutineScope。
     */
    private val scrollToLatestMessageFlow =
        MutableSharedFlow<Long>(
            extraBufferCapacity = 1,
        )

    @OptIn(ExperimentalFoundationApi::class)
    private val listState = LazyListState()


    private var scrollEventId = 0L

    var chatPageViewState by mutableStateOf(
        ChatPageViewState(
            chat = chat,
            listState = listState,
            scrollToLatestMessageFlow =
                scrollToLatestMessageFlow,
            initialScrollToLatestRequestId = 0L,
            topBarTitle = initialTitle,
            messageList = emptyList(),
            onClickAvatar = ::onClickAvatar,
            onClickMessage = ::onClickMessage,
            onMoreClick = ::onMoreClick
        ),
    )
        private set

    var bottomBarViewState by mutableStateOf(
        ChatPageBottomBarViewState(
            inputSelector = InputSelectorType.None,
            onInputSelectChanged =
                ::onInputSelectChanged,
            onSendTextMessage =
                ::onSendTextMessage,
            onTakePhoto = ::onTakePhoto,
            onAlbumClick = ::onAlbumClick
        ),
    )
        private set

    var loadMessageViewState by mutableStateOf(
        LoadMessageViewState(
            isInitialLoading = false,
            isRefreshing = false,
            isRefreshFinished = false,
            onExecuteRefresh = ::onLoadMoreMessage,
        ),
    )
        private set


    fun onTakePhoto() {
        pickImage(ImagePickSource.Camera)
    }

    fun onAlbumClick() {
        pickImage(ImagePickSource.Album)
    }


    override fun start() {
        super.start()
        "chatController statr".logNative()
        loadInitialMessages()

        /*
      * 页面关闭时，将当前会话的未读数清零。
      */
        markMessageAsRead()
    }


    /**
     * 首次进入页面时加载最近消息。
     *
     * lastMessage 必须传 null，表示从最近一页开始加载。
     * 这个过程不能设置下拉刷新的 isRefreshing。
     */
    private fun loadInitialMessages() {
        loadMessages(
            loadType = MessageLoadType.Initial,
        )
    }

    /**
     * 用户下拉时加载更早的历史消息。
     */
    fun onLoadMoreMessage() {
        loadMessages(
            loadType = MessageLoadType.History,
        )
    }


    /**
     * ImChatPage 接收到 MessageReceived 事件、完成 JSON 解码后，
     * 调用这个方法。
     *
     * 对应原 ChatViewModel 中的：
     *
     * viewModelScope.launch {
     *     attachNewMessage(message)
     *     tryScrollToLatestMessage()
     *     markMessageAsRead()
     * }
     */
    fun onReceiveMessage(
        message: Message,
    ) {
        attachNewMessage(message)
        tryScrollToLatestMessage()
        markMessageAsRead()
    }

    private fun loadMessages(
        loadType: MessageLoadType,
    ) {
        if (
            loadMessageViewState.isInitialLoading ||
            loadMessageViewState.isRefreshing
        ) {
            return
        }

        /*
         * 首次加载必须传 null，表示加载最近一页。
         *
         * 加载历史时，才使用当前列表里最旧的普通消息作为游标。
         * TimeMessage 只是 UI 分隔项，不能作为分页游标。
         */
        val lastMessage =
            when (loadType) {
                MessageLoadType.Initial -> {
                    null
                }

                MessageLoadType.History -> {
                    allMessage.lastOrNull {
                        it !is TimeMessage
                    }
                }
            }

        loadMessageViewState =
            when (loadType) {
                MessageLoadType.Initial -> {
                    loadMessageViewState.copy(
                        isInitialLoading = true,
                        isRefreshing = false,
                    )
                }

                MessageLoadType.History -> {
                    loadMessageViewState.copy(
                        isInitialLoading = false,
                        isRefreshing = true,
                    )
                }
            }

        gateway.loadHistory(
            chat = chat,
            lastMessage = lastMessage,
        ) { result ->
            when (result) {
                is ImResult.Success -> {
                    var hasMoreInfo = when (result) {
                        is LoadMessageResult.Success -> result.isLoadFinished
                        is LoadMessageResult.Failed -> false
                        else -> false
                    }
                    finishMessageLoading(hasMoreInfo)
                    handleLoadHistoryResult(
                        result = result.data,
                        loadType = loadType,
                    )
                }

                is ImResult.Failure -> {
                    finishMessageLoading(true)
                    Utils.logToNative(
                        when (loadType) {
                            MessageLoadType.Initial -> {
                                "加载最近消息失败：${result.message}"
                            }

                            MessageLoadType.History -> {
                                "加载历史消息失败：${result.message}"
                            }
                        },
                    )
                }
            }
        }
    }

    private fun handleLoadHistoryResult(
        result: LoadMessageResult,
        loadType: MessageLoadType,
    ) {
        when (result) {
            is LoadMessageResult.Success -> {
                addMessageToFooter(
                    result.messageList,
                )
                /*
             * 只有首次加载最近消息完成后才滚动到最新消息。
             *
             * 用户下拉加载历史消息时不能执行这个操作，
             * 否则加载完成后会突然跳回列表底部。
             */
                if (
                    loadType == MessageLoadType.Initial &&
                    result.messageList.isNotEmpty()
                ) {
                    requestInitialScrollToLatestMessage()
                }
            }

            is LoadMessageResult.Failed -> {
                /*
             * 失败时只结束当前加载状态，不修改 isRefreshFinished。
             *
             * 因为请求失败并不代表已经没有更多历史消息，
             * 用户之后仍然应该能够重试。
             */
                Utils.logToNative(
                    "加载历史消息失败：" +
                            result.reason,
                )
            }
        }
    }

    //结束加载
    private fun finishMessageLoading(hasMoreInfo: Boolean) {
        loadMessageViewState =
            loadMessageViewState.copy(
                isInitialLoading = false,
                isRefreshing = false,
                isRefreshFinished = hasMoreInfo
            )
    }


    /**
     * 追加历史消息。
     *
     * allMessage 的尾部是最旧消息，因此历史消息追加到尾部。
     *
     *这里不执行消息去重。
     */
    private fun addMessageToFooter(
        newMessageList: List<Message>,
    ) {
        if (newMessageList.isEmpty()) {
            return
        }

        /*
         * 如果列表里已经有消息，检查原有最旧消息与新一页第一条消息
         * 之间是否需要插入时间分隔。
         */
        if (allMessage.isNotEmpty()) {
            val currentOldestMessage =
                allMessage.lastOrNull {
                    it !is TimeMessage
                }

            /*
             *加载下一页前，移除上一页末尾临时生成的 TimeMessage；
            根据新旧两页的真实消息间隔，重新决定是否添加时间项；
             */
            val pageEndTimeMessage =
                allMessage.lastOrNull() as? TimeMessage

            if (
                currentOldestMessage != null &&
                pageEndTimeMessage
                    ?.targetMessage
                    ?.detail
                    ?.msgId == currentOldestMessage.detail.msgId
            ) {
                allMessage.removeAt(allMessage.lastIndex)
            }

            val firstHistoryMessage =
                newMessageList.firstOrNull()

            if (
                currentOldestMessage != null &&
                firstHistoryMessage != null &&
                currentOldestMessage
                    .detail
                    .milliseconds -
                firstHistoryMessage
                    .detail
                    .milliseconds >
                messageMinInterval
            ) {
                allMessage.add(
                    TimeMessage(
                        targetMessage =
                            currentOldestMessage,
                    ),
                )
            }
        }

        var messagesSinceLastTimeItem = 1

        newMessageList.forEachIndexed {
                index,
                currentMessage,
            ->

            allMessage.add(currentMessage)

            val nextOlderMessage =
                newMessageList.getOrNull(
                    index + 1,
                )

            val shouldInsertTimeMessage =
                nextOlderMessage == null ||
                        currentMessage
                            .detail
                            .milliseconds -
                        nextOlderMessage
                            .detail
                            .milliseconds >
                        messageMinInterval ||
                        messagesSinceLastTimeItem >= 10

            if (shouldInsertTimeMessage) {
                allMessage.add(
                    TimeMessage(
                        targetMessage =
                            currentMessage,
                    ),
                )

                messagesSinceLastTimeItem = 1
            } else {
                messagesSinceLastTimeItem += 1
            }
        }

        rebuildImageUrlList()
        publishMessageList()
    }

    /**
     * 追加实时新消息。
     *
     * 最新消息放到 index = 0。
     *
     * 按你的要求，不执行消息去重。
     */
    private fun attachNewMessage(
        newMessage: Message,
    ) {
        val latestMessage =
            allMessage.firstOrNull {
                it !is TimeMessage
            }

        // 十次聊天没出现时间，就追加一次时间
        val hasNoTimeMessageInRecentItems = allMessage
            .take(10)
            .none {
                it is TimeMessage
            }

        val shouldInsertTimeMessage =
            latestMessage == null || newMessage.detail.milliseconds - latestMessage.detail.milliseconds > messageMinInterval || hasNoTimeMessageInRecentItems

        if (shouldInsertTimeMessage) {
            allMessage.add(
                index = 0,
                element = TimeMessage(
                    targetMessage = newMessage,
                ),
            )
        }

        allMessage.add(
            index = 0,
            element = newMessage,
        )

        if (newMessage is ImageMessage) {
            /*
             * imageUrlList 使用从旧到新的显示顺序。
             */
            imageUrlList.add(
                newMessage.previewImageUrl,
            )
        }

        publishMessageList()
    }

    /**
     * 收到新消息时，只有用户原本正在查看列表底部，
     * 才自动滚动到最新消息。
     *
     * 当前 ChatScreen：
     * - messageList 会先 asReversed()
     * - LazyColumn 没有使用 reverseLayout
     *
     * 因此 canScrollForward == false 表示已经位于列表底部。
     */
    private fun tryScrollToLatestMessage() {
        if (!listState.canScrollForward) {
            forceScrollToLatestMessage()
        }
    }

    private fun requestInitialScrollToLatestMessage() {
        chatPageViewState = chatPageViewState.copy(initialScrollToLatestRequestId = chatPageViewState.initialScrollToLatestRequestId + 1L)
    }

    private fun forceScrollToLatestMessage() {
        scrollEventId += 1

        scrollToLatestMessageFlow.tryEmit(
            scrollEventId,
        )
    }

    /**
     * 清除当前会话未读数。
     *
     * 需要 ChatGateway.cleanUnread() 已经接通到 Android：
     *
     * ChatGateway
     * → ImModule
     * → KRImModule
     * → MessageLogic
     * → MessageRespository.cleanUnreadMessageCount()
     */
    private fun markMessageAsRead() {
        gateway.cleanUnread(
            chat = chat,
        ) { result ->
            if (result is ImResult.Failure) {
                Utils.logToNative(
                    "清除会话未读数失败：" +
                            result.message,
                )
            }
            if (result is ImResult.Success) {
                "清除会话未读数成功".logNative()
            }
        }
    }

    private fun publishMessageList() {
        /*
         * 必须创建新的 List。
         * 不能直接将 allMessage 暴露给 Compose State。
         */
        chatPageViewState =
            chatPageViewState.copy(
                messageList =
                    allMessage.toList(),
            )
    }

    private fun rebuildImageUrlList() {
        imageUrlList.clear()

        /*
         * allMessage 是从新到旧，
         * 图片预览列表按从旧到新保存。
         */
        for (index in allMessage.indices.reversed()) {
            val message =
                allMessage[index]

            if (message is ImageMessage) {
                imageUrlList.add(
                    message.previewImageUrl,
                )
            }
        }
    }

    private fun onInputSelectChanged(
        inputSelector: InputSelectorType,
    ) {
        if (
            bottomBarViewState.inputSelector ==
            inputSelector
        ) {
            return
        }

        bottomBarViewState =
            bottomBarViewState.copy(
                inputSelector = inputSelector,
            )
    }

    private fun pickImage(
        source: ImagePickSource,
    ) {
        onInputSelectChanged(
            InputSelectorType.None,
        )

        mediaPickerGateway.pickImage(
            source = source,
        ) { result ->
            when (result) {
                is ImagePickResult.Success -> {
                    sendImageMessage(
                        result.localPath,
                    )
                }

                ImagePickResult.Cancelled -> {
                    // 用户取消选择，不需要提示错误
                }

                is ImagePickResult.Failure -> {
                    Utils.toast(result.message)
                }
            }
        }
    }

    private fun sendImageMessage(
        imagePath: String,
    ) {
        if (imagePath.isBlank()) {
            return
        }

        gateway.sendImage(
            chat = chat,
            imagePath = imagePath,
        ) { result ->
            when (result) {
                is ImResult.Success -> {
                    handleSendMessages(result.data)
                }

                is ImResult.Failure -> {
                    result.message.ifBlank { "发送图片失败" }.Toast()
                }
            }
        }
    }

    private fun onSendTextMessage(
        text: String,
    ) {


        val content = text.trim()

        if (content.isEmpty()) {
            return
        }

        gateway.sendText(
            chat = chat,
            text = content,
        ) { result ->

            when (result) {
                is ImResult.Success -> {
                    handleSendMessages(
                        result.data,
                    )
                }

                is ImResult.Failure -> {
                    Utils.toast(
                        result.message.ifBlank {
                            "发送消息失败"
                        },
                    )
                }
            }
        }
    }

    /**
     * Android sendText 当前会返回：
     *
     * 1. Sending 状态的本地消息；
     * 2. Success 或 Failed 状态的最终消息。
     */
    private fun handleSendMessages(
        messages: List<Message>,
    ) {
        var sendingMessageId: String? = null

        messages.forEach { message ->
            when (
                val messageState =
                    message.detail.state
            ) {
                MessageState.Sending -> {
                    sendingMessageId =
                        message.detail.msgId

                    attachNewMessage(message)
                    forceScrollToLatestMessage()
                    markMessageAsRead()
                }

                MessageState.Success -> {
                    val targetMessageId =
                        sendingMessageId ?: return@forEach

                    resetMessageState(
                        msgId = targetMessageId,
                        messageState =
                            messageState,
                    )
                }

                is MessageState.Failed -> {
                    val targetMessageId =
                        sendingMessageId ?: return@forEach

                    resetMessageState(
                        msgId = targetMessageId,
                        messageState =
                            messageState,
                    )

                    if (
                        messageState
                            .reason
                            .isNotBlank()
                    ) {
                        Utils.toast(
                            messageState.reason,
                        )
                    }
                }
            }
        }
    }

    private fun resetMessageState(
        msgId: String,
        messageState: MessageState,
    ) {
        val messageIndex =
            allMessage.indexOfFirst {
                it.detail.msgId == msgId
            }

        if (messageIndex < 0) {
            return
        }

        val targetMessage =
            allMessage[messageIndex]

        val newMessage =
            when (targetMessage) {
                is TextMessage -> {
                    targetMessage.copy(
                        messageDetail =
                            targetMessage
                                .detail
                                .copy(
                                    state =
                                        messageState,
                                ),
                    )
                }

                is ImageMessage -> {
                    targetMessage.copy(
                        messageDetail =
                            targetMessage
                                .detail
                                .copy(
                                    state =
                                        messageState,
                                ),
                    )
                }

                is SystemMessage,
                is TimeMessage,
                    -> {
                    return
                }
            }

        allMessage[messageIndex] =
            newMessage

        publishMessageList()
    }

    private fun onClickAvatar(
        message: Message,
    ) {
        /*
         * commonMain 不能直接打开 Android Activity。
         * 后续应通过 RouterModule 或 BridgeModule 跳转。
         */
        Utils.logToNative(
            "click message avatar: " +
                    message.detail.sender.id,
        )
    }

    private fun onClickMessage(
        message: Message,
    ) {
        if (message !is ImageMessage) {
            return
        }

        val previewIndex = imageUrlList.indexOf(message.previewImageUrl).coerceAtLeast(0)
//imageUrlList

        val imageArray = JSONArray().apply {
            imageUrlList.forEach { url ->
                put(url)
            }
        }


        var data= PageNavigatorData(
            pageName = "PreviewImgPage",
            pageData = JSONObject().apply {
                put(PARAM_Preview_Img_InitPos, previewIndex)
                put(PARAM_Preview_Img_ImgList,imageArray)
            }
        )
        goToPage(data)

    }

    fun onMoreClick() {
        if (chat is  Chat.C2C){
            var data= PageNavigatorData(
                pageName = "FriendProfilePage",
                pageData = JSONObject().apply {
                    put(PARAM_FriendId, chat.id)
                    put(PARAM_ShowStartChat,false)
                }
            )
            goToPage(data)
        }else{
            var data= PageNavigatorData(
                pageName = "GroupProfilePage",
                pageData = JSONObject().apply {
                    put(PARAM_GroupId, chat.id)
                }
            )
            goToPage(data)
        }

    }
}