package com.wally.demo.kuiklywallychat.chat.im

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
  * @author Wally(25054984)
  * @since 2026/7/27
  * @email wanlei@haier.com
  * @desciption  交互通用数据结构
  */
data class RestoredSession(
    val userId: String,
    val canAutoLogin: Boolean,
)

sealed class ImResult<out T> {

    data class Success<T>(
        val data: T,
    ) : ImResult<T>()

    data class Failure(
        val code: Int,
        val message: String,
    ) : ImResult<Nothing>()
}

internal inline fun <T> JSONObject.toImResult(
    decode: (JSONObject) -> T,
): ImResult<T> {
    val success = optBoolean("success")
    val code = optInt("code")
    val message = optString("message")

    return if (success) {
        ImResult.Success(
            data = decode(optJSONObject("data") ?: JSONObject()),
        )
    } else {
        ImResult.Failure(
            code = code,
            message = message,
        )
    }
}