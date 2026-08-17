package com.wally.demo.kuiklywallychat.chat.tools

import com.tencent.kuikly.compose.resources.DrawableResource
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.core.base.attr.ImageUri

@OptIn(InternalResourceApi::class)
 fun commonDrawable(name: String): DrawableResource {
    return DrawableResource(
        ImageUri.commonAssets(name).toUrl(""),
    )
}