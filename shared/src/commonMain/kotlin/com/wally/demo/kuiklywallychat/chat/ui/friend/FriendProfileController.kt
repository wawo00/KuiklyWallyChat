package com.wally.demo.kuiklywallychat.chat.ui.friend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.chat.base.BaseController
import com.wally.demo.kuiklywallychat.chat.base.PageNavigator
import com.wally.demo.kuiklywallychat.chat.base.PageNavigatorData
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.im.FriendProfileGateway
import com.wally.demo.kuiklywallychat.chat.im.ImResult
import com.wally.demo.kuiklywallychat.ext.Toast
import com.wally.demo.kuiklywallychat.ext.logNative
import com.wally.demo.timsdk.ui.friend.logic.ConfirmDialogViewState
import com.wally.demo.timsdk.ui.friend.logic.FriendProfilePageViewState
import com.wally.demo.timsdk.ui.friend.logic.SetFriendRemarkDialogViewState

/**
 * @author Wally(25054984)
 * @since 2026/8/10
 * @email wanlei@haier.com
 * @desciption 用于好友信息详情
 */
class FriendProfileController(navigator: PageNavigator,val gateway: FriendProfileGateway, val friendId: String) : BaseController(navigator) {

    private var latestProfileRequestId = 0

    var pageViewState by mutableStateOf(
        value = FriendProfilePageViewState(
            personProfile = null,
            isMe = false,
            isFriend = false,
            onClickSetFriendRemark = ::showSetFriendRemarkDialog,
            onClickAddFriend = ::addFriend,
            onClickDeleteFriend = ::showConfirmDialog,
            onClickChat = ::goToChat,
        )

    )
        private set


    var remarkDialogViewState by mutableStateOf(
        value = SetFriendRemarkDialogViewState(
            isVisible = false,
            remark = "",
            onDismissDialog = ::dismissSetFriendRemarkDialog,
            onSetFriendRemark = ::setFriendRemark
        )
    )
        private set



    override fun start() {
        super.start()

        confirmDialogViewState = confirmDialogViewState.copy(
                contentStr = "确定删除好友吗",
            )


        loadFriendProfile()

    }

    fun loadFriendProfile(expectedIsFriend: Boolean? = null) {
        val requestId = ++latestProfileRequestId
        showLoading()
        gateway.LoadFriendProfile(friendId) { result ->
            hideLoading()

            if (requestId != latestProfileRequestId) {
                "[FriendProfile][Controller] ignore stale load requestId=$requestId".logNative()
                return@LoadFriendProfile
            }

            when (result) {
                is ImResult.Success -> {
                    // 添加/删除接口成功后，其结果比紧随其后的资料缓存更权威。
                    val resolvedIsFriend = expectedIsFriend ?: result.data.isFriend
                    val resolvedProfile = if (result.data.isFriend == resolvedIsFriend) {
                        result.data
                    } else {
                        result.data.copy(isFriend = resolvedIsFriend)
                    }
                    val previous = pageViewState
                    val next = previous.copy(
                        personProfile = resolvedProfile,
                        isFriend = resolvedIsFriend,
                    )
                    pageViewState = next
                }

                is ImResult.Failure -> {
                    result.message.ifBlank { "加载用户资料失败" }.Toast()
                }
            }
        }
    }

    private fun updateFriendStatus(isFriend: Boolean, source: String) {
        val previous = pageViewState
        val next = previous.copy(
            personProfile = previous.personProfile?.copy(isFriend = isFriend),
            isFriend = isFriend,
        )
        pageViewState = next
    }

    private fun addFriend() {
        showLoading()
        gateway.addFriend(friendId) { result ->
            hideLoading()
            when (result) {
                is ImResult.Success -> {
                    updateFriendStatus(isFriend = true, source = "add success")
                    "操作成功".Toast()
                    loadFriendProfile(expectedIsFriend = true)
                }

                is ImResult.Failure -> {
                    result.message.ifBlank { "操作失败" }.Toast()
                }
            }
        }
    }

    fun confirmDeleteFriend() {
        showLoading()
        gateway.deleteFriend(friendId) { result ->
            hideLoading()
            when (result) {
                is ImResult.Success -> {
                    dismissConfirmDialog()
                    updateFriendStatus(isFriend = false, source = "delete success")
                    "操作成功".Toast()
                    loadFriendProfile(expectedIsFriend = false)
                }

                is ImResult.Failure -> {
                    result.message.ifBlank { "操作失败" }.Toast()
                }
            }
        }
    }

    fun setFriendRemark(remark: String) {
        showLoading()
        gateway.setFriendRemark(friendId, remark) { result ->
            hideLoading()
            when (result) {
                is ImResult.Success -> {
                    "操作成功".Toast()
                    loadFriendProfile()
                    remarkDialogViewState=remarkDialogViewState.copy(isVisible=false)
                }

                is ImResult.Failure -> {
                    result.message.ifBlank { "操作失败" }.Toast()
                }
            }
        }
    }

    // 用于ui显示
    fun dismissSetFriendRemarkDialog() {
        remarkDialogViewState = remarkDialogViewState.copy(isVisible = false)
    }

    private fun showSetFriendRemarkDialog() {
        remarkDialogViewState = remarkDialogViewState.copy(isVisible = true)
    }


    fun goToChat(personProile: PersonProfile) {
       var data= PageNavigatorData(
           pageName = "ChatPage",
           pageData = JSONObject().apply {
               put("conversationId", personProile.id)
               put("chatType", "c2c")
               put("conversationName", personProile.showName)}
       )
        goToPage(data)
    }

    override fun onConfirmDialogConfirmClicked() {
        confirmDeleteFriend()
    }
}