package com.wally.demo.kuiklywallychat.ext

import androidx.compose.runtime.CompositionLocal
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.pager.Pager
import com.wally.demo.kuiklywallychat.base.Utils

fun String.Toast() {
    Utils.toast(this)
}

fun String.logNative() {
    Utils.logToNative(this)
}


/**
 * 关闭当前页面
 * 使用方式
 * compose组件中
 *    val page = LocalActivity.current
 *     page.closeCurrentPage
 */

 fun ComposeContainer.closeCurrentPage() {
    acquireModule<RouterModule>(RouterModule.MODULE_NAME).closePage()
}


/**
 * List<T> 转 JSONArray。
 *
 * transform 用于把自定义对象转换成 JSON 支持的值，
 * 例如 String、Int、Boolean、JSONObject 等。
 */
fun <T> Iterable<T>.toJSONArray(
    transform: (T) -> Any? = { it },
): JSONArray {
    return JSONArray().apply {
        for (value in this@toJSONArray) {
            put(transform(value))
        }
    }
}

/**
 * JSONArray 转 List<T>。
 *
 * transform 用于把 JSONArray 中的元素转换成目标类型。
 */
fun <T> JSONArray.toTypedList(
    transform: (Any?) -> T,
): List<T> {
    return (0 until length()).map { index ->
        transform(opt(index))
    }
}