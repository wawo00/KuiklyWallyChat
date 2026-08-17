package com.wally.demo.kuiklywallychat.chat.base

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

interface PageNavigator {
    fun goToPage(navigatorData: PageNavigatorData)
}

/**
 * 用于页面间跳转
 *
 * @desc 因为页面的跳转只能在page中实现，所以使用pagedata将内容包裹
 */
data class PageNavigatorData(
    val pageName:String,
    val pageData: JSONObject,
    val closeCurrentPage: Boolean=false //跳转时，是不是关闭当前页面
)
