package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.ColumnScope
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.ModalBottomSheet
import com.tencent.kuikly.compose.ui.Modifier

@Composable
fun CommonBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        dismissOnDrag = true,
        content = content,
    )
}