package com.wally.demo.kuiklywallychat.chat.base.model

import androidx.compose.runtime.Stable
import com.wally.demo.kuiklywallychat.chat.base.TimeFormatter


@Stable
data class PersonProfile(
    val id: String,
    val avatarUrl: String,
    val nickname: String,
    val remark: String,
    val signature: String,
    val addTime: Long,
    val isFriend: Boolean,
) {

    companion object {

        val Empty = PersonProfile(
            id = "",
            avatarUrl = "",
            nickname = "",
            remark = "",
            signature = "",
            addTime = 0,
            isFriend = false
        )

    }

    val showName: String
        get() = remark.ifBlank {
            nickname.ifBlank {
                id
            }
        }

}

@Stable
data class GroupMemberProfile(
    val detail: PersonProfile,
    val isOwner: Boolean,
    val joinTime: Long,
) {

    val joinTimeFormat = TimeFormatter.formatTimeYMDHMS(milliseconds = joinTime)

}

@Stable
data class GroupProfile(
    val id: String,
    val avatarUrl: String,
    val name: String,
    val introduction: String,
    val createTime: Long,
) {

    val createTimeFormat: String
        get() = TimeFormatter.formatTimeYMDHMS(milliseconds = createTime)

}


fun PersonProfile.toBridgeMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "avatarUrl" to avatarUrl,
        "nickname" to nickname,
        "remark" to remark,
        "signature" to signature,
        "addTime" to addTime,
        "isFriend" to isFriend,
    )
}
fun GroupProfile.toBridgeMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "avatarUrl" to avatarUrl,
        "name" to name,
        "introduction" to introduction,
        "createTime" to createTime,
    )
}

fun GroupMemberProfile.toBridgeMap(): Map<String, Any> {
    return mapOf(
        "detail" to detail.toBridgeMap(),
        "isOwner" to isOwner,
        "joinTime" to joinTime,
    )
}