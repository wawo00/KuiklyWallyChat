package com.wally.demo.kuiklywallychat.im.logic

import android.util.Log
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_UserId
import com.wally.demo.kuiklywallychat.im.ImRuntime
import com.wally.demo.kuiklywallychat.im.account.NativeResult
import kotlinx.coroutines.launch
import org.json.JSONObject

object LoginLogic: BaseLogic() {
    private  val  TAG="LoginLogic"
     fun restoreSession(callback: KuiklyRenderCallback?) {
         Log.i(TAG, "restoreSession: ")
        val session = ImRuntime.accountRepository.restoreSession()
        callback?.invoke(
            success(
                mapOf(
                    "userId" to session.userId,
                    "canAutoLogin" to session.canAutoLogin,
                ),
            ),
        )
    }

     fun login(params: String?, callback: KuiklyRenderCallback?) {
        val userId = JSONObject(params ?: "{}").optString("userId").trim()
        if (userId.isEmpty()) {
            callback?.invoke(failure(-1, "UserId 不能为空"))
            return
        }

        ImRuntime.AppCoroutineScope.launch {
            when (val result = ImRuntime.accountRepository.login(userId)) {
                is NativeResult.Success<Unit> -> callback?.invoke(success(emptyMap()))
                is NativeResult.Failure -> callback?.invoke(
                    failure(result.code, result.message),
                )
            }
        }
    }

     fun logout(callback: KuiklyRenderCallback?) {
        ImRuntime.AppCoroutineScope.launch {
            when (val result = ImRuntime.accountRepository.logout()) {
                is NativeResult.Success<String> -> {callback?.invoke(success( mapOf(PARAM_UserId to result.data)))}
                is NativeResult.Failure -> callback?.invoke(
                    failure(result.code, result.message),
                )
            }
        }
    }
}