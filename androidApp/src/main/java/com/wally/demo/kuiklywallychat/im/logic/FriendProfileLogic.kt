package com.wally.demo.kuiklywallychat.im.logic

import android.util.Log
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeMap
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendProfile
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_Remark
import com.wally.demo.kuiklywallychat.im.ImRuntime
import com.wally.demo.kuiklywallychat.im.account.NativeResult
import com.wally.demo.kuiklywallychat.im.logic.AccountLogic.failure
import com.wally.demo.kuiklywallychat.im.logic.AccountLogic.success
import kotlinx.coroutines.launch
import org.json.JSONObject

object FriendProfileLogic {

    fun LoadFriendProfile(params:String?,callback: KuiklyRenderCallback?) {
        Log.i("FriendProfileLogic", "LoadFriendProfile: ")
        val friendId= JSONObject(params ?:"{}").optString(PARAM_FriendId).trim()
        if (friendId.isEmpty()){
            callback?.invoke(
                failure(-1, "用户id不能为空")
            )
            return
        }
        ImRuntime.AppCoroutineScope.launch {
            when (val result = ImRuntime.friendshipRepository.getFriendProfile(friendId)) {
                is NativeResult.Success -> {
                    callback?.invoke(
                        success(
                            mapOf(
                                PARAM_FriendProfile to result.data.toBridgeMap()
                            ),
                        ),
                    )
                }

                is NativeResult.Failure -> {
                    callback?.invoke(
                        failure(result.code, result.message)
                    )
                }
                else -> {
                    callback?.invoke(
                        failure(-1, "用户信息为空")
                    )
                }
            }
        }
    }



    fun deleteFriend(params:String?,callback: KuiklyRenderCallback?){
        // 获得userId
        val friendId= JSONObject(params ?:"{}").optString(PARAM_FriendId).trim()
        if (friendId.isEmpty()){
            callback?.invoke(
                failure(-1, "用户id不能为空")
            )
            return
        }
        ImRuntime.AppCoroutineScope.launch {
            var result=ImRuntime.friendshipRepository.deleteFriend(friendId)
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

    fun setFriendRemark(params:String?,callback: KuiklyRenderCallback?){
        // 获得userId
        val friendId= JSONObject(params ?:"{}").optString(PARAM_FriendId).trim()
        val remark= JSONObject(params ?:"{}").optString(PARAM_Remark).trim()
        if (friendId.isEmpty()){
            callback?.invoke(
                failure(-1, "用户id不能为空")
            )
            return
        }
        if (remark.isEmpty()){
            callback?.invoke(
                failure(-1, "备注不能为空" )
            )
            return
        }
        ImRuntime.AppCoroutineScope.launch {
            var result=ImRuntime.friendshipRepository.setFriendRemark(friendId,remark)
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