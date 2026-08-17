package com.wally.demo.kuiklywallychat.im.logic

import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeMap
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_GroupId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_GroupProfile
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_Items
import com.wally.demo.kuiklywallychat.ext.log
import com.wally.demo.kuiklywallychat.im.ImRuntime
import com.wally.demo.kuiklywallychat.im.account.NativeResult
import com.wally.demo.kuiklywallychat.im.logic.AccountLogic.failure
import com.wally.demo.kuiklywallychat.im.logic.AccountLogic.success
import kotlinx.coroutines.launch
import org.json.JSONObject

object GroupLogic {


    fun loadGroupProfile(params: String?, callback: KuiklyRenderCallback?) {
        "loadGroupProfile execute".log()
        val groupId= JSONObject(params ?:"{}").optString(PARAM_GroupId).trim()
        ImRuntime.AppCoroutineScope.launch {
            var result=ImRuntime.groupRepos.getGroupInfo(groupId)
            when(result){
                is NativeResult.Success -> {
                    callback?.invoke(
                        success(
                            mapOf(
                                PARAM_GroupProfile to result.data.toBridgeMap(),
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

    fun loadGroupMembers(params: String?, callback: KuiklyRenderCallback?) {
        "loadGroupMembers execute".log()
        val groupId= JSONObject(params ?:"{}").optString(PARAM_GroupId).trim()
        ImRuntime.AppCoroutineScope.launch {
            var result=ImRuntime.groupRepos.getGroupMemberList(groupId)
            when(result){
                is NativeResult.Success -> {
                    callback?.invoke(
                        success(
                            mapOf(
                                PARAM_Items  to result.data.map { it.toBridgeMap() },
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

    fun quitGroup(params: String?, callback: KuiklyRenderCallback?) {
        "quitGroup execute".log()
        val groupId= JSONObject(params ?:"{}").optString(PARAM_GroupId).trim()
        ImRuntime.AppCoroutineScope.launch {

           var result= ImRuntime.groupRepos.quitGroup(groupId)
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