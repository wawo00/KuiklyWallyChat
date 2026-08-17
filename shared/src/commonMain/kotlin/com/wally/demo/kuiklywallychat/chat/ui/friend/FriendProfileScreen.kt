package com.wally.demo.timsdk.ui.friend

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.extension.keyboardHeightChange
import com.tencent.kuikly.compose.foundation.background
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
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.ButtonDefaults
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.Scaffold
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.TextField
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.wally.demo.kuiklywallychat.ext.logNative
import com.wally.demo.timsdk.ui.friend.logic.FriendProfilePageViewState
import com.wally.demo.timsdk.ui.friend.logic.SetFriendRemarkDialogViewState
import com.wally.demo.timsdk.widgets.AsyncImage
import com.wally.demo.timsdk.widgets.ChatPageTopBar
import com.wally.demo.timsdk.widgets.CommonBottomSheet


@Composable
fun FriendProfileScreen(pageViewState: FriendProfilePageViewState, showStartChat: Boolean,onBackClick:()-> Unit) {
    "[FriendProfile][Screen] compose isFriend=${pageViewState.isFriend}, profileIsFriend=${pageViewState.personProfile?.isFriend}, hasProfile=${pageViewState.personProfile != null}".logNative()
    val personProfile = pageViewState.personProfile ?: return

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            ChatPageTopBar("好友信息", backClick = onBackClick)
        },

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
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
                    contentScale = ContentScale.Crop
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
                            .background(Color.LightGray),
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
                    // remark备注
                    Text(
                        text = "Remark: ${personProfile.remark}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 按钮操作区
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // 去聊天吧
                if (!pageViewState.isMe&&showStartChat) {
                    FriendProfileButton(
                        text = "去聊天吧",
                        onClick = { pageViewState.onClickChat(pageViewState.personProfile) },
                        containerColor = Color(0xFF42A5F5)
                    )
                }


                Spacer(modifier = Modifier.height(12.dp))

                // 设置备注
                if (pageViewState.isFriend) {
                    FriendProfileButton(
                        text = "设置备注",
                        onClick = pageViewState.onClickSetFriendRemark,
                        containerColor = Color(0xFF42A5F5)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 删除好友 / 添加好友
                if (pageViewState.isFriend) {
                    "[FriendProfile][Screen] render friend actions".logNative()
                    FriendProfileButton(
                        text = "删除好友",
                        onClick = pageViewState.onClickDeleteFriend,
                        containerColor = Color(0xFF42A5F5)
                    )
                } else if (!pageViewState.isMe) {
                    "[FriendProfile][Screen] render add action".logNative()
                    FriendProfileButton(
                        text = "加为好友",
                        onClick = pageViewState.onClickAddFriend,
                        containerColor = Color(0xFF42A5F5)
                    )
                }
            }
        }
    }


}

@Composable
fun FriendProfileButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetFriendRemarkDialog(viewState: SetFriendRemarkDialogViewState) {

    var keyboardHeight by remember(viewState.isVisible) {
        mutableStateOf(0f)
    }
        CommonBottomSheet(
            visible = viewState.isVisible,
            onDismissRequest  = viewState.onDismissDialog,
        ) {
            var remark by remember(key1 = viewState.isVisible) {
                mutableStateOf(value = viewState.remark)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp + keyboardHeight.dp,
                    ),
            ) {
                Text(text = "设置备注", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth().keyboardHeightChange{ keyboardInfo ->
                        keyboardHeight = keyboardInfo.height.coerceAtLeast(0f)
                    },
                    value = remark,
                    onValueChange = {value->
                        remark=value
                    },
                    placeholder ={Text( "请输入备注")}
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {viewState.onSetFriendRemark(remark)}, modifier = Modifier.align(Alignment.End)) {
                    Text("确定")
                }
            }
    }

}