package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.tencent.kuikly.compose.BackHandler
import com.tencent.kuikly.compose.animation.AnimatedVisibility
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp

data class LoadingDialogViewState(
    val isVisiable: MutableState<Boolean> = mutableStateOf<Boolean>(false),
    val isCancelable: MutableState<Boolean> = mutableStateOf<Boolean>(false),
) {
    fun show(isCancelable: Boolean = false) {
        this.isCancelable.value = isCancelable
        isVisiable.value = true

    }

    fun dismiss() {
        isVisiable.value = false
    }
}


//@Composable
//fun LoadingDialog( viewState: LoadingDialogViewState) {
//    // todo:这里使用了动画，AnimatedVisibility
//    AnimatedVisibility(visible = viewState.isVisiable.value) {
//        BackHandler(
//            onBack = {}
//        )
//        Box(
//            modifier = Modifier
//                .size(72.dp)
//                .background(
//                    color = Color.White,
//                    shape = RoundedCornerShape(12.dp),
//                ),
//            contentAlignment = Alignment.Center,
//        ) {
//            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
//        }
//    }
//
//}
@Composable
fun LoadingDialog(viewState: LoadingDialogViewState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = viewState.isVisiable.value,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = Color.Gray,
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                )
            }
        }
    }

    if (viewState.isVisiable.value) {
        BackHandler(
            onBack = {
                if (viewState.isCancelable.value) {
                    viewState.dismiss()
                }
            },
        )
    }
}