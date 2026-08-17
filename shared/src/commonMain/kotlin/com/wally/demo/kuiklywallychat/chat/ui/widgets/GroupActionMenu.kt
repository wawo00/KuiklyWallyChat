package com.wally.demo.kuiklywallychat.chat.ui.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Card
import com.tencent.kuikly.compose.material3.CardDefaults
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.ExperimentalComposeUiApi
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.IntOffset
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.window.Popup
import com.tencent.kuikly.compose.ui.window.PopupProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GroupActionMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEditAvatar: () -> Unit,
    onQuit: () -> Unit,
) {
    if (!expanded) return

    Popup(
        alignment = Alignment.TopEnd,
        // 对应原本 DpOffset(x = -16.dp, y = 8.dp)。
        // Kuikly Popup 使用 IntOffset，单位是像素。
        offset = IntOffset(x = -16, y = 8),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Card(
            modifier = Modifier.width(160.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(8.dp),
        ) {
            Column {
                PopupMenuItem(
                    text = "修改头像",
                    onClick = {
                        onDismissRequest()
                        onEditAvatar()
                    },
                )

                PopupMenuItem(
                    text = "退出群聊",
                    isDestructive = true,
                    onClick = {
                        onDismissRequest()
                        onQuit()
                    },
                )
            }
        }
    }
}

@Composable
private fun PopupMenuItem(
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = if (isDestructive) {
                Color(0xFFD32F2F)
            } else {
                Color(0xFF222)
            },
        )
    }
}