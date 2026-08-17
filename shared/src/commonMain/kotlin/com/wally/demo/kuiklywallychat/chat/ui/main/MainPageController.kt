package com.wally.demo.kuiklywallychat.chat.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.chat.base.BaseController
import com.wally.demo.kuiklywallychat.chat.base.PageNavigator
import com.wally.demo.kuiklywallychat.chat.base.PageNavigatorData
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.im.AccountGateway
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_UserId
import com.wally.demo.kuiklywallychat.chat.im.ImResult
import com.wally.demo.kuiklywallychat.ext.Toast
import com.wally.demo.timsdk.ui.main.logic.MainPageDrawerViewState
import kotlinx.coroutines.NonCancellable.isActive

class MainPageController(navigator: PageNavigator,private val gateway: AccountGateway) : BaseController(navigator) {

    var state by mutableStateOf(MainPageDrawerViewState(personProfile = PersonProfile.Empty))
        private set


    override fun start() {
        super.start()
        loadSelfProfile()
    }


    fun onSelfProfileChanged(
        profile: PersonProfile,
    ) {
        if (!isActive) {
            return
        }

        state = state.copy(
            personProfile = profile,
            isProfileLoading = false,
            profileErrorMessage = null,
        )
    }

    private fun loadSelfProfile() {

        state = state.copy(
            isProfileLoading = true,
            profileErrorMessage = null,
        )

        gateway.getSelfProfile { result ->
            if (!isActive) {
                return@getSelfProfile
            }



            when (result) {
                is ImResult.Success -> {
                    state = state.copy(
                        personProfile = result.data,
                        isProfileLoading = false,
                        profileErrorMessage = null,
                    )
                }

                is ImResult.Failure -> {
                    state = state.copy(
                        isProfileLoading = false,
                        profileErrorMessage = result.message
                            .ifBlank { "加载用户资料失败" },
                    )
                }
            }
        }
    }

    fun changeTheme() {}
    fun logout() {
        gateway.logout { userId->
            var navigatorPageDate= PageNavigatorData(
                pageName = "imApp",
                pageData =  JSONObject().apply {
                    put(PARAM_UserId, userId)
                },
                closeCurrentPage = true
            )
            goToPage(navigatorPageDate)
        }

    }


    fun onConnectionChanged(optString: String?) {
        Utils.logToNative("onConnectionChanged : ${optString}")
    }

    fun onSessionInvalid(string: String) {
//        Utils.logToNative("onSessionInvalid !")
        //跳转到登录页
        logout()
    }
}