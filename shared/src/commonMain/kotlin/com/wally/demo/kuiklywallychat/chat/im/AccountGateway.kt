package com.wally.demo.kuiklywallychat.chat.im

import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile

/**
  * @author Wally(25054984)
  * @since 2026/7/31
  * @email wanlei@haier.com
  * @desciption 用于个人信息的桥接
  */

interface AccountGateway {
    fun restoreSession(callback: (ImResult<RestoredSession>) -> Unit) //自动登录
    fun login(userId: String, callback: (ImResult<Unit>) -> Unit)
    fun logout(callback: (ImResult<String>) -> Unit)
    fun getSelfProfile(
        callback: (ImResult<PersonProfile>) -> Unit,
    )
}