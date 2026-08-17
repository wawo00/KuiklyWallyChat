package com.wally.demo.kuiklywallychat.chat.ui.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.ColorFilter
import com.tencent.kuikly.compose.ui.graphics.ColorFilter.Companion.tint
import com.tencent.kuikly.compose.ui.unit.dp
import com.wally.demo.kuiklywallychat.chat.tools.commonDrawable

@Composable
fun ImageButton(
    assetName: String,
    contentDescription: String="",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                commonDrawable(assetName+".png"),
            ),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            colorFilter=colorFilter
        )
    }
}