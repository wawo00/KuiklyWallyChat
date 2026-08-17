package com.wally.demo.kuiklywallychat.chat.im

import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_DOWNLOAD_URL
import com.wally.demo.kuiklywallychat.chat.im.MediaPickerModule.Companion.PICK_IMAGE

/**
 * @author Wally(25054984)
 * @since 2026/8/14
 * @email wanlei@haier.com
 * @desciption 用于图片下载
 */
class DownloadModule : Module(), DownLoadGateway {
    override fun moduleName(): String {
        return MODULE_NAME
    }

    override fun downloadImage(imageUrl: String, callback: (DownloadResult) -> Unit) {
        val params = JSONObject().apply {
            put(PARAM_DOWNLOAD_URL,imageUrl)
        }

        callNativeMethod(
            DOWNLOAD_IMAGE,
            params,
        ) { result ->
            when (result?.optString("status")) {
                "success" -> {
//                    val localPath = result.optString("localPath")

//                    if (localPath.isBlank()) {
//                        callback(DownloadResult.Failure("原生层没有返回图片路径"))
//                    } else {
                        callback(DownloadResult.Success(""))
//                    }
                }

                "cancelled" -> {
                    callback(DownloadResult.Cancelled)
                }

                else -> {
                    callback(
                        DownloadResult.Failure(
                            result
                                ?.optString("message")
                                .orEmpty()
                                .ifBlank {
                                    "图片选择失败"
                                },
                        ),
                    )
                }
            }
        }
    }
    companion object {
        const val MODULE_NAME =
            "KRDownloadModule"

        const val DOWNLOAD_IMAGE =
            "downloadImage"
    }

    private fun callNativeMethod(methodName: String, data: JSONObject?, callbackFn: CallbackFn?) {
        toNative(
            false,
            methodName,
            data?.toString(),
            callbackFn,
            false
        )
    }

}