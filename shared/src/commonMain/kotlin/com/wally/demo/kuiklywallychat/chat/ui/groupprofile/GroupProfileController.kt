package com.wally.demo.kuiklywallychat.chat.ui.groupprofile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.chat.base.BaseController
import com.wally.demo.kuiklywallychat.chat.base.PageNavigator
import com.wally.demo.kuiklywallychat.chat.base.PageNavigatorData
import com.wally.demo.kuiklywallychat.chat.base.model.GroupMemberProfile
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.im.GroupProfileGateway
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendId
import com.wally.demo.kuiklywallychat.chat.im.ImResult
import com.wally.demo.kuiklywallychat.ext.Toast
import com.wally.demo.timsdk.ui.friend.logic.ConfirmDialogViewState
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * @author Wally(25054984)
 * @since 2026/8/10
 * @email wanlei@haier.com
 * @desciption 用于好友信息详情
 */
class GroupProfileController(navigator: PageNavigator, val gateway: GroupProfileGateway, val groupId: String,val onQuit:()->Unit) : BaseController(navigator) {

    var pageViewState by mutableStateOf(
        value = GroupProfilePageViewState(
            groupInfo = null,
            memberList = emptyList(),
            onItemClick = ::onItemClick,
            onChangeAvatar = ::onChangeAvatar,
            onClickQuitGroup = ::showConfirmDialog
        )
    )

        private set


    override fun start() {
        super.start()
//        var getProfile = async { groupProvider.getGroupInfo(groupId) }
//        var getMembers = async { groupProvider.getGroupMemberList(groupId) }
//        var profileResult = getProfile.await()
//        var membersResult = getMembers.await()
//        hideLoading()
//        pageViewState = pageViewState.copy(
//            groupInfo = profileResult,
//            memberList = membersResult
//        )
//        loadInitialDataByCallback()

        confirmDialogViewState =
            confirmDialogViewState.copy(
                contentStr = "确认退群吗",
            )
        loadInitialDataByCoroutine()
    }

    private fun loadInitialDataByCallback() {
        showLoading()

        var profileResult: ImResult<GroupProfile>? = null
        var membersResult: ImResult<List<GroupMemberProfile>>? = null
        var hasHandledResult = false

        fun tryHandleResult() {
            if (hasHandledResult) {
                return
            }

            val profile = profileResult ?: return
            val members = membersResult ?: return

            hasHandledResult = true
            hideLoading()

            when {
                profile is ImResult.Failure -> {
                    profile.message
                        .ifBlank { "加载群资料失败" }
                        .Toast()
                }

                members is ImResult.Failure -> {
                    members.message
                        .ifBlank { "加载群成员失败" }
                        .Toast()
                }

                profile is ImResult.Success &&
                        members is ImResult.Success -> {
                    pageViewState = pageViewState.copy(
                        groupInfo = profile.data,
                        memberList = members.data,
                    )
                }
            }
        }

        gateway.loadGroupProfileByCallback(groupId) { result ->
            profileResult = result
            tryHandleResult()
        }

        gateway.loadGroupMembersByCallback(groupId) { result ->
            membersResult = result
            tryHandleResult()
        }
    }


    /**
     * 使用协程实现，存在问题
     */
    private fun loadInitialDataByCoroutine() {
        controllerScope.launch {
            showLoading()

            try {
                val results =
                    supervisorScope {
                        val profileDeferred =
                            async {
                                gateway.loadGroupProfile(groupId)
                            }

                        val membersDeferred =
                            async {
                                gateway.loadGroupMembers(groupId)
                            }

                        profileDeferred.await() to
                                membersDeferred.await()
                    }

                val profileResult = results.first
                val membersResult = results.second

                if (
                    profileResult is ImResult.Success &&
                    membersResult is ImResult.Success
                ) {
                    pageViewState = pageViewState.copy(
                        groupInfo = profileResult.data,
                        memberList = membersResult.data,
                    )
                } else {
                    val message =
                        when {
                            profileResult is ImResult.Failure ->
                                profileResult.message

                            membersResult is ImResult.Failure ->
                                membersResult.message

                            else ->
                                "加载群资料失败"
                        }

                    message
                        .ifBlank { "加载群资料失败" }
                        .Toast()
                }
            } finally {
                hideLoading()
            }
        }
    }


    fun onItemClick(profile: GroupMemberProfile) {
        var data = PageNavigatorData(
            pageName = "FriendProfilePage",
            pageData = JSONObject().apply {
                put(PARAM_FriendId, profile.detail.id)
            }
        )

        goToPage(data)
    }

    fun onChangeAvatar(profile: GroupProfile) {
        "不能修改头像".Toast()
    }

    fun confirmQuit() {
        showLoading()
        gateway.quitGroup(groupId) { result ->
            hideLoading()
            when (result) {
                is ImResult.Success -> {
                    dismissConfirmDialog()
                    onQuit()
                    "操作成功".Toast()
                }

                is ImResult.Failure -> {
                    result.message.ifBlank { "操作失败" }.Toast()
                }
            }
        }
    }


    override fun onConfirmDialogConfirmClicked() {
        confirmQuit()
    }


}