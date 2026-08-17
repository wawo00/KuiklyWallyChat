package com.wally.demo.kuiklywallychat.base

import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.reactive.handler.*
import com.wally.demo.kuiklywallychat.chat.base.PageNavigator
import com.wally.demo.kuiklywallychat.chat.base.PageNavigatorData
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.im.DownloadModule
import com.wally.demo.kuiklywallychat.chat.im.ImModule
import com.wally.demo.kuiklywallychat.chat.im.MediaPickerModule

abstract class BasePager : ComposeContainer() , PageNavigator{
    private var nightModel: Boolean? by observable(null)

    override fun createExternalModules(): Map<String, Module>? {
//        val externalModules = hashMapOf<String, Module>()
//        externalModules[BridgeModule.MODULE_NAME] = BridgeModule()
//        return externalModules
        return hashMapOf(
            BridgeModule.MODULE_NAME to BridgeModule(),
            ImModule.MODULE_NAME to ImModule(),
            MediaPickerModule.MODULE_NAME to MediaPickerModule(),
            DownloadModule.MODULE_NAME to DownloadModule(),
        )
    }

    override fun created() {
        super.created()
        isNightMode()
    }

    override fun themeDidChanged(data: JSONObject) {
        super.themeDidChanged(data)
        nightModel = data.optBoolean(IS_NIGHT_MODE_KEY)
    }

    // 是否为夜间模式
    override fun isNightMode(): Boolean {
        if (nightModel == null) {
            nightModel = pageData.params.optBoolean(IS_NIGHT_MODE_KEY)
        }
        return nightModel!!
    }

    // 不开启调试UI模式
    override fun debugUIInspector(): Boolean {
        return false
    }

    companion object {
        const val IS_NIGHT_MODE_KEY = "isNightMode"
    }

     fun closeCurrentPage() {
        acquireModule<RouterModule>(
            RouterModule.MODULE_NAME,
        ).closePage()
    }

    override fun goToPage(navigatorData: PageNavigatorData) {
        acquireModule<RouterModule>(
            RouterModule.MODULE_NAME,
        ).openPage(
            pageName = navigatorData.pageName,
            pageData = navigatorData.pageData
        ).apply {
            if (navigatorData.closeCurrentPage){
                closeCurrentPage()
            }
        }

    }
}