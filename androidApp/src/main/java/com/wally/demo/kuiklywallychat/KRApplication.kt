package com.wally.demo.kuiklywallychat

import android.app.Application
import com.wally.demo.kuiklywallychat.im.ImRuntime
import com.wally.demo.kuiklywallychat.im.storage.LoginPreferences

class KRApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        application = this
        //初始化本地持久化层
        LoginPreferences.init(application = this)
        //不要只在 init 块中执行复杂初始化。Application.onCreate() 是 SDK 初始化的明确生命周期位置。
        ImRuntime.initialize(this)
    }

    companion object {
        lateinit var application: Application
    }
}