package com.wally.demo.kuiklywallychat.chat.ui.main.friendship

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.base.Utils.toast
import com.wally.demo.kuiklywallychat.chat.base.BaseController
import com.wally.demo.kuiklywallychat.chat.base.PageNavigator
import com.wally.demo.kuiklywallychat.chat.base.PageNavigatorData
import com.wally.demo.kuiklywallychat.chat.base.model.ActionResult
import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.im.FriendshipGateway
import com.wally.demo.kuiklywallychat.chat.im.ImResult
import com.wally.demo.kuiklywallychat.ext.Toast
import com.wally.demo.kuiklywallychat.ext.logNative
import com.wally.demo.timsdk.ui.main.friendship.logic.FriendshipState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class FriendShipController(navigator: PageNavigator,private val gateway: FriendshipGateway) : BaseController(navigator){

    /**
     * 页面销毁后变为 false。
     *
     * 原生异步回调仍有可能返回，但不能再修改页面状态。
     */
    var state by mutableStateOf(FriendshipState(onAddFriend = ::addFriend, onJoinGroup = ::onJoinGroup,onShowAddSheet=::showAddSheet))
        private set

    override  fun start() {
        loadJoinedGroups()
        loadFriends()
    }

    private fun loadFriends() {
        "loadFriends executed".logNative()
        gateway.loadFriends { result ->
            when (result) {
                is ImResult.Success -> {
                    Utils.logToNative("联系人请求成功.size is ${result.data.size}")
                    state =   state.copy(
                        friends = result.data,
                        isLoadingFriends = false
                    )
                }

                is ImResult.Failure -> {
                    state = state.copy(isLoadingFriends = false, errorMessage = result.message)
                }
            }
        }

    }

    /**
     * 提交添加好友请求。
     */
    fun addFriend(userId: String) {

        if (userId.isEmpty()) {
            state = state.copy(
                errorMessage = "请输入用户 ID",
            )
            return
        }
        state = state.copy(
            isSubmitting = true,
            errorMessage = null,
        )
        gateway.addFriend(userId) { result ->
            when (result) {
                is ImResult.Success -> {
                    "操作成功".Toast()
                    /*
                    * 主动重新加载，保证即使原生 FriendsChanged 事件丢失，
                    * 页面也可以显示最新列表。
                    */
                    dismissAddSheet()
                   var data= PageNavigatorData("ChatPage", pageData =JSONObject().apply {
                        put("conversationId", userId)
                        put("chatType", "c2c")
                        put("conversationName",userId)
                    } )
                    goToPage(data)
                }

                is ImResult.Failure -> {
                    state = state.copy(
                        isSubmitting = false,
                        errorMessage = result.message.orEmpty().ifBlank {
                            "添加好友失败"
                        },
                    )
                }
            }
        }
    }



    /**
     * 加载已加入的群列表。
     */
    private fun loadJoinedGroups() {
       showLoading()
        gateway.loadJoinedGroups { result ->
            hideLoading()
            state = when (result) {
                is ImResult.Success -> {
                    "UI groups=${result.data.map { it.id }}".logNative()
                    state.copy(
                        groups = result.data,
                        isLoadingGroups = false,
                    )
                }

                is ImResult.Failure -> {
                    state.copy(
                        isLoadingFriends = false, errorMessage = result.message,
                    )
                }
            }
        }
    }


    /**
     * 收到 ImEvent.FriendsChanged 后调用。
     *
     * 当前采用“失效通知”模式：
     * 原生只通知好友发生变化，Controller 再重新请求最终列表。
     */
    fun onFriendsChanged() {
//        if (!active) return
        loadFriends()
    }

    /**
     * 收到 ImEvent.GroupsChanged 后调用。
     */
    fun onGroupsChanged() {
        loadJoinedGroups()
    }


    fun showAddSheet(){
        state = state.copy(showAddFriendSheet = true)
    }
    fun dismissAddSheet(){
        // 只要把状态改为 false，界面就会消失
        state = state.copy(showAddFriendSheet = false)
    }

    fun onJoinGroup(index:Int){
        showLoading()
        val ids = listOf(
            "@TGS#3SSMB3WHI",
            "@TGS#3VOZA3WHT",
            "@TGS#3W42A3WHP",
            "@TGS#3DMJIK6MS",
            "@TGS#3YCNIK6MC"
        )

        gateway.joinGroup(ids[index]) { result ->
            dismissAddSheet()
            hideLoading()
            when (result) {
                is ImResult.Success -> {
                    /*
                    * 主动重新加载，保证即使原生 FriendsChanged 事件丢失，
                    * 页面也可以显示最新列表。
                    */
                    "操作成功".Toast()
                    loadJoinedGroups()
//                    loadFriends()
                }

                is ImResult.Failure -> {
                    result.message.orEmpty().ifBlank {
                        "入群失败"
                    }.Toast()
                }
            }
        }
    }


}