package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.ColorFilter
import com.tencent.kuikly.compose.ui.unit.dp
import com.wally.demo.kuiklywallychat.chat.ui.widgets.ImageButton


/**
 * @author Wally(25054984)
 * @since 2026/7/13
 * @email wanlei@haier.com
 * @desciption 聊天页面下方的图片选择
 * @param onTakePhoto 0 拍照，1 选图， -1 表示取消或者存在问题
 */

@Composable
fun ImageChosePanel(modifier: Modifier, onTakePhoto: () -> Unit, onAlbumClick: () -> Unit) {
//    val TAG = "ImageChosePanel"
//
//
//    // 用于保存拍摄返回的 Uri，以便在界面上展示或其他操作
//    var capturedImageUri by remember { mutableStateOf<String?>(null) }
//
//    // 1. 注册拍照的 Launcher
//    val takePictureLauncher = rememberLauncherForActivityResult(
//        contract = MatisseCaptureContract()
//    ) { result: MediaResource? ->
//        // 3. 处理拍照结果
//        if (result != null) {
//            val uri = result.uri       // 拍照后生成的 Uri
//            val path = result.path     // 文件的绝对路径
//            val name = result.name     // 文件名
//            capturedImageUri = uri.toString()
//            Log.i(TAG, "拍照成功: $name, 路径: $path")
//            onImageChose(0,uri)
//        } else {
//            onImageChose(-1,null)
//        }
//    }
//
//    val mediaPickerLauncher = rememberLauncherForActivityResult(
//        contract = MatisseContract()
//    ) { result: List<MediaResource>? ->
//        // 3. 处理选择回调结果
//        if (!result.isNullOrEmpty()) {
//            result.forEach { mediaResource ->
//                val uri = mediaResource.uri       // 资源的 Uri，用于图片加载或上传
//                val path = mediaResource.path     // 绝对路径
//                val name = mediaResource.name     // 文件名
//                val mimeType = mediaResource.mimeType // 文件类型，如 image/jpeg
//                Log.i(TAG, "选择图片成功: Selected: $name, URI: $uri")
//                onImageChose(1,uri)
//            }
//        }else{
//            onImageChose(-1,null)
//        }
//    }
//
//
//
//
//
//    Box(modifier) {
//        Row(modifier.padding(12.dp)) {
//
//            fun takePhoto() {
//// 2. 构造 MatisseCapture 并启动相机
//                // 推荐使用 MediaStoreCaptureStrategy 或 SmartCaptureStrategy
//                val captureConfig = MatisseCapture(
//                    captureStrategy = MediaStoreCaptureStrategy()
//                )
//                takePictureLauncher.launch(captureConfig)
//            }
//
//
//            fun selectImage() {
//                // 2. 配置 Matisse 参数并启动
//                val matisse = Matisse(
//                    maxSelectable = 1,                          // 最大选择数量
//                    imageEngine = CoilImageEngine(),            // 使用 Coil 加载图片
//                    mediaType = MediaType.ImageOnly,            // 仅选择图片 (也可以选 VideoOnly 或 ImageAndVideo)
//                    gridColumns = 4,                            // 网格显示的列数
//                )
//                mediaPickerLauncher.launch(matisse)
//            }
//
//            Icon(
//                modifier = Modifier
//                    .clickable(onClick = {
//                        takePhoto()
//                    })
//                    .size(32.dp),
//                imageVector = Icons.Default.PhotoCamera,
//                tint = Color(color = 0xFF42A5F5),
//                contentDescription = ""
//            )
//            Spacer(Modifier.width(20.dp))
//            Icon(
//                modifier = Modifier
//                    .clickable(onClick = {
//                        selectImage()
//                    })
//                    .size(32.dp), imageVector = Icons.Default.Image,
//                tint = Color(color = 0xFF42A5F5),
//                contentDescription = ""
//            )
//        }
//    }

    Box(modifier) {
        var tintColor = Color(color = 0xFF42A5F5)
        val colorFilter = ColorFilter.tint(tintColor)
        Row(modifier.padding(12.dp)) {
            ImageButton(
                assetName = "ic_camera",
                colorFilter =colorFilter,
                contentDescription = "",
                onClick = onTakePhoto
            )
            Spacer(Modifier.width(20.dp))
            ImageButton(
                assetName = "ic_album",
                colorFilter =colorFilter,
                contentDescription = "",
                onClick = onAlbumClick
            )
        }
    }
}

