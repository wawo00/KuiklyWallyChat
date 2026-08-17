package com.wally.demo.kuiklywallychat.chat.im

import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile

interface FriendshipGateway  {
    fun loadFriends(
        callback: (ImResult<List<PersonProfile>>) -> Unit,
    )

    fun loadJoinedGroups(
        callback: (ImResult<List<GroupProfile>>) -> Unit,
    )

    fun addFriend(
        userId: String,
        callback: (ImResult<Unit>) -> Unit,
    )

    fun joinGroup(
        groupId: String,
        callback: (ImResult<Unit>) -> Unit,
    )
}

/**
 * 返回数据协议
 * {
 *   "success": true,
 *   "code": 0,
 *   "message": "",
 *   "data": {
 *     "items": [
 *       {
 *         "id": "user_001",
 *         "avatarUrl": "...",
 *         "nickname": "Alice",
 *         "remark": "",
 *         "signature": "...",
 *         "addTime": 0,
 *         "isFriend": true
 *       }
 *     ]
 *   }
 * }
 *
 *
 *
 */