package com.wally.demo.kuiklywallychat.im.mediapicket

import android.content.Context
import github.leavesczy.matisse.MediaResource
import java.io.File

/**
 * @author Wally(25054984)
 * @since 2026/8/6
 * @email wanlei@haier.com
 * @desciption  文件准备器
 * @tips 这里没有对图片进行压缩
 */
class AndroidImageFilePreparer(
    private val context: Context,
) {
    fun prepare(resource: MediaResource): Result<String> {
        return runCatching {
            val existingPath =
                resource.path?.takeIf { path ->
                    path.isNotBlank() &&
                            File(path).isFile
                }

            if (existingPath != null) {
                return@runCatching existingPath // Result.Success<String>
            }

            val targetFile = File(context.cacheDir, "chat_img_${System.currentTimeMillis()}.png")

            context.contentResolver.openInputStream(resource.uri)
                .use { input ->
                    requireNotNull(input) {
                        "无法读取图片"
                    }

                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            targetFile.absolutePath
        }

    }
}