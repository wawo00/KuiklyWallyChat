package com.wally.demo.kuiklywallychat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import com.tencent.kuikly.core.render.android.css.ktx.toMap
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegator
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.adapter.KRColorParserAdapter
import com.wally.demo.kuiklywallychat.adapter.KRFontAdapter
import com.wally.demo.kuiklywallychat.adapter.KRImageAdapter
import com.wally.demo.kuiklywallychat.adapter.KRLogAdapter
import com.wally.demo.kuiklywallychat.adapter.KRRouterAdapter
import com.wally.demo.kuiklywallychat.adapter.KRThreadAdapter
import com.wally.demo.kuiklywallychat.adapter.KRUncaughtExceptionHandlerAdapter
import com.wally.demo.kuiklywallychat.im.ImEventBus
import com.wally.demo.kuiklywallychat.im.ImRuntime
import com.wally.demo.kuiklywallychat.im.bridge.KRImModule
import com.wally.demo.kuiklywallychat.im.mediapicket.AndroidMediaPickerCoordinator
import com.wally.demo.kuiklywallychat.module.KRBridgeModule
import com.wally.demo.kuiklywallychat.module.KRDownloadModule
import com.wally.demo.kuiklywallychat.module.KRMediaPickerModule
import com.wally.demo.kuiklywallychat.module.KRShareModule
import github.leavesczy.compose_chat.utils.AlbumUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

class KuiklyRenderActivity : AppCompatActivity(), KuiklyRenderViewBaseDelegatorDelegate {

    private lateinit var eventBusJob: Job
    private lateinit var hrContainerView: ViewGroup
    private lateinit var loadingView: View
    private lateinit var errorView: View

    private val kuiklyRenderViewDelegator = KuiklyRenderViewBaseDelegator(this)

    private lateinit var mediaPickerCoordinator:
            AndroidMediaPickerCoordinator

    private data class PendingImagePickRequest(
        val source: String,
        val callback: KuiklyRenderCallback?,
    )

    private data class PendingDownloadRequest(
        val source: String,
        val callback: KuiklyRenderCallback?,
    )

    private var pendingImagePickRequest: PendingImagePickRequest? = null
    private var pendingDownloadRequest: PendingDownloadRequest? = null

    private val imagePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            val request = pendingImagePickRequest
                ?: return@registerForActivityResult

            pendingImagePickRequest = null

            if (granted) {
                launchImagePicker(request.source, request.callback)
            } else {
                request.callback?.invoke(
                    mapOf(
                        "status" to "error",
                        "message" to permissionDeniedMessage(request.source),
                    ),
                )
            }
        }

    private val downloadPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val request = pendingDownloadRequest
                ?: return@registerForActivityResult

            pendingDownloadRequest = null

            if (permissions.values.all { it }) {
                Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show()
                launchImageDownload(request.source, request.callback)
            } else {
                Toast.makeText(this, "权限申请失败", Toast.LENGTH_SHORT).show()
                request.callback?.invoke(
                    mapOf(
                        "status" to "error",
                        "message" to "下载所需权限被拒绝",
                    ),
                )
            }
        }

    private val pageName: String
        get() {
            val pn = intent.getStringExtra(KEY_PAGE_NAME) ?: ""
            return if (pn.isNotEmpty()) {
                return pn
            } else {
                "imApp" //首页路由
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_hr)
        setupImmersiveMode()
        hrContainerView = findViewById(R.id.hr_container)
        loadingView = findViewById(R.id.hr_loading)
        errorView = findViewById(R.id.hr_error)
        kuiklyRenderViewDelegator.onAttach(hrContainerView, "", pageName, createPageData())

        //转发android的消息给kuikly
        eventBusJob = lifecycleScope.launch { // implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
            ImRuntime.eventBus.events.collect {event->
                Log.i(
                    "KuiklyRenderActivity",
                    "forward event: " +
                            "pageName=$pageName, " +
                            "event=${event.name}, " +
                            "data=${event.data}",
                )

                kuiklyRenderViewDelegator.sendEvent(
                    event.name,
                    event.data,
                )
            }
        }
        mediaPickerCoordinator =
            AndroidMediaPickerCoordinator(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        eventBusJob.cancel()
        kuiklyRenderViewDelegator.onDetach()
    }

    override fun onPause() {
        super.onPause()
        kuiklyRenderViewDelegator.onPause()
    }

    override fun onResume() {
        super.onResume()
        kuiklyRenderViewDelegator.onResume()
    }

    override fun registerExternalModule(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerExternalModule(kuiklyRenderExport)
        with(kuiklyRenderExport) {
            moduleExport(KRImModule.MODULE_NAME) {
                KRImModule()
            }
            moduleExport(KRBridgeModule.MODULE_NAME) {
                KRBridgeModule()
            }
            moduleExport(KRShareModule.MODULE_NAME) {
                KRShareModule()
            }
            moduleExport(KRMediaPickerModule.MODULE_NAME) {
                KRMediaPickerModule()
            }
            moduleExport(KRDownloadModule.MODULE_NAME) {
                KRDownloadModule()
            }
        }
    }

    override fun registerExternalRenderView(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerExternalRenderView(kuiklyRenderExport)
        with(kuiklyRenderExport) {

        }
    }

    private fun createPageData(): Map<String, Any> {
        val param = argsToMap()
        param["appId"] = 1
        return param
    }

    private fun argsToMap(): MutableMap<String, Any> {
        val jsonStr = intent.getStringExtra(KEY_PAGE_DATA) ?: return mutableMapOf()
        return JSONObject(jsonStr).toMap()
    }

    private fun setupImmersiveMode() {
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = Color.TRANSPARENT
            window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

    }

    companion object {

        private const val KEY_PAGE_NAME = "pageName"
        private const val KEY_PAGE_DATA = "pageData"

        init {
            initKuiklyAdapter()
        }

        fun start(context: Context, pageName: String, pageData: JSONObject) {
            val starter = Intent(context, KuiklyRenderActivity::class.java)
            starter.putExtra(KEY_PAGE_NAME, pageName)
            starter.putExtra(KEY_PAGE_DATA, pageData.toString())
            context.startActivity(starter)
        }

        private fun initKuiklyAdapter() {
            with(KuiklyRenderAdapterManager) {
                krImageAdapter = KRImageAdapter(KRApplication.application)
                krLogAdapter = KRLogAdapter
                krUncaughtExceptionHandlerAdapter = KRUncaughtExceptionHandlerAdapter
                krFontAdapter = KRFontAdapter
                krColorParseAdapter = KRColorParserAdapter(KRApplication.application)
                krRouterAdapter = KRRouterAdapter
                krThreadAdapter = KRThreadAdapter()
            }
        }
    }
    fun pickImage(
        source: String,
        callback: KuiklyRenderCallback?,
    ) {
        val permission = requiredImagePermission(source)

        if (permission == null) {
            launchImagePicker(source, callback)
            return
        }

        if (
            ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            launchImagePicker(source, callback)
            return
        }

        if (pendingImagePickRequest != null) {
            callback?.invoke(
                mapOf(
                    "status" to "error",
                    "message" to "已有权限请求正在处理中",
                ),
            )
            return
        }

        pendingImagePickRequest = PendingImagePickRequest(
            source = source,
            callback = callback,
        )
        imagePermissionLauncher.launch(permission)
    }

    private fun requiredImagePermission(source: String): String? =
        when (source) {
            "camera" -> Manifest.permission.CAMERA
            "album" -> {
                // Keep this condition aligned with Matisse 2.2.0. On Android 13+,
                // apps targeting below API 33 still use READ_EXTERNAL_STORAGE.
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    applicationInfo.targetSdkVersion >= Build.VERSION_CODES.TIRAMISU
                ) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            }

            else -> null
        }

    private fun permissionDeniedMessage(source: String): String =
        when (source) {
            "camera" -> "相机权限被拒绝，无法拍照"
            "album" -> "相册访问权限被拒绝，无法选择图片"
            else -> "权限请求被拒绝"
        }

    private fun launchImagePicker(
        source: String,
        callback: KuiklyRenderCallback?,
    ) {
        mediaPickerCoordinator.pickImage(
            source = source,
            callback = callback,
        )
    }

    fun downloadImage(source: String, callback: KuiklyRenderCallback?) {
        val permissions = requiredDownloadPermissions()

        if (permissions.isEmpty()) {
            launchImageDownload(source, callback)
            return
        }

        if (permissions.all { permission ->
                ContextCompat.checkSelfPermission(this, permission) ==
                        PackageManager.PERMISSION_GRANTED
            }
        ) {
            launchImageDownload(source, callback)
            return
        }

        if (pendingDownloadRequest != null) {
            Toast.makeText(this, "已有下载权限请求正在处理中", Toast.LENGTH_SHORT).show()
            callback?.invoke(
                mapOf(
                    "status" to "error",
                    "message" to "已有下载权限请求正在处理中",
                ),
            )
            return
        }

        pendingDownloadRequest = PendingDownloadRequest(source, callback)
        Toast.makeText(this, "正在申请保存图片所需权限", Toast.LENGTH_SHORT).show()
        downloadPermissionLauncher.launch(permissions)
    }

    private fun requiredDownloadPermissions(): Array<String> =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)

            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ->
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            else -> emptyArray()
        }

    private fun launchImageDownload(
        source: String,
        callback: KuiklyRenderCallback?,
    ) {

        lifecycleScope.launch {
            val result = AlbumUtils.insertImageToAlbum(
                context = this@KuiklyRenderActivity,
                imageUri = source
            )
            if (result) {
                Toast.makeText(this@KuiklyRenderActivity, "图片保存成功", Toast.LENGTH_SHORT).show()
                callback?.invoke(
                    mapOf(
                        "status" to "success",
                    ),
                )
            } else {
                Toast.makeText(this@KuiklyRenderActivity, "失败了", Toast.LENGTH_SHORT).show()

                callback?.invoke(
                    mapOf(
                        "status" to "error",
                        "message" to "图片保存失败",
                    ),
                )
            }
        }

//        mediaPickerCoordinator.downloadImage(source) { result ->
//            val message = result["message"] as? String
//            val status = result["status"] as? String
//            if (status == "success") {
//                Toast.makeText(this, "图片保存成功", Toast.LENGTH_SHORT).show()
//            } else if (status == "error") {
//                Toast.makeText(
//                    this,
//                    message ?: "图片保存失败",
//                    Toast.LENGTH_SHORT,
//                ).show()
//            }
//            callback?.invoke(result)
//        }
    }
}
