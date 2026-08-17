package com.wally.demo.kuiklywallychat.chat.ui.preview

data class PreviewImgViewState(
    var imgList: List<String> =emptyList(),
    var initPos: Int=0,
    var downloadCLick: (String) -> Unit
)