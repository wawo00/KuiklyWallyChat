package com.wally.demo.kuiklywallychat.chat.im

import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
  * @author Wally(25054984)
  * @since 2026/8/6
  * @email wanlei@haier.com
  * @desciption 模仿bridgemodule 实现的专用于图片选择的连接层
  */
class MediaPickerModule : Module(), MediaPickerGateway{
    override fun moduleName(): String {
        return MODULE_NAME
    }


    override fun pickImage(
        source: ImagePickSource,
        callback: (ImagePickResult) -> Unit,
    ) {
        val params = JSONObject().apply {
            put(
                "source",
                when (source) {
                    ImagePickSource.Camera -> "camera"
                    ImagePickSource.Album -> "album"
                },
            )
        }

        callNativeMethod(
            PICK_IMAGE,
            params,
        ) { result ->
            when (result?.optString("status")) {
                "success" -> {
                    val localPath =
                        result.optString("localPath")

                    if (localPath.isBlank()) {
                        callback(
                            ImagePickResult.Failure(
                                "原生层没有返回图片路径",
                            ),
                        )
                    } else {
                        callback(
                            ImagePickResult.Success(
                                localPath,
                            ),
                        )
                    }
                }

                "cancelled" -> {
                    callback(ImagePickResult.Cancelled)
                }

                else -> {
                    callback(
                        ImagePickResult.Failure(
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
            "KRMediaPickerModule"

         const val PICK_IMAGE =
            "pickImage"
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