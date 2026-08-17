package com.wally.demo.kuiklywallychat.chat.im

import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile


interface FriendProfileGateway {
    fun LoadFriendProfile(
        friendId:String,
        callback: (ImResult<PersonProfile>) -> Unit,
    )

    fun setFriendRemark(
        friendId: String,
        remark: String,
        callback: (ImResult<Unit>) -> Unit,
    )

    fun addFriend(
        userId: String,
        callback: (ImResult<Unit>) -> Unit,
    )

    fun deleteFriend(
        friendId: String,
        callback: (ImResult<Unit>) -> Unit,
    )
}