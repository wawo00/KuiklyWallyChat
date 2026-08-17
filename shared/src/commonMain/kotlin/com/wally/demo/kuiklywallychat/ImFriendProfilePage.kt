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
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_ShowStartChat
import com.wally.demo.kuiklywallychat.chat.ui.friend.FriendProfileController
import com.wally.demo.kuiklywallychat.chat.ui.widgets.KuiklyAlertDialog
import com.wally.demo.timsdk.ui.friend.FriendProfileScreen
import com.wally.demo.timsdk.ui.friend.SetFriendRemarkDialog
import com.wally.demo.timsdk.widgets.LoadingDialog

@Page("FriendProfilePage", supportInLocal = true)
class ImFriendProfilePage : BasePager() {

    private var friendProfileController: FriendProfileController? by mutableStateOf(null)

    private var showStartChat=true //聊天详情中右上角进来的不显示开始聊天按钮

    override fun willInit() {
        super.willInit()

        setContent {
            val friendProfileController = friendProfileController

            if (
                friendProfileController == null
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                return@setContent
            } else {
                val pageViewState = friendProfileController.pageViewState
                FriendProfileScreen(pageViewState,showStartChat){
                    closeCurrentPage()
                }
                KuiklyAlertDialog(friendProfileController.confirmDialogViewState)
                SetFriendRemarkDialog(friendProfileController.remarkDialogViewState)
                LoadingDialog(friendProfileController.loadingDialogViewState)
            }
        }
    }


    override fun created() {
        super.created()
        val gateway = acquireModule<ImModule>(ImModule.MODULE_NAME)
        val params = pageData.params
        val friendId = params.optString(PARAM_FriendId).trim()
         showStartChat=params.optBoolean(PARAM_ShowStartChat,true)
        val newFriendProfileController = FriendProfileController(navigator = this, gateway = gateway, friendId)
        friendProfileController = newFriendProfileController
        newFriendProfileController.start() //todo:实际编译时若当前 Kuikly 生命周期要求 acquireModule 在 created() 后调用，则在 created() 中创建 Controller
    }


    override fun pageWillDestroy() {
        friendProfileController?.stop()
        super.pageWillDestroy()
    }




}