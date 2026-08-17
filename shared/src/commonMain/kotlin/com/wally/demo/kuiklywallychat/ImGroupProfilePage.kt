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
import com.wally.demo.kuiklywallychat.chat.im.ImModule
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_GroupId
import com.wally.demo.kuiklywallychat.chat.ui.groupprofile.GroupProfileController
import com.wally.demo.kuiklywallychat.chat.ui.groupprofile.GroupProfileScreen
import com.wally.demo.kuiklywallychat.chat.ui.widgets.KuiklyAlertDialog

import com.wally.demo.timsdk.widgets.LoadingDialog

@Page("GroupProfilePage", supportInLocal = true)
class ImGroupProfilePage : BasePager() {
    private var groupProfileController: GroupProfileController? by mutableStateOf(null)
    private var showStartChat=true //聊天详情中右上角进来的不显示开始聊天按钮
    override fun willInit() {
        super.willInit()

        setContent {
            val controller = groupProfileController

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
                GroupProfileScreen(pageViewState)
                KuiklyAlertDialog(controller.confirmDialogViewState)
                LoadingDialog(controller.loadingDialogViewState)
            }
        }
    }


    override fun created() {
        super.created()
        val gateway = acquireModule<ImModule>(ImModule.MODULE_NAME)
        val params = pageData.params
        val groupId = params.optString(PARAM_GroupId).trim()
        val newController = GroupProfileController(navigator = this, gateway = gateway, groupId, onQuit = {closeCurrentPage()})
        groupProfileController = newController
        newController.start() //todo:实际编译时若当前 Kuikly 生命周期要求 acquireModule 在 created() 后调用，则在 created() 中创建 Controller
    }


    override fun pageWillDestroy() {
        groupProfileController?.stop()
        super.pageWillDestroy()
    }


}