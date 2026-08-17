package com.wally.demo.kuiklywallychat.chat.ui.login

/**
  * @author Wally(25054984)
  * @since 2026/7/27 
  * @email wanlei@haier.com
  * @desciption 登录状态管理
 * @tips:为什么没有之前onclicklogin函数了？ ->状态只应描述页面，不应持有 Android Activity 或业务函数,
  */


data class LoginState(
    val userId: String = "",
    val isLoading: Boolean = false,
    val showLoginForm: Boolean = true,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
)