package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.SolidColor
import com.tencent.kuikly.compose.ui.graphics.painter.BrushPainter
import com.tencent.kuikly.compose.ui.graphics.painter.ColorPainter
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.unit.dp
import com.wally.demo.kuiklywallychat.ext.logNative


@Composable
fun FriendShipListItem(
    modifier: Modifier = Modifier,
    imageUrl: String,
    title: String,
    subTitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                imageUrl,
                placeholder = ColorPainter(Color.LightGray),
                error = ColorPainter(Color.Red)
            ),
            contentDescription = title,
            modifier = Modifier
                .size(64.dp)
                .clip(shape = RoundedCornerShape(size = 6.dp)),
        )
        Spacer(Modifier.size(10.dp))
        Column {
            Text(text = title)
            Spacer(Modifier.height(10.dp))
            Text(text = subTitle)
        }
    }
}
