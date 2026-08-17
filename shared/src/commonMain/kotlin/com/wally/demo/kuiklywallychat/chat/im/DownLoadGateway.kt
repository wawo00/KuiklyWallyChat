package com.wally.demo.kuiklywallychat.chat.im

interface DownLoadGateway {
    fun downloadImage(
        imageUrl:String,
        callback:(DownloadResult)-> Unit
    )
}

sealed interface DownloadResult {
    data object Cancelled : DownloadResult
    data class Success(val localPath: String?) : DownloadResult
    data class Failure(val message: String) : DownloadResult
}