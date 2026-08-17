//package com.wally.demo.timsdk.ui.main.logic
//
//import android.content.Intent
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.viewModelScope
//import com.wally.demo.timsdk.MyApp
//import com.wally.demo.timsdk.base.datasource.LoginSpPreferences
//import com.wally.demo.timsdk.base.models.ActionResult
//import com.wally.demo.timsdk.base.models.PersonProfile
//import com.wally.demo.timsdk.logic.ComposeChat.accountProvider
//import com.wally.demo.timsdk.models.ServerConnectState
//import com.wally.demo.timsdk.provider.AppThemeProvider
//import com.wally.demo.timsdk.ui.friend.FriendProfileActivity
//import com.wally.demo.timsdk.ui.login.LoginActivity
//import com.wally.demo.timsdk.ui.main.conversation.logic.ConversationViewModel
//import com.wally.demo.timsdk.ui.theme.AppThemeMode
//import kotlinx.coroutines.launch
//
//class MainViewModel : ConversationViewModel() {
//
//    var mainPageDrawerViewState by mutableStateOf(
//        value = MainPageDrawerViewState(
//            appTheme = AppThemeProvider.appThemeMode,
//            personProfile = accountProvider.personProfileFlow.value,
//            onPersonInfoClick = ::onPersonInfoClick,
//            onLogout = ::onLogout,
//            changeTheme = ::changeTheme
//        )
//    )
//        private set
//
//
//    init {
//
//        viewModelScope.launch {
//            launch {
//                requestData()
//            }
//
//            launch {
//                accountProvider.personProfileFlow.collect { personProfile ->
//                    onPersonProfileChanged(personProfile = personProfile)
//                }
//            }
//            launch {
//                accountProvider.serverConnectStateFlow.collect { state ->
//                    when (state) {
//                        ServerConnectState.KickedOffline -> {
//                            showToast("别人登录了你的号，你被踢下线了")
//                            LoginSpPreferences.onUserLogout()
//                            navToLoginPage()
//                        }
//
//                        ServerConnectState.Logout,
//                        ServerConnectState.UserSigExpired,
//                            -> {
//                            navToLoginPage()
//                        }
//
//                        ServerConnectState.Idle,
//                        ServerConnectState.Connecting,
//                        ServerConnectState.ConnectFailed,
//                            -> {
//                        }
//
//                        ServerConnectState.Connected -> {
//                            requestData()
//                        }
//
//                    }
//                }
//            }
//
//
//        }
//    }
//    private fun onPersonProfileChanged(personProfile: PersonProfile) {
//        if (mainPageDrawerViewState.personProfile != personProfile) {
//            mainPageDrawerViewState = mainPageDrawerViewState.copy(personProfile = personProfile)
//        }
//    }
//    private suspend fun requestData() {
//        conversationProvider.refreshTotalUnreadMessageCount()
//        accountProvider.refreshPersonProfile()
//    }
//
//    private fun navToLoginPage() {
//        val intent = Intent(MyApp.instance, LoginActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//        MyApp.instance.startActivity(intent)
//    }
//
//    fun onPersonInfoClick(){
//        FriendProfileActivity.navTo(MyApp.instance,mainPageDrawerViewState.personProfile.id)
//    }
//
//    fun onLogout(){
//        viewModelScope.launch {
//            showLoading()
//            when (val result = accountProvider.logout()) {
//                is ActionResult.Success -> {
//                    LoginSpPreferences.onUserLogout()
//                    showToast("操作成功")
//                    navToLoginPage()
//                }
//
//                is ActionResult.Fail -> {
//                    showToast(msg = result.desc)
//                    navToLoginPage()
//                }
//            }
//            hideLoading()
//        }
//    }
//
//    fun changeTheme(){
//        val nextTheme = AppThemeProvider.appThemeMode.nextTheme()
//        mainPageDrawerViewState = mainPageDrawerViewState.copy(appTheme = nextTheme)
//        AppThemeProvider.onAppThemeModeChanged(appThemeMode = nextTheme)
//    }
//    private fun AppThemeMode.nextTheme(): AppThemeMode {
//        val values = AppThemeMode.entries
//        return values.getOrElse(index = ordinal + 1, defaultValue = { values[0] })
//    }
//}