package com.wally.demo.kuiklywallychat.module

import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.KuiklyRenderActivity
import com.wally.demo.kuiklywallychat.chat.im.MediaPickerModule.Companion.PICK_IMAGE
import org.json.JSONObject

/**
 * @author Wally(25054984)
 * @since 2026/8/6
 * @email wanlei@haier.com
 * @desciption 用于跟KRBridgeModule区分,作用也是一样
 * @tip 主要是因为这个basemodule里面有activity成员变量
 */
class KRMediaPickerModule :
    KuiklyRenderBaseModule() {
    override fun call(
        method: String,
        params: String?,
        callback: KuiklyRenderCallback?,
    ): Any? {
        if (method == PICK_IMAGE) {
            val source =
                JSONObject(params ?: "{}")
                    .optString("source")

            val hostActivity =
                activity as? KuiklyRenderActivity

            if (hostActivity == null) {
                callback?.invoke(
                    mapOf(
                        "status" to "error",
                        "message" to "当前页面不支持图片选择",
                    ),
                )
                return null
            }

            hostActivity.pickImage(
                source = source,
                callback = callback,
            )

        }
        return null

    }

    companion object {
        const val MODULE_NAME =
            "KRMediaPickerModule"
    }
}