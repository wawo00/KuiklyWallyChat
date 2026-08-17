package com.wally.demo.kuiklywallychat.chat.base.model
/**
  * @author Wally(25054984)
  * @since 2026/6/30
  * @email wanlei@haier.com
  * @desciption 用于返回timsdk的结果
  */
sealed class ActionResult {
    data object Success: ActionResult()
    data class Fail(var code: Int, var msg: String?): ActionResult(){
        constructor(msg: String?):this(-1,msg)
        val desc="$code : ${msg ?:"unknown"}"
    }

}
