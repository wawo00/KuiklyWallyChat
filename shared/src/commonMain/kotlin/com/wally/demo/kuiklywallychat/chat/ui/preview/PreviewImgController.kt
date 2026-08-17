package com.wally.demo.kuiklywallychat.chat.ui.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.chat.base.BaseController
import com.wally.demo.kuiklywallychat.chat.base.PageNavigator
import com.wally.demo.kuiklywallychat.chat.base.PageNavigatorData
import com.wally.demo.kuiklywallychat.chat.base.model.GroupMemberProfile
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.im.DownLoadGateway
import com.wally.demo.kuiklywallychat.chat.im.DownloadResult
import com.wally.demo.kuiklywallychat.chat.im.GroupProfileGateway
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendId
import com.wally.demo.kuiklywallychat.chat.im.ImResult
import com.wally.demo.kuiklywallychat.chat.im.ImagePickResult
import com.wally.demo.kuiklywallychat.chat.im.ImagePickSource
import com.wally.demo.kuiklywallychat.chat.ui.chat.InputSelectorType
import com.wally.demo.kuiklywallychat.ext.Toast
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * @author Wally(25054984)
 * @since 2026/8/14
 * @email wanlei@haier.com
 * @desciption 用于图片预览和下载
 */
class PreviewImgController(
    navigator: PageNavigator,
    val gateway: DownLoadGateway,
    val initPos: Int,
    val imgList: List<String>,

    ) : BaseController(navigator) {

    var pageViewState by mutableStateOf(
        value = PreviewImgViewState(
            imgList = imgList,
            initPos = initPos,
            downloadCLick = ::downloadClick
        )
    )
        private set


    fun downloadClick(
        url: String,
    ) {
        showLoading()
        gateway.downloadImage(
            imageUrl = url,
        ) { result ->
            hideLoading()
            when (result) {
                is DownloadResult.Success -> {
                    "成功下载到 :${result.localPath}".Toast()
                }

                DownloadResult.Cancelled -> {
                    // 用户取消选择，不需要提示错误
                }

                is DownloadResult.Failure -> {
                    "下载失败：${result.message}".Toast()
                }
            }
        }
    }


}