package com.wally.demo.kuiklywallychat.chat.ui.widgets
import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.wrapContentWidth
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Card
import com.tencent.kuikly.compose.material3.CardDefaults
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.TextButton
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.window.Dialog
import com.tencent.kuikly.compose.ui.window.DialogProperties
import com.wally.demo.timsdk.ui.friend.logic.ConfirmDialogViewState

@Composable
fun KuiklyAlertDialog(
    content: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            inWindow = true,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(content)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }

                    TextButton(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .wrapContentWidth(),
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}


@Composable
fun KuiklyAlertDialog(
    confirmDialogViewState:ConfirmDialogViewState,
) {

    if (!confirmDialogViewState.isVisible) {
        return
    }
    Dialog(
        onDismissRequest = confirmDialogViewState.onDismissDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            inWindow = true,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(confirmDialogViewState.contentStr)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = confirmDialogViewState.onDismissDialog) {
                        Text("取消")
                    }

                    TextButton(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .wrapContentWidth(),
                        onClick = {
                            confirmDialogViewState.onConfirm()
                            confirmDialogViewState.onDismissDialog()
                        },
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}