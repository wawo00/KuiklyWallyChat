package com.wally.demo.kuiklywallychat.im.logic

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.chat.base.model.ActionResult
import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeMap
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_GroupId
import com.wally.demo.kuiklywallychat.im.ImRuntime
import com.wally.demo.kuiklywallychat.im.account.NativeResult
import com.wally.demo.kuiklywallychat.im.logic.LoginLogic.failure
import com.wally.demo.kuiklywallychat.im.logic.LoginLogic.success
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

object FriendshipLogic {
    fun loadFriends(callback: KuiklyRenderCallback?) {
        Log.i("FriendshipLogic", "logic中执行loadFriends: ")
        ImRuntime.AppCoroutineScope.launch {
            when (val result = ImRuntime.friendshipRepository.loadFriends()) {
                is NativeResult.Success -> {
                    callback?.invoke(
                        success(
                            mapOf(
                                "items" to result.data.map {
                                    it.toBridgeMap()
                                },
                            ),
                        ),
                    )
                }

                is NativeResult.Failure -> {
                    callback?.invoke(
                        failure(result.code, result.message)
                    )
                }
            }
        }
    }

    fun loadJoinedGroups(callback: KuiklyRenderCallback?) {
        Log.i("FriendshipLogic", "logic中执行loadJoinedGroups: ")
        ImRuntime.AppCoroutineScope.launch {
            when (val result = ImRuntime.groupRepos.getJoinedGroupListOrigin()) {
                is NativeResult.Success -> {
                    Log.i("FriendshipLogic", "groups=${result.data.map { it.id }}")
                    callback?.invoke(
                        success(
                            mapOf(
                                "items" to result.data.map {
                                    it.toBridgeMap()
                                },
                            ),
                        ),
                    )
                }

                is NativeResult.Failure -> {
                    callback?.invoke(
                        failure(result.code, result.message)
                    )
                }
            }
        }
    }


    fun addFriend(params:String?,callback: KuiklyRenderCallback?){
        // 获得userId
        val userId= JSONObject(params ?:"{}").optString(PARAM_FriendId).trim()
        if (userId.isEmpty()){
            callback?.invoke(
                failure(-1, "用户id不能为空")
            )
            return
        }
        ImRuntime.AppCoroutineScope.launch {
            var result=ImRuntime.friendshipRepository.addFriend(userId)
            when(result){
                is NativeResult.Success->{
                    callback?.invoke(
                        success(emptyMap())
                    )
                }
                is NativeResult.Failure->{
                    callback?.invoke(
                        failure(result.code,result.message)
                    )
                }

            }

        }
    }

    fun joinGroup(params:String?,callback: KuiklyRenderCallback?) {
        // 获得userId
        val userId= JSONObject(params ?:"{}").optString(PARAM_GroupId).trim()
        if (userId.isEmpty()){
            callback?.invoke(
                failure(-1, "群组id不能为空")
            )
            return
        }
        ImRuntime.AppCoroutineScope.launch {
            var result=ImRuntime.groupRepos.joinGroup(userId)
            when(result){
                is NativeResult.Success->{
                    callback?.invoke(
                        success(emptyMap())
                    )
                }
                is NativeResult.Failure->{
                    callback?.invoke(
                        failure(result.code,result.message)
                    )
                }

            }

        }

    }

}