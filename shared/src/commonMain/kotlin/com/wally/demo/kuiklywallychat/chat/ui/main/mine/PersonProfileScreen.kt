package com.wally.demo.kuiklywallychat.chat.ui.main.mine

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.wally.demo.timsdk.ui.main.logic.MainPageDrawerViewState
import com.wally.demo.timsdk.widgets.AsyncImage


@Composable
fun PersonProfileScreen(viewState: MainPageDrawerViewState, onClickPreviewImage: (String) -> Unit){
    val personProfile = viewState.personProfile ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 头部背景和头像
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            // 背景图 (使用用户的头像作为模糊背景，或者一个固定背景)
            AsyncImage(
                model = personProfile.avatarUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )

            // 蒙层，让背景暗一点，文字更清晰
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            // 信息展示
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 圆形头像
                AsyncImage(
                    model = personProfile.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable(onClick = {onClickPreviewImage(personProfile.avatarUrl)}),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 昵称
                Text(
                    text = personProfile.nickname.ifEmpty { personProfile.id },
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                // 签名
                if (personProfile.signature.isNotEmpty()) {
                    Text(
                        text = personProfile.signature,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // ID
                Text(
                    text = "ID: ${personProfile.id}",
                    color = Color.White.copy(alpha = 1.0f),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

            }
        }
    }

}