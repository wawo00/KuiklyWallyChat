package com.wally.demo.timsdk.ui.main.friendship

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.text.input.rememberTextFieldState
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.MaterialTheme
import com.tencent.kuikly.compose.material3.ModalBottomSheet
import com.tencent.kuikly.compose.material3.Surface
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.TextField
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.ui.widgets.EmptyContent
import com.wally.demo.timsdk.ui.main.friendship.logic.FriendshipState
import com.wally.demo.timsdk.widgets.FriendShipListItem

/**
 * @author Wally(25054984)
 * @since 2026/7/6
 * @email wanlei@haier.com
 * @desciption 用于主页中显示联系人
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendShipScreen(
    state: FriendshipState,
    onFriendClick: (PersonProfile) -> Unit,
    onGroupClick: (GroupProfile) -> Unit,
    onDismissAddSheet: () -> Unit,
){

    Box(modifier = Modifier.fillMaxSize()) {
        when {
//            (state.isLoadingFriends || state.isLoadingGroups)  -> {Text("正在加载")}
            state.errorMessage != null && (state.friends.isEmpty()||state.groups.isEmpty()) -> ErrorContent(state.errorMessage)
            state.friends.isEmpty() && state.groups.isEmpty() -> EmptyContent()
            else ->  {
            }
        }
        FriendShipList(state,onGroupClick,onFriendClick)
        KuiklyFloatButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            state.onShowAddSheet
        )

        //    // --- 请在 Box 的最后添加以下代码 ---
        if (state.showAddFriendSheet) {
            ModalBottomSheet(
                visible = state.showAddFriendSheet,
                onDismissRequest = onDismissAddSheet,
            ) {
                // 这里是弹窗的内容，你可以根据图片自己调整
                FriendShipSheetContent(state)
            }
        }

    }

}

@Composable
fun ErrorContent(errorMsg:String?){
    Box(modifier = Modifier.fillMaxSize()){
        Text(text = "数据错误: ${errorMsg.orEmpty().ifBlank { "未知错误" }}")
    }
}



@Composable
fun KuiklyFloatButton(
    modifier: Modifier = Modifier,
    onShowAddSheet: () -> Unit,
) {
    Surface(
        onClick =onShowAddSheet,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 6.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "＋",
                fontSize = 28.sp,
            )
        }
    }
}
@Composable
fun FriendShipList(viewState: FriendshipState, onGroupClick: (GroupProfile) -> Unit, onFriendClick: (PersonProfile) -> Unit) {
    val friendList = viewState.friends
    val joinedGroupList = viewState.groups
//    val onItemClick = viewState.onItemClick
    if (friendList.isEmpty() && joinedGroupList.isEmpty()) {
        Text("Empty2", fontSize = 42.sp)
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            //先显示group，再显示friend
            items(joinedGroupList) { group ->
                FriendShipListItem(
                    imageUrl = group.avatarUrl,
                    title = group.name,
                    subTitle = group.introduction,
                    onClick = {
                        onGroupClick(group)
                    }
                )
            }
            items(friendList) { friend ->
                FriendShipListItem(
                    imageUrl = friend.avatarUrl,
                    title = friend.showName,
                    subTitle = friend.signature,
                    onClick = {
                        onFriendClick(friend)
//                        onItemClick(friend.id, FriendShipType.FRIEND)
                    }
                )
            }
        }
    }
}

@Composable
fun FriendShipSheetContent(state: FriendshipState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 这里可以使用 OutlinedTextField (输入框)
        // 和多个 Button (蓝色按钮)
        var text1 by remember { mutableStateOf("") }
        TextField(
            value=text1,
            modifier = Modifier.padding(vertical = 10.dp),
            onValueChange =  { text1 = it },
            label = { Text("基础输入框") },
            placeholder = { Text("请输入内容...") },

        )

        Button(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            onClick = {
                    state.onAddFriend(text1)
            }
        ) {
            Text(text="添加好友", color = Color.White)
        }

        // 加入交流群的按钮...
        repeat(5) { index ->

            Button(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                onClick = {
                    state.onJoinGroup(index)
                }
            ) {
                Text("加入交流群 0x0${index + 1}", color = Color.White)
            }
        }
    }
}


