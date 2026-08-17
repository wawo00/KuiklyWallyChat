package com.wally.demo.kuiklywallychat.chat.base

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wally.demo.timsdk.ui.friend.logic.ConfirmDialogViewState
import com.wally.demo.timsdk.widgets.LoadingDialogViewState
import kotlinx.coroutines.CoroutineScope


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class BaseController(var navigator: PageNavigator?) {
    private var active = false

    private val controllerJob= SupervisorJob()


    // 存在缺陷！这个是在io的，有的请求必须要在main中执行,但是render中判断了如果是main就断言了
    protected val controllerScope= CoroutineScope(controllerJob+ Dispatchers.Default)


    val loadingDialogViewState = LoadingDialogViewState()
    open fun start() {
        active = true
    }

    open fun stop() {
        active=false
        navigator=null
        controllerScope.cancel()
    }
    fun showLoading(isCanCancel: Boolean =false){
        loadingDialogViewState.show(isCanCancel)
    }

    fun hideLoading(){
        loadingDialogViewState.dismiss()
    }


    fun goToPage(navigatorData: PageNavigatorData){
        navigator?.goToPage(navigatorData)
    }
    open var confirmDialogViewState by mutableStateOf(
        value = ConfirmDialogViewState(
            isVisible = false,
            contentStr = "确认操作吗",
            onDismissDialog = ::dismissConfirmDialog,
            onConfirm = ::onConfirmDialogConfirmClicked
        )
    )
     fun showConfirmDialog() {
        confirmDialogViewState = confirmDialogViewState.copy(isVisible = true)
    }

     fun dismissConfirmDialog() {
        confirmDialogViewState = confirmDialogViewState.copy(isVisible = false)
    }

   open fun onConfirmDialogConfirmClicked(){

    }
}

