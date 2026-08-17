package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.ExperimentalFoundationApi
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.combinedClickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.wrapContentSize
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Card
import com.tencent.kuikly.compose.material3.CardDefaults
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.ExperimentalComposeUiApi
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.onGloballyPositioned
import com.tencent.kuikly.compose.ui.layout.positionInRoot
import com.tencent.kuikly.compose.ui.layout.positionInWindow
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.IntOffset
import com.tencent.kuikly.compose.ui.unit.IntSize
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.compose.ui.window.Popup
import com.tencent.kuikly.compose.ui.window.PopupProperties
import com.wally.demo.kuiklywallychat.chat.base.model.WallyConversation
import com.wally.demo.kuiklywallychat.ext.logNative
import com.wally.demo.timsdk.ui.main.conversation.logic.ConversationPageViewState
import kotlin.math.roundToInt


/**
 * @author Wally(25054984)
 * @since 2026/7/7
 * @email wanlei@haier.com
 * @desciption 用于聊天列表中的item,带有未读和长按显示选择弹窗
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationListItem(
    modifier: Modifier = Modifier.fillMaxWidth(),
    conversation: WallyConversation,
    pageViewState: ConversationPageViewState,
    onClickConversation: (WallyConversation) -> Unit,
    onShowMoreMenu: (
        conversation: WallyConversation,
        position: IntOffset,
        size: IntSize,
    ) -> Unit
) {



    //记录item的坐标和大小，用于显示dropmeanu


    var itemPosition by remember(conversation.id) {
        mutableStateOf(IntOffset.Zero)
    }

    var itemSize by remember(conversation.id) {
        mutableStateOf(IntSize.Zero)
    }


    var menuExpanded by remember {
        mutableStateOf(value = false)
    }
    Box(
        modifier = modifier
            .padding(10.dp)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                //使用lognative打印postion
//                "position: $position".logNative()

                itemPosition = IntOffset(
                    x = position.x.roundToInt(),
                    y = position.y.roundToInt(),
                )

                itemSize = coordinates.size
            }
            .combinedClickable(onClick = {
                onClickConversation(conversation)
            }, onLongClick = {
                onShowMoreMenu(
                    conversation,
                    itemPosition,
                    itemSize,
                )
            })
    ) {
        Row {
            Box {
                AsyncImage(
                    model = conversation.avatarUrl,
                    contentDescription = "",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(shape = RoundedCornerShape(size = 6.dp)),
                )
                //未读数
                if (conversation.unreadMessageCount > 0) {
                    Text(
                        textAlign = TextAlign.Center,
                        text = conversation.unreadMessageCount.toString(),
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(color = 0xFF42A5F5), shape = CircleShape)
                            .wrapContentSize(align = Alignment.Center)
                            .align(Alignment.TopEnd),
                        color = Color.White,
                        /**
                         * style = TextStyle(
                         *     fontSize = 12.sp,
                         *     platformStyle = PlatformTextStyle(
                         *         includeFontPadding = false,
                         *     ),
                         * ) 替换成下面的
                         */
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        maxLines = 1,
                    )
                }

            }
            Spacer(Modifier.size(10.dp))
            Column {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.name,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = conversation.lastMessage.detail.conversationTime)
                }
                Spacer(Modifier.height(10.dp))
                Text(text = conversation.formatMessage)
            }
        }
//        MoreActionMenu(
//            expanded = menuExpanded,
//            anchorPosition = itemPosition,
//            anchorSize = itemSize,
//            conversation = conversation,
//            pageViewState = pageViewState,
//            onDismiss = {
//                menuExpanded = false
//            },
//        )
    }

}
//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//fun MoreActionMenu(
//    expanded: Boolean,
//    anchorPosition: IntOffset,
//    anchorSize: IntSize,
//    onDismiss: () -> Unit,
//    conversation: WallyConversation,
//    pageViewState: ConversationPageViewState,
//) {
//    if (!expanded) return
//
//    val menuWidth = 140.dp
//    val density = LocalDensity.current
//
//    val menuWidthPx = with(density) {
//        menuWidth.roundToPx()
//    }
//
//    val verticalOffsetPx = with(density) {
//        8.dp.roundToPx()
//    }
//
//    val popupOffset = IntOffset(
//        x = anchorPosition.x +
//                (anchorSize.width - menuWidthPx) / 2,
//        y = anchorPosition.y +
//                anchorSize.height / 2 +
//                verticalOffsetPx,
//    )
//
//    val pinActionText =
//        if (conversation.isPinned) {
//            "取消置顶"
//        } else {
//            "置顶"
//        }
//
//    Popup(
//        alignment = Alignment.TopStart,
//        offset = popupOffset,
//        onDismissRequest = onDismiss,
//        properties = PopupProperties(
//            focusable = true,
//            dismissOnBackPress = true,
//            dismissOnClickOutside = true,
//        ),
//    ) {
//        Card(
//            modifier = Modifier.width(menuWidth),
//            shape = RoundedCornerShape(8.dp),
//            elevation = CardDefaults.cardElevation(8.dp),
//        ) {
//            Column {
//                ActionMenuItem(
//                    text = "删除",
//                    onClick = {
//                        onDismiss()
//                        pageViewState.onDeleteConversation(
//                            conversation,
//                        )
//                    },
//                )
//
//                ActionMenuItem(
//                    text = pinActionText,
//                    onClick = {
//                        onDismiss()
//                        pageViewState.onPinConversation(
//                            conversation,
//                            !conversation.isPinned,
//                        )
//                    },
//                )
//            }
//        }
//    }
//}


