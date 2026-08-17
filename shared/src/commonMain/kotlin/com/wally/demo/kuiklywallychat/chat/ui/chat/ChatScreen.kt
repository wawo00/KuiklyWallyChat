package com.wally.demo.kuiklywallychat.chat.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.Scaffold
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.pullToRefreshItem
import com.tencent.kuikly.compose.material3.rememberPullToRefreshState
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.base.model.ImageMessage
import com.wally.demo.kuiklywallychat.chat.base.model.SystemMessage
import com.wally.demo.kuiklywallychat.chat.base.model.TextMessage
import com.wally.demo.kuiklywallychat.chat.base.model.TimeMessage
import com.wally.demo.timsdk.widgets.ChatMessageItem
import com.wally.demo.timsdk.widgets.ChatPageBottomBar
import com.wally.demo.timsdk.widgets.ChatPageTopBar
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatPageViewState: ChatPageViewState,
    chatPageBottomBarViewState: ChatPageBottomBarViewState,
    loadMessageViewState: LoadMessageViewState,
    onBackClick: () -> Unit,
) {
    var keyboardHeight by remember { mutableStateOf(0f) }

    val displayMessages = remember(
        chatPageViewState.messageList,
    ) {
        chatPageViewState.messageList.asReversed()
    }
    val currentDisplayMessages by rememberUpdatedState(
        displayMessages,
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = keyboardHeight.dp),
        topBar = {
            ChatPageTopBar(chatPageViewState.topBarTitle, backClick = onBackClick,chatPageViewState.onMoreClick)
        },
        bottomBar = {
            ChatPageBottomBar(chatPageBottomBarViewState) {
                keyboardHeight = it
            }
        }

    ) { padding ->
        /*
         * 首次加载最近消息完成后滚动到底部。
         *
         * initialScrollToLatestRequestId 是 State，不会因为 ChatScreen
         * 暂时还没有开始 collect 而丢失。
         */
        LaunchedEffect( chatPageViewState.initialScrollToLatestRequestId) {
            if (
                chatPageViewState
                    .initialScrollToLatestRequestId == 0L
            ) {
                return@LaunchedEffect
            }

            /*
            * 等待本次 messageList 重组并完成一轮列表布局。
            * 如果 Kuikly 当前版本存在 scrollToItem，可以优先使用无动画跳转。
             */
            delay(16L)

            if (currentDisplayMessages.isNotEmpty()) {
                /*
                 * pullToRefreshItem 占据 LazyColumn 的第 0 项。
                 *
                 * 消息数量为 N 时，最后一条消息在 LazyColumn 中的索引是 N，
                 * 而不是 N - 1。
                 */
                val latestItemIndex =
                    currentDisplayMessages.size

                chatPageViewState.listState
                    .scrollToItem(latestItemIndex)
            }

        }

        /*
     * 这个 Flow 继续处理实时收到新消息、发送消息后的滚动。
     */
        LaunchedEffect(chatPageViewState.listState) {
            chatPageViewState.scrollToLatestMessageFlow.collect {
                delay(10L)

                if (currentDisplayMessages.isNotEmpty()) {
                    val latestItemIndex =
                        currentDisplayMessages.size

                    chatPageViewState.listState
                        .animateScrollToItem(
                            latestItemIndex,
                        )
                }
            }
        }

        val pullRefreshState = rememberPullToRefreshState(loadMessageViewState.isRefreshing)
        val listState = chatPageViewState.listState
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
//                .pullToRefresh(
//                    state = pullRefreshState,
//                    enabled = loadMessageViewState.isRefreshFinished,
//                    isRefreshing = loadMessageViewState.isRefreshing,
//                    onRefresh = loadMessageViewState.onExecuteRefresh
//                )
        ) {
            if (loadMessageViewState.isInitialLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "正在加载最近消息...",
                        color = Color.Gray,
                    )
                }
            }else{
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
//                reverseLayout = true,数据结构倒序了，lazycolum就不用了
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    state = chatPageViewState.listState
                ) {
                    // kuikly实现下拉
                    pullToRefreshItem(
                        state = pullRefreshState,
                        scrollState = listState,
                        onRefresh = {
                            if (!loadMessageViewState.isRefreshing) {
                                loadMessageViewState.onExecuteRefresh()
                            }
                        }
                    ) { pullProgress, isRefreshing, _ ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = when {
                                    isRefreshing -> {
                                        "正在加载历史消息..."
                                    }

                                    pullProgress >= 1f -> {
                                        "松开加载历史消息"
                                    }

                                    else -> {
                                        "下拉加载历史消息"
                                    }
                                },
                                color = if (isRefreshing) {
                                    Color.Blue
                                } else {
                                    Color.Gray
                                },
                                fontSize = 14.sp,
                            )
                        }

                    }


                    // 模拟单条对方消息项
                    items(
//                    items = chatPageViewState.messageList,
                        items = displayMessages,//这里不使用原始数据，使用倒序之后的
                        key = { message -> message.detail.msgId },
                        contentType = { message ->
                            when (message) {
                                is TimeMessage -> {
                                    "TimeMessage"
                                }

                                is SystemMessage -> {
                                    "SystemMessage"
                                }

                                is TextMessage -> {
                                    if (message.detail.isOwnMessage) {
                                        "ownTextMessage"
                                    } else {
                                        "friendTextMessage"
                                    }
                                }

                                is ImageMessage -> {
                                    if (message.detail.isOwnMessage) {
                                        "ownImageMessage"
                                    } else {
                                        "friendImageMessage"
                                    }
                                }
                            }
                        }) { message ->
                        ChatMessageItem(modifier = Modifier, message, chatPageViewState.chat is Chat.Group) { message ->
                            //跳转图片预览
                            chatPageViewState.onClickMessage(message)
                        }
                    }
                }
            }


//            // 消息列表

            // 底部输入区（你可以在这里添加一个 TextField 和发送按钮）


//            PullToRefreshDefaults.Indicator(
//                modifier = Modifier
//                    .align(alignment = Alignment.TopCenter),
//                isRefreshing = loadMessageViewState.isRefreshing,
//                state = pullRefreshState,
//                color = Color.Blue,
//            )

        }
    }


}