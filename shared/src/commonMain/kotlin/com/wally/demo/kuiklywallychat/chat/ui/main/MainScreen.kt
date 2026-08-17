package com.wally.demo.kuiklywallychat.chat.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.DrawerValue
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.ModalNavigationDrawer
import com.tencent.kuikly.compose.material3.NavigationBar
import com.tencent.kuikly.compose.material3.NavigationBarItem
import com.tencent.kuikly.compose.material3.Scaffold
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.TopAppBar
import com.tencent.kuikly.compose.material3.rememberDrawerState
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.util.fastForEachIndexed
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.WallyConversation
import com.wally.demo.kuiklywallychat.chat.ui.main.mine.PersonProfileScreen
import com.wally.demo.kuiklywallychat.chat.ui.widgets.ImageButton
import com.wally.demo.kuiklywallychat.chat.ui.widgets.NavigationIconWithBadge
import com.wally.demo.timsdk.ui.main.conversation.ConversationScreen
import com.wally.demo.timsdk.ui.main.conversation.logic.ConversationPageViewState
import com.wally.demo.timsdk.ui.main.friendship.FriendShipScreen
import com.wally.demo.timsdk.ui.main.friendship.logic.FriendshipState
import com.wally.demo.timsdk.ui.main.logic.MainPageDrawerViewState
import com.wally.demo.timsdk.widgets.LoadingDialog
import com.wally.demo.timsdk.widgets.LoadingDialogViewState
import com.wally.demo.timsdk.widgets.MainDrawMenu
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainPageDrawerViewState: MainPageDrawerViewState,
    conversationState: ConversationPageViewState,
    friendshipState: FriendshipState,
    friendShipLoadingState:LoadingDialogViewState,
    onFriendClick: (PersonProfile) -> Unit,
    onGroupClick: (GroupProfile) -> Unit,
    onDismissAddFriendSheet: () -> Unit,
    onPersonInfoClick: () -> Unit,
    onChangeTheme: () -> Unit,
    onLogoutClick: () -> Unit,
    onClickConversation:(WallyConversation)-> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    //选中的tab状态
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val bottomTabs = listOf(
        BottomTab("聊天", BottomTabIcon.CONVERSATION),
        BottomTab("联系人", BottomTabIcon.FRIENDSHIP),
        BottomTab("个人信息", BottomTabIcon.PROFILE),
    )
//    val unReadTotalNum by ComposeChat.conversationProvider.totalUnReadMsgCountFlow.collectAsState(initial = 0)
//    var unReadTotalNum by remember() { ComposeChat.conversationProvider.totalUnReadMsgCountFlow.collectAsState(0)}
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawMenu(
                mainPageDrawerViewState,
                personInfoClick =onPersonInfoClick,
                changeTheme =onChangeTheme,
                logoutClick = onLogoutClick,
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        ImageButton(
                            assetName = "ic_menu",
                            contentDescription = "打开侧边栏",
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            },
                        )
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    bottomTabs.fastForEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = index == selectedTab,
                            onClick = {
                                selectedTab = index
                            },
                            icon = {
                                NavigationIconWithBadge(
                                    icon = tab.icon,
                                    unreadCount = if (index == 0) {
                                        conversationState.unReadTotalNum.toInt()
                                    } else {
                                        0
                                    },
                                    contentDescription = tab.name,
                                )
                            },
                            label = {
                                Text(tab.name)
                            },
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> {
                        ConversationScreen(conversationState, onClickConversation)
                    }

                    1 -> {
                        FriendShipScreen(
                            state = friendshipState,
                            onFriendClick = onFriendClick,
                            onGroupClick = onGroupClick,
                            onDismissAddSheet = onDismissAddFriendSheet,
                        )
                        LoadingDialog(friendShipLoadingState)
//                        FriendShipScreen(friendshipPageViewState)
                    }

                    2 -> {
                        PersonProfileScreen(mainPageDrawerViewState){urlStr->
                            Utils.toast("点击了头像预览")
                        }
                    }
                }
            }


        }
    }
}

enum class BottomTabIcon {
    CONVERSATION,
    FRIENDSHIP,
    PROFILE,
}

data class BottomTab(
    val name: String,
    var icon: BottomTabIcon,
)

