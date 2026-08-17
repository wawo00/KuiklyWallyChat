package com.wally.demo.kuiklywallychat.im.account

import android.app.Application
import android.util.Log
import com.tencent.imsdk.v2.V2TIMCallback
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMSDKConfig
import com.tencent.imsdk.v2.V2TIMSDKListener
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeMap
import com.wally.demo.kuiklywallychat.chat.im.ImEvent.ConnectionChanged
import com.wally.demo.kuiklywallychat.chat.im.ImEvent.UserSigExpired
import com.wally.demo.kuiklywallychat.im.ImEventBus
import com.wally.demo.kuiklywallychat.im.auth.UserSigProvider
import com.wally.demo.kuiklywallychat.im.storage.LoginPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume


class ImAccountRepository(
    private val application: Application,
    private val sdkAppId: Int,
    private val userSigProvider: UserSigProvider,
    private val eventBus: ImEventBus,
) {
    private var initialized = false
    private var TAG = "ImAccountRepository"
    var loginPreferences = LoginPreferences

    var currentUserId="" //当前登录的用户id
    fun initialize() {
        if (initialized) return

        val config = V2TIMSDKConfig().apply {
            logLevel = V2TIMSDKConfig.V2TIM_LOG_INFO
        }

        V2TIMManager.getInstance().addIMSDKListener(
            object : V2TIMSDKListener() {
                override fun onConnecting() {
                    Log.i(TAG, "onConnecting: ")
                    eventBus.emit(ConnectionChanged, mapOf("state" to "connecting"))
                }

                override fun onConnectSuccess() {
                    Log.i(TAG, "onConnectSuccess: ")
                    eventBus.emit(ConnectionChanged, mapOf("state" to "connected"))
                }

                override fun onConnectFailed(code: Int, error: String) {
                    Log.i(TAG, "onConnectFailed: ")
                    eventBus.emit(
                        ConnectionChanged,
                        mapOf("state" to "connecteFailed", "code" to code, "message" to error),
                    )
                }

                override fun onUserSigExpired() {
                    loginPreferences.disableAutoLogin()
                    eventBus.emit(UserSigExpired, emptyMap())
                }

                override fun onKickedOffline() {
                    loginPreferences.disableAutoLogin()
                    eventBus.emit("im.kickedOffline", emptyMap())
                }
                override fun onSelfInfoUpdated(info: V2TIMUserFullInfo) {
                    val profile = info.toPersonProfile()

                    eventBus.emit(
                        name = "im.selfProfile.changed",
                        data = mapOf(
                            "profile" to profile.toBridgeMap(),
                        ),
                    )
                }
            },
        )

        val success = V2TIMManager.getInstance().initSDK(
            application,
            sdkAppId,
            config,
        )
        check(success) { "腾讯 IM SDK 初始化失败" }
        initialized = true
    }

    fun restoreSession(): RestoredSessionData {
        return RestoredSessionData(
            userId = loginPreferences.lastLoginUserId,
            canAutoLogin = loginPreferences.canAutoLogin,
        )
    }

    suspend fun login(userId: String): NativeResult<Unit> {
        initialize()

        val normalizedUserId = userId.trim()
        if (normalizedUserId.isEmpty()) {
            return NativeResult.Failure(-1, "UserId 不能为空")
        }

        val userSig = userSigProvider.getUserSig(normalizedUserId)
            .getOrElse { return NativeResult.Failure(-2, it.message ?: "获取 UserSig 失败") }
        currentUserId=userId
        val result = suspendCancellableCoroutine<NativeResult<Unit>> { continuation ->
            V2TIMManager.getInstance().login(
                normalizedUserId,
                userSig,
                object : V2TIMCallback {
                    override fun onSuccess() {
                        if (continuation.isActive) {
                            continuation.resume(NativeResult.Success(data = Unit))
                        }
                    }

                    override fun onError(code: Int, message: String?) {
                        if (continuation.isActive) {
                            continuation.resume(
                                NativeResult.Failure(code, message.orEmpty()),
                            )
                        }
                    }
                },
            )
        }

        if (result is NativeResult.Success) {
            Log.i(TAG, "login success: ")

            loginPreferences.onLoginSuccess(normalizedUserId)
            eventBus.emit(
                "im.loginState.changed",
                mapOf("loggedIn" to true, "userId" to normalizedUserId),
            )
        }
        return result
    }

    suspend fun logout(): NativeResult<String> {
        val result = suspendCancellableCoroutine<NativeResult<String>> { continuation ->
            V2TIMManager.getInstance().logout(object : V2TIMCallback {
                override fun onSuccess() {
                    if (continuation.isActive) continuation.resume(NativeResult.Success(data = currentUserId))
                }

                override fun onError(code: Int, message: String?) {
                    if (continuation.isActive) {
                        continuation.resume(NativeResult.Failure(code, message.orEmpty()))
                    }
                }
            })
        }

        if (result is NativeResult.Success) {
            loginPreferences.onLogoutSuccess()
            eventBus.emit("im.loginState.changed", mapOf("loggedIn" to false))
        }
        return result
    }
    suspend fun getSelfProfile(): NativeResult<PersonProfile> {
        return withContext(Dispatchers.Main.immediate) {
            val loginUserId = V2TIMManager.getInstance().loginUser.orEmpty()

            if (loginUserId.isBlank()) {
                return@withContext NativeResult.Failure(
                    code = -1,
                    message = "当前没有已登录用户",
                )
            }

            suspendCancellableCoroutine< NativeResult<PersonProfile> > { continuation ->
                V2TIMManager.getInstance().getUsersInfo(
                    listOf(loginUserId),
                    object : V2TIMValueCallback<List<V2TIMUserFullInfo>> {

                        override fun onSuccess(
                            users: List<V2TIMUserFullInfo>?,
                        ) {
                            if (!continuation.isActive) {
                                return
                            }

                            val userInfo = users?.firstOrNull()

                            if (userInfo == null) {
                                continuation.resume(
                                    NativeResult.Failure(
                                        code = -2,
                                        message = "腾讯 IM SDK 未返回用户资料",
                                    ),
                                )
                                return
                            }
                            continuation.resume(NativeResult.Success(data = userInfo.toPersonProfile()))

                        }

                        override fun onError(
                            code: Int,
                            message: String?,
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(
                                    NativeResult.Failure(
                                        code = code,
                                        message = message.orEmpty()
                                            .ifBlank { "获取用户资料失败" },
                                    ),
                                )
                            }
                        }
                    },
                )
            }
        }
    }

}




data class RestoredSessionData(
    val userId: String,
    val canAutoLogin: Boolean,
)

sealed interface NativeResult<out T> {
    data class Success<T>(
        val data: T,
    ) : NativeResult<T>

    data class Failure(val code: Int, val message: String) : NativeResult<Nothing>
}
 fun V2TIMUserFullInfo.toPersonProfile(): PersonProfile {
    return PersonProfile(
        id = userID.orEmpty(),
        avatarUrl = faceUrl.orEmpty(),
        nickname = nickName?.trim().orEmpty(),
        remark = "",
        signature = selfSignature?.trim().orEmpty(),
        addTime = 0L,
        isFriend = false,
    )
}