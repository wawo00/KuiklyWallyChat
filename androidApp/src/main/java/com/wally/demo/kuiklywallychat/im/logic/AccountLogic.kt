package com.wally.demo.kuiklywallychat.im.logic

import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeMap
import com.wally.demo.kuiklywallychat.im.ImRuntime
import com.wally.demo.kuiklywallychat.im.account.NativeResult
import kotlinx.coroutines.launch


object AccountLogic : BaseLogic() {

    fun getSelfProfile(
        params:String?,
        callback: KuiklyRenderCallback?,
    ) {
        ImRuntime.AppCoroutineScope.launch {
            when (
                val result =
                    ImRuntime.accountRepository.getSelfProfile()
            ) {
                is NativeResult.Success -> {
                    callback?.invoke(
                        success(
                            mapOf(
                                "profile" to result.data.toBridgeMap(),
                            ),
                        ),
                    )
                }

                is NativeResult.Failure -> {
                    callback?.invoke(
                        failure(
                            code = result.code,
                            message = result.message,
                        ),
                    )
                }
            }
        }
    }
}