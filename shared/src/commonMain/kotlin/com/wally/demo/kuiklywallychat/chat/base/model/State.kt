package com.wally.demo.kuiklywallychat.chat.base.model

import androidx.compose.runtime.Stable


// 用于timsdk的状态回调
enum class ServerConnectState {
    Idle,
    Logout,
    Connecting,
    Connected,
    ConnectFailed,
    UserSigExpired,
    KickedOffline;
}
