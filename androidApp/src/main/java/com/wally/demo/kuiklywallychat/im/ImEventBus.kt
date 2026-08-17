package com.wally.demo.kuiklywallychat.im

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
  * @author Wally(25054984)
  * @since 2026/7/27 
  * @email wanlei@haier.com
  * @desciption Android 主动事件通知 Kuikly，不持有activity，使用流实现
  */


data class ImNativeEvent(
    val name: String,
    val data: Map<String, Any>,
)

class ImEventBus {

    private val mutableEvents =
        MutableSharedFlow<ImNativeEvent>(
            extraBufferCapacity = 32,
        )

    val events = mutableEvents.asSharedFlow()

    fun emit(
        name: String,
        data: Map<String, Any>,
    ) {
        val emitted = mutableEvents.tryEmit(
            ImNativeEvent(name, data),
        )

        Log.i(
            "ImEventBus",
            "emit: name=$name, " +
                    "emitted=$emitted, " +
                    "subscribers=${mutableEvents.subscriptionCount.value}",
        )
    }
}