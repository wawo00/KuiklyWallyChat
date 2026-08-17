package com.wally.demo.kuiklywallychat.chat.im
/**
  * @author Wally(25054984)
  * @since 2026/7/27
  * @email wanlei@haier.com
  * @desciption 对其kuikly和native间的方法名称
  */
object ImMethod {

    const val RestoreSession = "restoreSession"
    const val Login = "login"
    const val Logout = "logout"
    const val GetSelfProfile = "getSelfProfile"
    const val UpdateSelfProfile = "updateSelfProfile"

    const val LoadConversations = "loadConversations"
    const val PinConversation = "pinConversation"
    const val DeleteConversation = "deleteConversation"
    const val DeleteC2CConversation = "deleteC2CConversation"
    const val DeleteGroupConversation = "deleteGroupConversation"

    const val RefreshTotalUnreadMessageCount="refreshTotalUnreadMessageCount"
    const val CleanUnread = "cleanUnread"

    const val LoadHistory = "loadHistory"
    const val SendText = "sendText"
    const val SendImage = "sendImage"

    const val LoadFriends = "loadFriends"
    const val LoadFriendProfile = "loadFriendProfile"
    const val AddFriend = "addFriend"
    const val DeleteFriend = "deleteFriend"
    const val SetFriendRemark = "setFriendRemark"
    const val LoadJoinedGroups = "loadJoinedGroups"
    const val LoadGroupProfile = "loadGroupProfile"
    const val LoadGroupMembers = "loadGroupMembers"
    const val JoinGroup = "joinGroup"
    const val QuitGroup = "quitGroup"

    const val PickImages = "pickImages"
    const val PreviewImages = "previewImages"
    const val DownLoadImage = "DownLoadImage"
}