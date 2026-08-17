package com.wally.demo.kuiklywallychat.chat.base.model

sealed class UIEvent {
    data class ShowToast(val msg: String) : UIEvent()
    data class NavigateTo(val screen: String) : UIEvent()
    data  object showLoading : UIEvent()
    data  object hideLoading : UIEvent()
    data  object showDialog : UIEvent()
}
