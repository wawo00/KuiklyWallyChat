package com.wally.demo.kuiklywallychat.chat.ui.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

@Composable
fun UnreadBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val badgeText = when {
        count > 99 -> "99+"
        else -> count.toString()
    }

    Text(
        text = badgeText,
        modifier = modifier
            .background(
                color = Color.Red,
                shape = CircleShape,
            ).padding(2.dp),
        color = Color.White,
        fontSize = 10.sp,
        lineHeight = 10.sp,
        maxLines = 1,
    )
}