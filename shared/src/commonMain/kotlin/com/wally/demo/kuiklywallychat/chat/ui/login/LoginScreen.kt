package com.wally.demo.kuiklywallychat.chat.ui.login

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.TextField
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp


/**
  * @author Wally(25054984)
  * @since 2026/7/27
  * @email wanlei@haier.com
  * @desciption 登录页面，不要引入 Android Compose Material
  */
@Composable
fun LoginScreen(
    state: LoginState,
    onUserIdChanged: (String) -> Unit,
    onLogin: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("腾讯 IM 登录")

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            value = state.userId,
            onValueChange = onUserIdChanged,
            enabled = !state.isLoading,
            label = { Text("UserId") },
            singleLine = true,
        )

        state.errorMessage?.let {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = it,
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !state.isLoading,
            onClick = onLogin,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("登录")
            }
        }
    }
}