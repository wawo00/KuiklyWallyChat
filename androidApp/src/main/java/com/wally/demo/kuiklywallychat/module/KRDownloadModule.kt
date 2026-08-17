package com.wally.demo.kuiklywallychat.module

import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.KuiklyRenderActivity
import com.wally.demo.kuiklywallychat.chat.im.DownloadModule.Companion.DOWNLOAD_IMAGE
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_DOWNLOAD_URL
import com.wally.demo.kuiklywallychat.chat.im.MediaPickerModule.Companion.PICK_IMAGE
import org.json.JSONObject

/**
 * @author Wally(25054984)
 * @since 2026/8/14
 * @email wanlei@haier.com
 * @desciption 用于下载资源操作
 * @tip 主要是因为这个basemodule里面有activity成员变量
 */
class KRDownloadModule :
    KuiklyRenderBaseModule() {
    override fun call(
        method: String,
        params: String?,
        callback: KuiklyRenderCallback?,
    ): Any? {
        if (method == DOWNLOAD_IMAGE) {
            val source = JSONObject(params ?: "{}").optString(PARAM_DOWNLOAD_URL)

            val hostActivity = activity as? KuiklyRenderActivity

            if (hostActivity == null) {
                callback?.invoke(
                    mapOf(
                        "status" to "error",
                        "message" to "不支持下载",
                    ),
                )
                return null
            }

            hostActivity.downloadImage(
                source = source,
                callback = callback,
            )

        }
        return null

    }

    companion object {
        const val MODULE_NAME =
            "KRDownloadModule"
    }
}