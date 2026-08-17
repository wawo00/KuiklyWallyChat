package com.wally.demo.timsdk.ui.main.logic

import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.timsdk.ui.theme.AppThemeMode


data class MainPageDrawerViewState(
//    var appTheme: AppThemeMode,
    var personProfile: PersonProfile= PersonProfile.Empty,
    val isProfileLoading: Boolean = false,
    val profileErrorMessage: String? = null,
//    var onPersonInfoClick:()-> Unit,
//    var onLogout:()->Unit,
//    var changeTheme:()-> Unit
)