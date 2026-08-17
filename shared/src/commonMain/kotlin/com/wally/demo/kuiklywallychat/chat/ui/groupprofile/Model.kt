package com.wally.demo.kuiklywallychat.chat.ui.groupprofile

import com.wally.demo.kuiklywallychat.chat.base.model.GroupMemberProfile
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile


data class GroupProfilePageViewState(
    val groupInfo: GroupProfile?,
    val memberList: List<GroupMemberProfile>,
    val onItemClick:(GroupMemberProfile)-> Unit,
    val onChangeAvatar:(GroupProfile)->Unit,
    val onClickQuitGroup :()-> Unit, //仅仅用来显示删除弹窗
)