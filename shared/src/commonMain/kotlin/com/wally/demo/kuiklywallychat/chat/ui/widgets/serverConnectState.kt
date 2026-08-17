package com.wally.demo.timsdk.widgets

import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.lazy.LazyListScope
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Color.Companion.LightGray
import com.tencent.kuikly.compose.ui.input.key.Key.Companion.R
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.wally.demo.kuiklywallychat.chat.base.model.ServerConnectState
import com.wally.demo.timsdk.ui.main.conversation.logic.ConversationPageViewState


fun LazyListScope.serverConnectState(modifier: Modifier = Modifier, pageState: ConversationPageViewState) {
    var serverConnectState = pageState.serverConnectState
    when (serverConnectState) {
        ServerConnectState.Idle, ServerConnectState.Connected -> {
            return
        }

        else -> {
            item(key = "serverConnectState", contentType = "serverConnectState") {
                Box(modifier = modifier
                    .fillMaxWidth()
                    .background(color = LightGray)) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        text ="serverConnectState :${serverConnectState}",
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                    )
                }
            }
        }
    }

}