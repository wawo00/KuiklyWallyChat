package com.wally.demo.timsdk.ui.main.conversation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Card
import com.tencent.kuikly.compose.material3.CardDefaults
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.unit.IntOffset
import com.tencent.kuikly.compose.ui.unit.dp
import com.wally.demo.kuiklywallychat.chat.base.model.WallyConversation
import com.wally.demo.timsdk.ui.main.conversation.logic.ConversationMenuState
import com.wally.demo.timsdk.ui.main.conversation.logic.ConversationPageViewState
import com.wally.demo.timsdk.widgets.ConversationListItem
import com.wally.demo.timsdk.widgets.EmptyPage
import com.wally.demo.timsdk.widgets.serverConnectState


@Composable
fun ConversationScreen(
    pageViewState: ConversationPageViewState,
    onClickConversation: (conversation: WallyConversation) -> Unit,
) {

    var menuState by remember {
        mutableStateOf<ConversationMenuState?>(null)
    }
    LaunchedEffect(pageViewState.scrollTrigger) {
        if (pageViewState.scrollTrigger > 0) {
            pageViewState.listState.animateScrollToItem(0)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        //显示lazycolumn
        LazyColumn(modifier = Modifier.fillMaxSize(), state = pageViewState.listState) {
            //先显示im状态
            serverConnectState(modifier = Modifier, pageState = pageViewState)
            var list = pageViewState.conversationList
            if (list.isEmpty()) {
                item(key = "empty") {
                    EmptyPage(Modifier)
                }
            } else {
                items(pageViewState.conversationList, key = { conversation -> conversation.id }) { wallyConversation ->
                    ConversationListItem(conversation = wallyConversation, pageViewState = pageViewState, onClickConversation = { clickedItem->
                        onClickConversation(clickedItem)
                    },onShowMoreMenu = { selectedConversation, position, size ->
                        menuState = ConversationMenuState(
                            conversation = selectedConversation,
                            itemPosition = position,
                            itemSize = size,
                        )
                    })
                }
            }

        }
        menuState?.let { state ->
            ConversationActionMenuOverlay(
                state = state,
                pageViewState = pageViewState,
                onDismiss = {
                    menuState = null
                },
            )
        }
    }


}

@Composable
fun ConversationActionMenuOverlay(
    state: ConversationMenuState,
    pageViewState: ConversationPageViewState,
    onDismiss: () -> Unit,
) {
    val menuWidth = 140.dp
    val density = LocalDensity.current

    val menuWidthPx = with(density) {
        menuWidth.roundToPx()
    }

    val verticalOffsetPx = with(density) {
        8.dp.roundToPx()
    }

    val menuPosition = IntOffset(
        x = state.itemPosition.x +
                (state.itemSize.width - menuWidthPx) / 2,
        y = state.itemPosition.y- state.itemSize.height / 2,//-verticalOffsetPx
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember {
                    MutableInteractionSource()
                },
                onClick = onDismiss,
            ),
    ) {
        Card(
            modifier = Modifier
                .offset {
                    menuPosition
                }
                .width(menuWidth)
                .clickable(
                    indication = null,
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    onClick = {},
                ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(8.dp),
        ) {
            Column {
                ActionMenuItem(
                    text = "删除",
                    onClick = {
                        onDismiss()
                        pageViewState.onDeleteConversation(
                            state.conversation,
                        )
                    },
                )

                ActionMenuItem(
                    text = if (state.conversation.isPinned) {
                        "取消置顶"
                    } else {
                        "置顶"
                    },
                    onClick = {
                        onDismiss()
                        pageViewState.onPinConversation(
                            state.conversation,
                            !state.conversation.isPinned,
                        )
                    },
                )
            }
        }
    }
}
@Composable
private fun ActionMenuItem(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = text)
    }
}
