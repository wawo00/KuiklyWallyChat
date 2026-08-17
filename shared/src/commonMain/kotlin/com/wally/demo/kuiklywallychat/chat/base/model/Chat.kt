package com.wally.demo.kuiklywallychat.chat.base.model

import androidx.compose.runtime.Stable

/**
 * @Author: leavesCZY
 * @Date: 2026/6/4 21:12
 * @Desc:
 */
@Stable
sealed class Chat(open val id: String)  {

    @Stable
    data class C2C(override val id: String) : Chat(id = id)

    @Stable
    data class Group(override val id: String) : Chat(id = id)

}
 fun Chat.toBridgeType(): String {
    return when (this) {
        is Chat.C2C -> "c2c"
        is Chat.Group -> "group"
    }
}