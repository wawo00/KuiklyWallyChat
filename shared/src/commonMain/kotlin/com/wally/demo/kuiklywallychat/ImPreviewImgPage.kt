package com.wally.demo.kuiklywallychat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.core.annotations.Page
import com.wally.demo.kuiklywallychat.base.BasePager
import com.wally.demo.kuiklywallychat.chat.im.DownloadModule
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_Preview_Img_ImgList
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_Preview_Img_InitPos
import com.wally.demo.kuiklywallychat.chat.im.MediaPickerModule
import com.wally.demo.kuiklywallychat.chat.ui.preview.PreviewImageScreen
import com.wally.demo.kuiklywallychat.chat.ui.preview.PreviewImgController
import com.wally.demo.kuiklywallychat.chat.ui.widgets.KuiklyAlertDialog
import com.wally.demo.kuiklywallychat.ext.Toast

import com.wally.demo.timsdk.widgets.LoadingDialog

@Page("PreviewImgPage", supportInLocal = true)
class ImPreviewImgPage : BasePager() {
    private var mControll: PreviewImgController? by mutableStateOf(null)
    override fun willInit() {
        super.willInit()

        setContent {
            val controller = mControll

            if (
                controller == null
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                return@setContent
            } else {
                val pageViewState = controller.pageViewState
                PreviewImageScreen(pageViewState)
                LoadingDialog(controller.loadingDialogViewState)
            }
        }
    }


    override fun created() {
        super.created()
        val gateway = acquireModule<DownloadModule>(DownloadModule.MODULE_NAME)
        val params = pageData.params
        val initPos = params.optInt(PARAM_Preview_Img_InitPos)
        val imgArray=params.optJSONArray(PARAM_Preview_Img_ImgList)
        val imgUrls = imgArray?.toList()?.filterIsInstance<String>()?:emptyList()

        val downloadGateway= acquireModule<DownloadModule>(
            DownloadModule.MODULE_NAME,
        )

        val newController = PreviewImgController(
            navigator = this,
            gateway = downloadGateway,
            initPos = initPos,
            imgList = imgUrls,
        )
        mControll = newController
        newController.start()
    }


    override fun pageWillDestroy() {
        mControll?.stop()
        super.pageWillDestroy()
    }


}