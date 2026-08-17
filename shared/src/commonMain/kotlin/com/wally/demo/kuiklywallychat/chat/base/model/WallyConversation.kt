package com.wally.demo.kuiklywallychat.chat.base.model

import androidx.compose.runtime.Stable
import com.wally.demo.kuiklywallychat.chat.base.ImString

@Stable
data class WallyConversation(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val unreadMessageCount: Long,
    val lastMessage: Message,
    val isPinned: Boolean,
    val type: ConversationType
) {

    val formatMessage = run {
        val messageDetail = lastMessage.detail
        val senderName = when (type) {
            ConversationType.C2C -> {
                ""
            }

            ConversationType.Group -> {
                when (lastMessage) {
                    is TextMessage,
                    is ImageMessage -> {
                        if (messageDetail.isOwnMessage) {
                            ""
                        } else {
                            messageDetail.sender.showName + ": "
                        }
                    }

                    is SystemMessage,
                    is TimeMessage -> {
                        ""
                    }
                }
            }
        }
        val messageState = when (messageDetail.state) {
            MessageState.Success -> {
                ""
            }

            MessageState.Sending -> {
                ImString.MessageSending + " "
            }

            is MessageState.Failed -> {
                ImString.MessageSendFailed + " "
            }
        }
        senderName + messageState + lastMessage.formatMessage
    }

}

@Stable
enum class ConversationType {
    C2C,
    Group;
}
 fun ConversationType.toBridgeValue(): String {
    return when (this) {
        ConversationType.C2C -> "c2c"
        ConversationType.Group -> "group"
    }
}
