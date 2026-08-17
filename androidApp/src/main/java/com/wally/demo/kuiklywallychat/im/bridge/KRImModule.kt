package com.wally.demo.kuiklywallychat.im.bridge

import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.AddFriend
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.CleanUnread
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.DeleteC2CConversation
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.DeleteFriend
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.DeleteGroupConversation
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.DownLoadImage
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.GetSelfProfile
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.JoinGroup
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.LoadConversations
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.LoadFriendProfile
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.LoadFriends
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.LoadGroupMembers
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.LoadGroupProfile
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.LoadHistory
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.LoadJoinedGroups
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.Login
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.Logout
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.PinConversation
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.QuitGroup
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.RestoreSession
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.SendImage
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.SendText
import com.wally.demo.kuiklywallychat.chat.im.ImMethod.SetFriendRemark

import com.wally.demo.kuiklywallychat.im.logic.AccountLogic
import com.wally.demo.kuiklywallychat.im.logic.ConversationLogic
import com.wally.demo.kuiklywallychat.im.logic.FriendProfileLogic
import com.wally.demo.kuiklywallychat.im.logic.FriendshipLogic
import com.wally.demo.kuiklywallychat.im.logic.GroupLogic
import com.wally.demo.kuiklywallychat.im.logic.LoginLogic
import com.wally.demo.kuiklywallychat.im.logic.LoginLogic.failure
import com.wally.demo.kuiklywallychat.im.logic.LoginLogic.login
import com.wally.demo.kuiklywallychat.im.logic.LoginLogic.logout
import com.wally.demo.kuiklywallychat.im.logic.MessageLogic

/**
 * @author Wally(25054984)
 * @since 2026/7/27
 * @email wanlei@haier.com
 * @desciption 平台和kuikly的桥阶层
 *1. KRImModule 只解析参数、调用 Repository、编码结果；
 *2.SDK 细节全部留在 Repository；
 *3.每条调用最多触发一次 callback；
 *4.所有异常转换成统一失败结构，不能让异常穿透到 Kuikly；
 * 5.不在 Module 中创建新的 SDK 单例或 CoroutineScope。
 */

/**
 * 数据结构统一:
 * {
 * "success": true,
 * "code": 0,
 * "message": "",
 * "data": {}
 * }
 * 失败示例：
 *
 * {
 * "success": false,
 * "code": 70001,
 * "message": "UserSig 已过期",
 * "data": {}
 * }
 * restoreSession 成功示例：
 *
 * {
 * "success": true,
 * "code": 0,
 * "message": "",
 * "data": {
 * "userId": "user_001",
 * "canAutoLogin": true
 * }
 * }
 */
class KRImModule : KuiklyRenderBaseModule() {
    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        return when (method) {
            RestoreSession -> LoginLogic.restoreSession(callback)
            Login -> login(params, callback)
            Logout -> logout(callback)
            LoadFriends -> FriendshipLogic.loadFriends(callback)
            LoadJoinedGroups -> FriendshipLogic.loadJoinedGroups(callback)
            AddFriend -> FriendshipLogic.addFriend(params, callback)
            JoinGroup -> FriendshipLogic.joinGroup(params, callback)
            GetSelfProfile -> AccountLogic.getSelfProfile(params, callback)
            LoadConversations -> ConversationLogic.LoadConversations(params, callback)
            DeleteGroupConversation -> ConversationLogic.deleteGroupConversation(params, callback)
            DeleteC2CConversation -> ConversationLogic.deleteC2CConversation(params, callback)
            PinConversation -> ConversationLogic.pinConversation(params, callback)
            LoadHistory ->
                MessageLogic.loadHistory(
                    params,
                    callback,
                )

            SendText ->
                MessageLogic.sendText(
                    params,
                    callback,
                )

            SendImage ->
                MessageLogic.sendImage(
                    params,
                    callback,
                )

            CleanUnread ->
                MessageLogic.cleanUnread(
                    params,
                    callback,
                )

            LoadFriendProfile -> FriendProfileLogic.LoadFriendProfile(params, callback)
            DeleteFriend -> FriendProfileLogic.deleteFriend(params, callback)
            SetFriendRemark -> FriendProfileLogic.setFriendRemark(params, callback)
            LoadGroupProfile -> GroupLogic.loadGroupProfile(params, callback)
            LoadGroupMembers -> GroupLogic.loadGroupMembers(params, callback)
            QuitGroup -> GroupLogic.quitGroup(params, callback)
            DownLoadImage -> GroupLogic.quitGroup(params, callback)

            else -> callback?.invoke(failure(-404, "未知 IM 方法: $method"))
        }
    }


    companion object {
        const val MODULE_NAME = "KRImModule"
    }
}