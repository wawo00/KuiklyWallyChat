package com.wally.demo.kuiklywallychat.chat.ui.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp
import com.wally.demo.kuiklywallychat.chat.tools.commonDrawable
import com.wally.demo.kuiklywallychat.chat.ui.main.BottomTabIcon

@Composable
fun NavigationIconWithBadge(
    icon: BottomTabIcon,
    unreadCount: Int,
    contentDescription: String,
) {
    val assetName = when (icon) {
        BottomTabIcon.CONVERSATION -> "ic_chat.png"
        BottomTabIcon.FRIENDSHIP -> "ic_explore.png"
        BottomTabIcon.PROFILE -> "ic_fresh.png"
    }


    Box(
        modifier = Modifier.size(50.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(
                    commonDrawable(assetName),
                ),
                contentDescription = contentDescription,
            )
        }

        if (unreadCount > 0) {
            UnreadBadge(
                modifier = Modifier.align(Alignment.TopStart),
                count = unreadCount,
            )
        }
    }
}