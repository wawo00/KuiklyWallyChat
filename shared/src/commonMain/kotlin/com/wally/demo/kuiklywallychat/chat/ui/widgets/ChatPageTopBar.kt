package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.TopAppBar
import com.wally.demo.kuiklywallychat.chat.ui.widgets.ImageButton
import com.wally.demo.kuiklywallychat.ext.Toast


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPageTopBar(title:String, backClick: () -> Unit, moreClick: (() -> Unit)?=null) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            ImageButton(
                assetName = "ic_back",
                onClick = backClick
            )
        },

        actions = {
            moreClick?.let {
                ImageButton(
                    assetName = "ic_more",
                    onClick = it
                )
            }

        }
    )
}
