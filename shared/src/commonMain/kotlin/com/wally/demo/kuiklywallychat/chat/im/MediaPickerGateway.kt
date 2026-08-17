package com.wally.demo.kuiklywallychat.chat.im

/**
  * @author Wally(25054984)
  * @since 2026/8/6
  * @email wanlei@haier.com
  * @desciption 用于端侧获取图片
  */

interface MediaPickerGateway {
    fun pickImage(
        source: ImagePickSource,
        callback: (ImagePickResult) -> Unit,
    )


}

enum class ImagePickSource {
    Camera,
    Album,
}

sealed interface ImagePickResult {
    data object Cancelled : ImagePickResult
    data class Success(val localPath: String) : ImagePickResult
    data class Failure(val message: String) : ImagePickResult
}