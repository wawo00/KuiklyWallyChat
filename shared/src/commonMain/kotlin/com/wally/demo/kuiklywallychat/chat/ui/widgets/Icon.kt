package com.wally.demo.kuiklywallychat.chat.ui.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp
import com.wally.demo.kuiklywallychat.chat.tools.commonDrawable

/**
  * @author Wally(25054984)
  * @since 2026/7/31
  * @email wanlei@haier.com
  * @desciption 用于模仿android.compose中icon，assetsname传入的时候不带.png，在内部进行拼装
  */
@Composable
fun Icon (assetName:String,size:Int=24) {
    Image(
        painter = painterResource(commonDrawable(assetName+".png")),
        contentDescription = "",
        modifier = Modifier
            .size(size.dp)
    )
}