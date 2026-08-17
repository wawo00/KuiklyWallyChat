package com.wally.demo.timsdk.widgets


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxWithConstraints
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.tencent.kuikly.compose.material3.Surface
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.resources.DrawableResource
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.ColorFilter
import com.tencent.kuikly.compose.ui.graphics.SolidColor
import com.tencent.kuikly.compose.ui.graphics.painter.BrushPainter
import com.tencent.kuikly.compose.ui.graphics.painter.ColorPainter
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.base.attr.ImageUri
import com.wally.demo.kuiklywallychat.CommonContants
import com.wally.demo.kuiklywallychat.chat.base.model.ImageMessage
import com.wally.demo.kuiklywallychat.chat.base.model.Message
import com.wally.demo.kuiklywallychat.chat.base.model.MessageState
import com.wally.demo.kuiklywallychat.chat.base.model.TextMessage
import com.wally.demo.kuiklywallychat.chat.base.model.TimeMessage
import com.wally.demo.timsdk.ui.theme.AppTheme


/**
 * @author Wally(25054984)
 * @since 2026/7/14
 * @email wanlei@haier.com
 * @desciption
 */
@Composable
fun ChatMessageItem(modifier: Modifier, message: Message, showPartName: Boolean, onClick: (Message) -> Unit) {
    val isOwn = message.detail.isOwnMessage
    if (message is TimeMessage) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = message.formatMessage, color = Color.Black, fontSize = 12.sp)
        }
    } else if (message is TextMessage) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (message.detail.isOwnMessage) Arrangement.End else Arrangement.Start
        ) {
//      }
            if (!isOwn) {
                // --- 对方发送的消息布局：[头像] [间距] [气泡] [loading] ---
                MessageAvatar(modifier, url = message.detail.sender.avatarUrl)
                Spacer(modifier = Modifier.width(8.dp))
                // 气泡上面有名字
                // weight(fill = false) 让气泡最多占用剩余空间，超出则换行，避免把头像挤出屏幕
                if (showPartName) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(text = message.detail.sender.showName)
                        MessageContent(modifier, message = message, isOwn = isOwn)
                    }
                } else {
                    MessageContent(modifier.weight(1f, fill = false), message = message, isOwn = isOwn)

                }
                MessageStateUI(modifier = modifier, messageState = message.detail.state)
            } else {
                // --- 自己发送的消息布局：[loading][气泡] [间距] [头像] ---
                MessageStateUI(modifier = modifier, messageState = message.detail.state)
                if (showPartName) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(text = message.detail.sender.showName)
                        MessageContent(modifier = modifier, message = message, isOwn = isOwn)
                    }
                } else {
                    MessageContent(modifier = modifier.weight(1f, fill = false), message = message, isOwn = isOwn)
                }
                Spacer(modifier = Modifier.width(8.dp))
                MessageAvatar(modifier, url = message.detail.sender.avatarUrl)
            }
        }
    } else if (message is ImageMessage) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (message.detail.isOwnMessage) Arrangement.End else Arrangement.Start
        ) {
            if (!isOwn) {
                // --- 对方发送的消息布局：[头像] [间距] [tup] [loading] ---
                MessageAvatar(modifier, url = message.detail.sender.avatarUrl)
                Spacer(modifier = Modifier.width(8.dp))
                ImageMessage(Modifier, message, { onClick(message) })
                MessageStateUI(modifier = Modifier, messageState = message.detail.state)
            } else {
                // --- 自己发送的消息布局：[loading][图片] [间距] [头像] ---
                MessageStateUI(modifier = Modifier, messageState = message.detail.state)
                ImageMessage(Modifier, message, { onClick(message) })
                Spacer(modifier = Modifier.width(8.dp))
                MessageAvatar(modifier, url = message.detail.sender.avatarUrl)
            }
        }

    }

}

@Composable
private fun ImageMessage(
    modifier: Modifier,
    message: ImageMessage,
    onClickMessage: (Message) -> Unit,
) {
    val localDensity = LocalDensity.current
    val previewImage = message.previewImage
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = if (message.detail.isOwnMessage) {
            Alignment.TopEnd
        } else {
            Alignment.TopStart
        }
    ) {
        val imageWidth = previewImage.width
        val imageHeight = previewImage.height
        val imageMinWidthDp = maxWidth / 10f * 4
        val imageMaxWidthDp = maxWidth / 10f * 9
        val layout = remember(key1 = message.detail.msgId) {
            val isALegalWidthAndHeight = imageWidth > 0 && imageHeight > 0
            if (isALegalWidthAndHeight) {
                val ratio = 1.0f * imageWidth / imageHeight
                val imageWidthDp = with(localDensity) {
                    imageWidth.toDp()
                }
                val width = if (imageWidthDp <= imageMinWidthDp) {
                    imageMinWidthDp
                } else if (imageWidthDp < imageMaxWidthDp) {
                    imageWidthDp
                } else {
                    imageMaxWidthDp
                }
                width to ratio
            } else {
                imageMinWidthDp to 1.0f
            }
        }
        val (mWidth, mRatio) = layout
        AsyncImage(
            model = previewImage.url,
            contentScale = ContentScale.Crop,
            modifier= Modifier
                .width(width = mWidth)
                .aspectRatio(ratio = mRatio)
                .clip(shape = RoundedCornerShape(size = 6.dp))
                .clickable(onClick = {
                    onClickMessage(message)
                }),
        )
//        ComponentImage(
//            modifier = Modifier
//                .width(width = mWidth)
//                .aspectRatio(ratio = mRatio)
//                .clip(shape = RoundedCornerShape(size = 6.dp))
//                .clickable(onClick = {
//                    onClickMessage(message)
//                }),
//            model = previewImage.url,
//            contentScale = ContentScale.Crop,
//            alignment = Alignment.Center
//        )
    }
}

// 抽取头像组件
@Composable
fun MessageAvatar(modifier: Modifier, url: String?) {
    Image(
        painter = rememberAsyncImagePainter(
            url,
            placeholder = ColorPainter(Color.Gray),
        ),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape),
    )

}

// 抽取气泡组件
@Composable
fun MessageContent(modifier: Modifier, message: TextMessage, isOwn: Boolean) {
    Surface(
        shadowElevation = 2.dp,
        // 根据图片调整：对方通常是蓝色气泡，自己通常是白色或灰色（或反过来，这里遵循你之前的逻辑）
        color = if (isOwn) Color.LightGray else Color(0xFF5E97F6),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.padding(top = 4.dp)
    ) {
        Text(
            text = message.formatMessage,
            modifier = Modifier.padding(12.dp),
            // 蓝色背景配白色字，灰色背景配黑色字
            color = if (isOwn) Color.Black else Color.White
        )
    }
}

@OptIn(InternalResourceApi::class)
@Composable
private fun MessageStateUI(
    modifier: Modifier,
    messageState: MessageState,
) {
    val logoDrawable = DrawableResource(ImageUri.pageAssets(CommonContants.LOGO_ICON).toUrl("ChatDemo"))
    Box(
        modifier = modifier
            .size(size = 20.dp)
    ) {
        when (messageState) {
            MessageState.Sending -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Color(color = 0xFF42A5F5),
                    strokeWidth = 2.dp
                )
            }

            is MessageState.Failed -> {
                Image(
                    modifier = Modifier
                        .fillMaxSize(),
                    painter = painterResource(logoDrawable),
                    colorFilter =ColorFilter.tint(color = AppTheme.colorScheme.c_FFFF545C_FFFA525A.color),
                    contentDescription = null
                )
            }

            MessageState.Success -> {
            }
        }
    }
}

