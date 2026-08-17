package com.wally.demo.timsdk.ui.main.friendship.logic

import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile

data class FriendshipState(
    val friends: List<PersonProfile> = emptyList(),
    val groups: List<GroupProfile> = emptyList(),
    val isLoadingFriends: Boolean = false,
    val isLoadingGroups: Boolean = false,
    val errorMessage: String? = null,
    val showAddFriendSheet: Boolean = false,
    val addFriendUserId: String = "",
    val isSubmitting: Boolean = false,
    val onAddFriend:(String)-> Unit,
    var onJoinGroup:(Int)-> Unit,
    var onShowAddSheet:()-> Unit

){
    /**
     * 首次进入页面、还没有任何旧数据时，显示全屏 Loading。
     *
     * 如果已经有旧列表，后台刷新时不必清空列表并显示全屏 Loading。
     */
    val isInitialLoading: Boolean
        get() = friends.isEmpty() &&
                groups.isEmpty() &&
                (isLoadingFriends || isLoadingGroups)

    val isRefreshing: Boolean
        get() = isLoadingFriends || isLoadingGroups

    val isEmpty: Boolean
        get() = friends.isEmpty() &&
                groups.isEmpty() &&
                !isRefreshing
}




