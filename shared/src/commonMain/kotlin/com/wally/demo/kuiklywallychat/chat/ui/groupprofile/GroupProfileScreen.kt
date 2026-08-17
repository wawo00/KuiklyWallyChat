package com.wally.demo.kuiklywallychat.chat.ui.groupprofile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Scaffold
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.graphicsLayer
import com.tencent.kuikly.compose.ui.input.key.Key.Companion.R
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.platform.LocalOnBackPressedDispatcherOwner
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.DpOffset
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.module.RouterModule
import com.wally.demo.kuiklywallychat.chat.base.model.GroupMemberProfile
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.ui.widgets.GroupActionMenu
import com.wally.demo.kuiklywallychat.chat.ui.widgets.ImageButton
import com.wally.demo.timsdk.widgets.AsyncImage
import com.wally.demo.timsdk.widgets.ChatPageTopBar


@Composable
fun GroupProfileScreen(viewState: GroupProfilePageViewState) {

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
//            .navigationBarsPadding()  使用scaffold进行包装
//            .systemBarsPadding()
        ) {
            var scrollState = rememberLazyListState()

            var denstiny = LocalDensity.current
            var stickHeaderHeightPx = with(denstiny) { 200.dp.toPx() }

            val topBarAlpha by remember {
                derivedStateOf {
                    if (scrollState.firstVisibleItemIndex > 0) {
                        1f
                    } else {
                        (scrollState.firstVisibleItemScrollOffset / stickHeaderHeightPx).coerceIn(0f, 1f)
                    }
                }
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                item(key = "header", contentType = { "header" }) {
                    viewState.groupInfo?.apply {
                        GroupHeader(this)
                    }
                }

                items(viewState.memberList, key = { it.detail.id }, contentType = { "GroupMemberItem" }) { member ->
                    GroupMemberItem(member, {
                        viewState.onItemClick(member)
                    })
                }
            }

            GroupProfileTopbar(viewState.groupInfo?.name ?: "", topBarAlpha, {
                viewState.onClickQuitGroup()
            })
        }
    }


}

@Composable
private fun GroupHeader(groupProfile: GroupProfile) {
    val introductionStr = "groupId:${groupProfile.id} \n createTime: ${groupProfile.createTimeFormat} \n"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = groupProfile.avatarUrl,
            contentDescription = "",
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.align(alignment = Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(60.dp)
                    .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp)),
                model = groupProfile.avatarUrl,
                contentDescription = ""
            )
            Text(text = introductionStr, color = Color.White)
        }


    }

}


@Composable
private fun GroupProfileTopbar(title: String, alpha: Float, onQuit: () -> Unit) {
    var menuExpanded by remember {
        mutableStateOf(false)
    }
    val onBackPressDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val page = LocalActivity.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha } // 核心：背景随滚动渐变
                .background(Color.White) // 渐变后的背景色
        )

        Row {

            ImageButton(assetName = "ic_back", modifier = Modifier.size(44.dp), onClick = {
                page.acquireModule<RouterModule>(RouterModule.MODULE_NAME).closePage()
            })

            Text(
                text = title, modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { this.alpha = alpha }
                    .align(Alignment.CenterVertically), textAlign = TextAlign.Center, fontSize = 22.sp
            )

            Box {
                ImageButton(assetName = "ic_more", modifier = Modifier.size(44.dp), onClick = {
                    menuExpanded = true
                })

                GroupActionMenu(
                    expanded = menuExpanded,
                    onDismissRequest = {
                        menuExpanded = false
                    },
                    onEditAvatar = {
                        // 建议发送 UiEffect.Toast("暂未实现")
                        // 或触发图片选择流程
                    },
                    onQuit = {
                        onQuit()
                    },
                )
//                DropdownMenu(
//                    // x 为负数向左移，y 为正数向下移
//                    offset = DpOffset(x = (-16).dp, y = 8.dp),
//                    expanded = menuExpanded,
//                    modifier = Modifier.align(alignment = Alignment.TopEnd),
//                    onDismissRequest = { menuExpanded = false }) {
//                    DropdownMenuItem(text = { Text(text = "修改头像") }, onClick = {
//                        menuExpanded = false
//                        Toast.makeText(MyApp.instance, "没实现", Toast.LENGTH_SHORT).show()
//                    })
//                    DropdownMenuItem(text = { Text(text = "退出群聊") }, onClick = {
//                        menuExpanded = false
//                        onQuit()
//                    })
            }
//            }

        }


    }


}

@Composable
fun GroupMemberItem(memberProfile: GroupMemberProfile, onClick: () -> Unit) {
    Row(modifier = Modifier.padding(10.dp).clickable(onClick = onClick)) {
        // 图片
        AsyncImage(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(10.dp)),
            model = memberProfile.detail.avatarUrl,
            contentDescription = "",
        )
        Spacer(Modifier.width(10.dp))
        Column() {
            Text(text = memberProfile.detail.showName)
            Spacer(Modifier.height(10.dp))
            Text(text = memberProfile.joinTimeFormat)
        }
    }

}