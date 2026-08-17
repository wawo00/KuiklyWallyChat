package com.wally.demo.kuiklywallychat.chat.im
/**
  * @author Wally(25054984)
  * @since 2026/7/27
  * @email wanlei@haier.com
  * @desciption 约束 kuikly和原生层直接传递的消息
  */
object ImEvent {
    const val LoginStateChanged = "im.loginState.changed"
    const val ConnectionChanged = "im.connection.changed"
    const val UserSigExpired = "im.userSig.expired"
    const val KickedOffline = "im.kickedOffline"
    const val SelfProfileChanged = "im.selfProfile.changed"

    const val ConversationsChanged = "im.conversations.changed"
    const val UnreadCountChanged = "im.unreadCount.changed"

    const val MessageReceived = "im.message.received"
    const val MessageUpdated = "im.message.updated"

    const val FriendsChanged = "im.friends.changed"
    const val GroupsChanged = "im.groups.changed"
}