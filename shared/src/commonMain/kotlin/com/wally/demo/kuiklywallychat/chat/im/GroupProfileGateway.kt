package com.wally.demo.kuiklywallychat.chat.im

import com.wally.demo.kuiklywallychat.chat.base.model.GroupMemberProfile
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile


interface GroupProfileGateway {


    fun loadGroupProfileByCallback(
        groupId: String,
        callback: (ImResult<GroupProfile>) -> Unit,
    )

    fun loadGroupMembersByCallback(
        groupId:String,
        callback: (ImResult<List<GroupMemberProfile>>) -> Unit,
    )



//改成协程
    suspend fun loadGroupProfile(
        groupId: String,
    ): ImResult<GroupProfile>

    suspend fun loadGroupMembers(
        groupId: String,
    ): ImResult<List<GroupMemberProfile>>

    fun quitGroup(
        groupId: String,
        callback: (ImResult<Unit>) -> Unit,
    )
}