package com.wally.demo.timsdk.ui.friend.logic

import androidx.compose.runtime.Stable
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile


//todo:为什么需要 @Stable =>如果两个entity equals，使用的compose就不重组了，否则下次重组使用这个entity肯定会重组
@Stable
data class FriendProfilePageViewState(
    val personProfile: PersonProfile?,
    val isMe: Boolean,
    val isFriend: Boolean,
    val onClickSetFriendRemark: () -> Unit,
    val onClickAddFriend: () -> Unit,
    val onClickDeleteFriend: () -> Unit,
    val onClickChat: (PersonProfile) -> Unit,
)
@Stable
data class ConfirmDialogViewState(
    val contentStr:String="",
    val isVisible: Boolean,
    val onDismissDialog: () -> Unit,
    val onConfirm: () -> Unit
)
@Stable
data class SetFriendRemarkDialogViewState(
    val isVisible: Boolean,
    val remark: String,
    val onSetFriendRemark: (remark: String) -> Unit,
    val onDismissDialog: () -> Unit
)