package com.wally.demo.kuiklywallychat.chat.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.chat.im.AccountGateway
import com.wally.demo.kuiklywallychat.chat.im.ImResult

/**
  * @author Wally(25054984)
  * @since 2026/7/27
  * @email wanlei@haier.com
  * @desciption 之前的viewmodel
  */
class LoginController(
    private val imGateway: AccountGateway,
) {
    private val TAG="LoginController"
    var state: LoginState by mutableStateOf(LoginState())
        private set

    fun start() {
        imGateway.restoreSession { result ->
            Utils.logToNative("调用了imGateway.restoreSession")

            when (result) {
                is ImResult.Success -> {
                    val session = result.data
                    state = state.copy(
                        userId = session.userId,
                        showLoginForm = !session.canAutoLogin,
                        errorMessage = null,
                    )
                    if (session.canAutoLogin && session.userId.isNotBlank()) {
                        login()
                    }
                }

                is ImResult.Failure -> {
                    state = state.copy(
                        showLoginForm = true,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    fun onUserIdChanged(value: String) {
        if (!state.isLoading && value.length <= 32) {
            state = state.copy(
                userId = value,
                errorMessage = null,
            )
        }
    }

    fun login() {
        if (state.isLoading) return

        val userId = state.userId.trim()
        if (userId.isEmpty()) {
            state = state.copy(errorMessage = "请输入 UserId")
            return
        }

        state = state.copy(
            userId = userId,
            isLoading = true,
            errorMessage = null,
        )

        imGateway.login(userId) { result ->
            state = when (result) {
                is ImResult.Success -> state.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    showLoginForm = false,
                )

                is ImResult.Failure -> state.copy(
                    isLoading = false,
                    isLoggedIn = false,
                    showLoginForm = true,
                    errorMessage = "${result.code}: ${result.message}",
                )
            }
        }
    }

    fun onSessionInvalid(message: String) {
        state = state.copy(
            isLoading = false,
            isLoggedIn = false,
            showLoginForm = true,
            errorMessage = message,
        )
    }

    fun onConnectionChanged(optString: String?) {
        Utils.logToNative("onConnectionChanged  :${optString}")
        Utils.toast(" connect changed :${optString}")

    }
}