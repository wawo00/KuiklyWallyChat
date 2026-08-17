package com.wally.demo.kuiklywallychat.im.mediapicket

import androidx.appcompat.app.AppCompatActivity
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import github.leavesczy.matisse.CoilImageEngine
import github.leavesczy.matisse.Matisse
import github.leavesczy.matisse.MatisseCapture
import github.leavesczy.matisse.MatisseCaptureContract
import github.leavesczy.matisse.MatisseContract
import github.leavesczy.matisse.MediaResource
import github.leavesczy.matisse.MediaStoreCaptureStrategy
import github.leavesczy.matisse.MediaType

class AndroidMediaPickerCoordinator(
    private val activity: AppCompatActivity,
) {

    private var imageFilePreparer= AndroidImageFilePreparer(activity)
    private var pendingCallback:
            KuiklyRenderCallback? = null

    private val captureLauncher =
        activity.registerForActivityResult(
            MatisseCaptureContract(),
        ) { resource ->
            if (resource == null) {
                completeCancelled()
            } else {
                completeWithResource(resource)
            }
        }

    private val albumLauncher =
        activity.registerForActivityResult(
            MatisseContract(),
        ) { resources ->
            val resource =
                resources?.firstOrNull()

            if (resource == null) {
                completeCancelled()
            } else {
                completeWithResource(resource)
            }
        }

    fun pickImage(
        source: String,
        callback: KuiklyRenderCallback?,
    ) {
        if (pendingCallback != null) {
            callback?.invoke(
                mapOf(
                    "status" to "error",
                    "message" to "已有图片选择请求正在执行",
                ),
            )
            return
        }

        pendingCallback = callback

        when (source) {
            "camera" -> {
                captureLauncher.launch(
                    MatisseCapture(
                        captureStrategy =
                            MediaStoreCaptureStrategy(),
                    ),
                )
            }

            "album" -> {
                albumLauncher.launch(
                    Matisse(
                        maxSelectable = 1,
                        imageEngine =
                            CoilImageEngine(),
                        mediaType =
                            MediaType.ImageOnly,
                        gridColumns = 4,
                    ),
                )
            }

            else -> {
                completeFailure(
                    "不支持的图片来源：$source",
                )
            }
        }
    }

    private fun completeWithResource(
        resource: MediaResource,
    ) {
        imageFilePreparer
            .prepare(resource)
            .onSuccess { localPath ->
                pendingCallback?.invoke(
                    mapOf(
                        "status" to "success",
                        "localPath" to localPath,
                    ),
                )
                pendingCallback = null
            }
            .onFailure { throwable ->
                completeFailure(
                    throwable.message
                        ?: "图片处理失败",
                )
            }
    }

    private fun completeCancelled() {
        pendingCallback?.invoke(
            mapOf(
                "status" to "cancelled",
            ),
        )
        pendingCallback = null
    }

    private fun completeFailure(
        message: String,
    ) {
        pendingCallback?.invoke(
            mapOf(
                "status" to "error",
                "message" to message,
            ),
        )
        pendingCallback = null
    }

}