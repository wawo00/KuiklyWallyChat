package com.wally.demo.kuiklywallychat.im.logic

import android.util.Log
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.chat.base.model.ActionResult
import com.wally.demo.kuiklywallychat.chat.base.model.WallyConversation
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeMap
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeValue
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_ConversationId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_PinConversation
import com.wally.demo.kuiklywallychat.ext.log
import com.wally.demo.kuiklywallychat.im.ImRuntime
import com.wally.demo.kuiklywallychat.im.account.NativeResult
import com.wally.demo.kuiklywallychat.im.logic.AccountLogic.failure
import com.wally.demo.kuiklywallychat.im.logic.AccountLogic.success
import com.wally.demo.kuiklywallychat.im.logic.ConversationLogic.TAG
import kotlinx.coroutines.launch
import org.json.JSONObject

object ConversationLogic {

    private val TAG = "ConversationLogic"

    fun LoadConversations(params: String?, callback: KuiklyRenderCallback?) {
        Log.i(TAG, "loadConversation: ")
        ImRuntime.AppCoroutineScope.launch {
            when (val result = ImRuntime.conversationRespository.loadConversation()) {
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


    fun deleteGroupConversation(params: String?, callback: KuiklyRenderCallback?) {
        "deleteGroupConversation".log()
        var id = JSONObject(params ?: "{}").optString(PARAM_ConversationId)

        ImRuntime.AppCoroutineScope.launch {
            when (val result = ImRuntime.conversationRespository.deleteGroupConversation(id)) {
                is ActionResult.Success -> {
                    callback?.invoke(
                        success(data = emptyMap())
                    )
                }

                is ActionResult.Fail -> {
                    callback?.invoke(failure(result.code, result.desc))
                }
            }
        }
    }

    fun deleteC2CConversation(params: String?, callback: KuiklyRenderCallback?) {
        "deleteC2CConversation".log()
        var id = JSONObject(params ?: "{}").optString(PARAM_ConversationId)
        ImRuntime.AppCoroutineScope.launch {
            when (val result = ImRuntime.conversationRespository.deleteC2CConversation(id)) {
                is ActionResult.Success -> {
                    callback?.invoke(success(data = emptyMap()))
                }

                is ActionResult.Fail -> {
                    callback?.invoke(failure(result.code, result.desc))
                }
            }
        }
    }

    fun pinConversation(params: String?, callback: KuiklyRenderCallback?) {
        var id=JSONObject(params ?: "{}").optString(PARAM_ConversationId)
        var pin=JSONObject(params ?: "{}").optBoolean(PARAM_PinConversation)
        "pinConversation".log()
        // 需要将string转换为conversion
        ImRuntime.AppCoroutineScope.launch {
            when (val result = ImRuntime.conversationRespository.pinConversation(id,pin)) {
                is ActionResult.Success -> {
                    callback?.invoke(success(emptyMap()))
                }

                is ActionResult.Fail -> {
                    callback?.invoke(failure(result.code, result.desc))
                }
            }
        }
    }


}


private fun WallyConversation.toBridgeMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "name" to name,
        "avatarUrl" to avatarUrl,
        "unreadMessageCount" to unreadMessageCount,
        "lastMessage" to lastMessage.toBridgeMap(),
        "isPinned" to isPinned,
        "type" to type.toBridgeValue(),
    )
}


