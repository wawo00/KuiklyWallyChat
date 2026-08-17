package com.wally.demo.kuiklywallychat.adapter

import com.wally.demo.kuiklywallychat.KRApplication
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tencent.kuikly.core.render.android.KuiklyRenderViewContext
import com.tencent.kuikly.core.render.android.adapter.HRImageLoadOption
import com.tencent.kuikly.core.render.android.adapter.IKRImageAdapter
import com.wally.demo.kuiklywallychat.adapter.intoDrawableTarget
import com.wally.demo.kuiklywallychat.im.ImRuntime.AppCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class KRImageAdapter(val context: Context) : IKRImageAdapter {

//    override fun fetchDrawable(
//        imageLoadOption: HRImageLoadOption,
//        callback: (drawable: Drawable?) -> Unit,
//    ) {
//        if (imageLoadOption.isBase64()) {
//            loadFromBase64(imageLoadOption, callback)
//        } else if (imageLoadOption.isWebUrl() || imageLoadOption.isAssets() || imageLoadOption.isFile()) {
//            // http/assets/file 图片使用 glide 加载
//            requestImage(imageLoadOption, callback)
//        }
//    }

//
//    private val imageDecodeExecutor: ExecutorService= Executors.newSingleThreadExecutor{ runnable ->
//        Thread(runnable,"Kuikly-AnimatedWebpDecoder")
//            .apply {
//                isDaemon=true
//            }
//    }

//    private  val mainHandler=Handler(Looper.getMainLooper())


    override fun fetchDrawable(
        imageLoadOption: HRImageLoadOption,
        callback: (drawable: Drawable?) -> Unit,
    ) {
        when {
            imageLoadOption.isBase64() -> {
                loadFromBase64(
                    imageLoadOption,
                    callback,
                )
            }

            imageLoadOption.isWebUrl() ||
                    imageLoadOption.isAssets() ||
                    imageLoadOption.isFile() ||
                    imageLoadOption.src.startsWith("/") -> { //改造1.满足接受类似/storage/emulated/0/xxx.jpg的路径
                requestImage(
                    imageLoadOption,
                    callback,
                )
            }

            else -> {
                /*
                 * 无法识别的地址也必须回调，否则上层可能一直处于等待状态。
                 */
                callback(null)
            }
        }
    }

    override fun getDrawableWidth(
        kuiklyRenderViewContext: KuiklyRenderViewContext,
        drawable: Drawable,
    ): Float {
        return drawable.intrinsicWidth.toFloat()
    }

    override fun getDrawableHeight(
        kuiklyRenderViewContext: KuiklyRenderViewContext,
        drawable: Drawable,
    ): Float {
        return drawable.intrinsicHeight.toFloat()
    }

    private fun requestImage(
        imageLoadOption: HRImageLoadOption,
        callback: (drawable: Drawable?) -> Unit,
    ) {
        val src = if (imageLoadOption.isAssets()) {
            val assetPath = imageLoadOption.src
                .substring(HRImageLoadOption.SCHEME_ASSETS.length)

            "file:///android_asset/$assetPath"
        } else {
            imageLoadOption.src
        }

        when {
            // Animated WebP：Android 9+ 使用系统 ImageDecoder。
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                    isAnimatedWebpCandidate(src) -> {
                requestAnimatedWebp(src, callback)
            }

            // 原有 GIF 处理逻辑保留。
            src.substringBefore('?').lowercase().endsWith(".gif") -> {
                Glide.with(KRApplication.application)
                    .asGif()
                    .load(src)
                    .intoDrawableTarget(src, callback)
            }

            // 普通静态图。
            else -> {
                Glide.with(KRApplication.application)
                    .asDrawable()
                    .load(src)
                    .intoDrawableTarget(src, callback)
            }
        }
    }

    //Animated WebP 的专用下载和解码：
    private fun requestAnimatedWebp(
        src: String,
        callback: (Drawable?) -> Unit,
    ) {
        Glide.with(KRApplication.application)
            .downloadOnly()
            .load(src)
            .into(object : CustomTarget<File>() {

                override fun onLoadCleared(placeholder: Drawable?) {
                    callback(null)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.e("KRImageAdapter", "Animated WebP 下载失败：$src")
                    callback(null)
                }

                override fun onResourceReady(
                    file: File,
                    transition: Transition<in File>?,
                ) {
                    // ImageDecoder 不应在主线程做解码。

                    AppCoroutineScope.launch {
                        var result = runCatching {
                            ImageDecoder.decodeDrawable(
                                ImageDecoder.createSource(file),
                            )
                        }
                        withContext(Dispatchers.Main.immediate){
                            result.onSuccess { drawable->

//                                Log.d(
//                                    "KRImageAdapter",
//                                    "Animated WebP 解码成功：" +
//                                            "url=$src, " +
//                                            "drawable=${drawable::class.java.name}, " +
//                                            "animatable=${drawable is Animatable}",
//                                )
                                callback(drawable)

                            }.onFailure { error->
                                Log.e(
                                    "KRImageAdapter",
                                    "Animated WebP 解码失败：$src",
                                    error,
                                )
                                callback(null)

                            }
                        }

                    }
                }
            })
    }

    private fun isAnimatedWebpCandidate(url: String): Boolean {
        val normalizedUrl = url
            .substringBefore('?')
            .substringBefore('#')
            .lowercase()

        return normalizedUrl.endsWith(".awebp") ||
                normalizedUrl.endsWith(".webp")
    }

    private fun loadFromBase64(
        imageLoadOption: HRImageLoadOption,
        callback: (drawable: Drawable?) -> Unit,
    ) {
        execOnSubThread {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val bytes = Base64.decode(imageLoadOption.src.split(",")[1], Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            try {
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                options.inJustDecodeBounds = false
                try {
                    options.inSampleSize = calculateInSampleSize(
                        options,
                        imageLoadOption.requestWidth,
                        imageLoadOption.requestHeight
                    )
                } catch (e: ArithmeticException) { // 偶现报除以0，可能是inSampleSize超过int的范围溢出了。这里catch兜底使用原始inSampleSize
                    Log.d("ECHRImageAdapter", "loadFromBase64: $e")
                }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                callback.invoke(BitmapDrawable(Resources.getSystem(), bitmap))
            } catch (e: OutOfMemoryError) {
                Log.d("ECHRImageAdapter", "oom happen: $e")
            }
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        return if (reqWidth != 0 && reqHeight != 0 && reqWidth != -1 && reqHeight != -1) {
            var height = options.outHeight
            var width = options.outWidth
            var inSampleSize: Int
            inSampleSize = 1
            while (height > reqHeight && width > reqWidth) {
                val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
                val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
                val ratio = if (heightRatio > widthRatio) heightRatio else widthRatio
                if (ratio < 2) {
                    break
                }
                width = width shr 1
                height = height shr 1
                inSampleSize = inSampleSize shl 1
            }
            inSampleSize
        } else {
            1
        }
    }

}

fun <T: Drawable>RequestBuilder<T>.intoDrawableTarget(
    src: String,
    callback: (Drawable?) -> Unit,
) {
    into(object : CustomTarget<T>() {
        override fun onLoadCleared(placeholder: Drawable?) {
            callback(null)
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            Log.e("KRImageAdapter", "图片加载失败：$src")
            callback(null)
        }

        override fun onResourceReady(
            resource: T,
            transition: Transition<in T>?,
        ) {
//            Log.d(
//                "KRImageAdapter",
//                "图片加载成功：url=$src, " +
//                        "drawable=${resource::class.java.name}, " +
//                        "animatable=${resource is Animatable}",
//            )
            callback(resource)
        }
    })
}