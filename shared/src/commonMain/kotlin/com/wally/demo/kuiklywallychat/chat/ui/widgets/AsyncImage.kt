package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.wally.demo.kuiklywallychat.chat.tools.commonDrawable

private const val DEFAULT_PLACEHOLDER_ASSET = "ic_holder_def.png"
private const val DEFAULT_ERROR_ASSET = "ic_error.png"

/**
 * Kuikly Compose implementation compatible with the commonly used Coil
 * AsyncImage(model, contentDescription, modifier) call shape.
 */
@Composable
fun AsyncImage(
    model: String?,
    contentDescription: String?=null,
    modifier: Modifier = Modifier,
    contentScale :ContentScale= ContentScale.Crop,
) {
    val placeholderPainter = painterResource(
        commonDrawable(DEFAULT_PLACEHOLDER_ASSET),
    )
    val errorPainter = painterResource(
        commonDrawable(DEFAULT_ERROR_ASSET),
    )

    Image(
        painter = rememberAsyncImagePainter(
            model = model,
            placeholder = placeholderPainter,
            error = errorPainter,
        ),
        contentScale=contentScale,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
