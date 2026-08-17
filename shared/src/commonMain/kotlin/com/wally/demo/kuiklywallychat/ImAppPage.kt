package com.wally.demo.kuiklywallychat

import androidx.compose.runtime.LaunchedEffect
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.module.CallbackRef
import com.tencent.kuikly.core.module.NotifyModule
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.base.BasePager
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.chat.im.ImEvent
import com.wally.demo.kuiklywallychat.chat.im.ImModule
import com.wally.demo.kuiklywallychat.chat.ui.login.LoginController
import com.wally.demo.kuiklywallychat.chat.ui.login.LoginScreen

@Page("imApp", supportInLocal = true)
class ImAppPage : BasePager() { //继承BasePager才能收到

    private lateinit var loginController: LoginController
    private val notifyRefs = mutableListOf<Pair<String, CallbackRef>>()
    override fun willInit() {
        super.willInit()

        setContent {
            val state = loginController.state
            if (state.isLoggedIn) {
                Utils.logToNative("自动登录了")
                LaunchedEffect(state.isLoggedIn){
                        acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(
                            pageName = "MainPage",
                            pageData = JSONObject().apply {
                                put("userId",state.userId)
                            }
                        ).apply { closeCurrentPage() }
                }

            } else if (state.showLoginForm) {
                LoginScreen(
                    state = state,
                    onUserIdChanged = loginController::onUserIdChanged,
                    onLogin = loginController::login,
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }

    override fun created() {
        super.created()
        val gateway = acquireModule<ImModule>(ImModule.MODULE_NAME)
        loginController = LoginController(gateway)
//        subscribeImEvents() 这样实现收用不到通知
        loginController.start() //todo:实际编译时若当前 Kuikly 生命周期要求 acquireModule 在 created() 后调用，则在 created() 中创建 Controller
    }

    override fun pageWillDestroy() {
//        unsubscribeImEvents()
        super.pageWillDestroy()
    }


    private fun subscribeImEvents() {
        val notifyModule = acquireModule<NotifyModule>(NotifyModule.MODULE_NAME)
        Utils.logToNative(
            "imapppage subscribeImEvents start",
        )
        fun listen(eventName: String, handler: (JSONObject?) -> Unit) {
            val callbackRef = notifyModule.addNotify(eventName) { eventData ->
                handler(eventData)
            }
            notifyRefs += eventName to callbackRef
        }

        listen(ImEvent.UserSigExpired) {
            loginController.onSessionInvalid("UserSig 已过期，请重新登录")
        }

        listen(ImEvent.KickedOffline) {
            loginController.onSessionInvalid("账号已在其他设备登录")
        }

        listen(ImEvent.ConnectionChanged) { eventData ->
//            loginController.onConnectionChanged(eventData?.optString("state"))
        }
    }

    private fun unsubscribeImEvents() {
        val notifyModule = acquireModule<NotifyModule>(NotifyModule.MODULE_NAME)

        notifyRefs.forEach { (eventName, callbackRef) ->
            notifyModule.removeNotify(eventName, callbackRef)
        }
        notifyRefs.clear()
    }
}
