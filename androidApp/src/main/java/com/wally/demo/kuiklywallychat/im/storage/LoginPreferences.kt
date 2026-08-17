package com.wally.demo.kuiklywallychat.im.storage

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


object LoginPreferences {

    private lateinit var preferences: SharedPreferences

    val lastLoginUserId: String
        get() = preferences.getString("last_login_user_id", "").orEmpty()

    val canAutoLogin: Boolean
        get() = preferences.getBoolean("auto_login", false)

    fun onLoginSuccess(userId: String) {
        preferences.edit()
            .putString("last_login_user_id", userId)
            .putBoolean("auto_login", true)
            .apply()
    }

    fun onLogoutSuccess() {
        preferences.edit()
            .putBoolean("auto_login", false)
            .apply()
    }

    fun init(application: Application) {
        preferences = application.getSharedPreferences(
            "im_account",
            Context.MODE_PRIVATE,
        )
    }

    fun disableAutoLogin() {
        preferences.edit()
            .putBoolean("auto_login", false)
            .apply()
    }
}