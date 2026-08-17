package com.wally.demo.kuiklywallychat.chat.im

import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.base.model.GroupMemberProfile
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.base.model.LoadMessageResult
import com.wally.demo.kuiklywallychat.chat.base.model.Message
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.WallyConversation
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeType
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_ConversationId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_GroupId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendProfile
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_PinConversation
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_Remark
import com.wally.demo.kuiklywallychat.ext.logNative
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @author Wally(25054984)
 * @since 2026/7/27
 * @email wanlei@haier.com
 * @desciption 参考当前工程 BridgeModule.kt写的通过回调实现双向通讯的实现类,目前仅仅实现了登录相关的功能(ImGateWay)
 */
class ImModule : Module(), FriendshipGateway, AccountGateway, ConversationGateway, ChatGateway, FriendProfileGateway, GroupProfileGateway {

    override fun moduleName(): String = MODULE_NAME

    override fun restoreSession(
        callback: (ImResult<RestoredSession>) -> Unit,
    ) {
        callNative(ImMethod.RestoreSession, JSONObject()) { result ->
            callback(
                result!!.toImResult { data ->
                    RestoredSession(
                        userId = data.optString("userId"),
                        canAutoLogin = data.optBoolean("canAutoLogin"),
                    )
                },
            )
        }
    }

    override fun login(
        userId: String,
        callback: (ImResult<Unit>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put("userId", userId)
        }
        callNative(ImMethod.Login, params) { result ->
            callback(result!!.toImResult { Unit })
        }
    }

    override fun logout(callback: (ImResult<String>) -> Unit) {
        callNative(ImMethod.Logout, JSONObject()) { result ->
            callback(result!!.toImResult { data ->
                data.optString("userId")
            })
        }
    }


    /**
     * 调用原生方法
     * @description:名字可能有点歧义，其实调用的是平台特定方法
     */
    private fun callNative(
        method: String,
        params: JSONObject,
        callback: CallbackFn,
    ) {
        toNative(
            false,
            method,
            params.toString(),
            callback,
            false,
        )
    }

    override fun loadFriends(callback: (ImResult<List<PersonProfile>>) -> Unit) {
        callNative(ImMethod.LoadFriends, JSONObject()) { result ->
//            callback(result!!.toImResult { Unit })
            if (result == null) {
                callback(ImResult.Failure(-1, "加载好友列表失败：原生层未返回数据"))
            } else {
                callback(
                    result.toImResult { data ->
                        val items = data.optJSONArray("items") ?: JSONArray() // 约定数据结构师通过items进行传递
                        ImJsonCodec.decodeProfiles(items)
                    }
                )
            }

        }
    }

    override fun loadJoinedGroups(callback: (ImResult<List<GroupProfile>>) -> Unit) {
        callNative(ImMethod.LoadJoinedGroups, JSONObject()) { result ->
            if (result == null) {
                callback(ImResult.Failure(-1, "加载群列表失败：原生层未返回数据"))
            } else {
                callback(
                    result.toImResult { data ->
                        ImJsonCodec.decodeGroupProfiles(data.optJSONArray("items") ?: JSONArray())
                    }
                )
            }

        }
    }


    override fun joinGroup(
        groupId: String,
        callback: (ImResult<Unit>) -> Unit,
    ) {

        val params = JSONObject().apply {
            put(PARAM_GroupId, groupId)
        }
        callNative(
            method = ImMethod.JoinGroup,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层没返回任何内容",
                    ),
                )
                return@callNative
            }
            callback(result.toImResult { Unit })
        }
    }

    override fun getSelfProfile(
        callback: (ImResult<PersonProfile>) -> Unit,
    ) {
        callNative(
            method = ImMethod.GetSelfProfile,
            params = JSONObject(),
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "获取用户资料失败：原生层未返回数据",
                    ),
                )
                return@callNative
            }

            callback(
                result.toImResult { data ->
                    val profileJson = data.optJSONObject("profile")
                        ?: JSONObject()

                    ImJsonCodec.decodePersonProfile(profileJson)
                },
            )
        }
    }


    // ----- conversation ---
    override fun loadConversations(callback: (ImResult<List<WallyConversation>>) -> Unit) {
        Utils.logToNative("执行了loadConversations")
        callNative(ImMethod.LoadConversations, JSONObject()) { result ->
//            callback(result!!.toImResult { Unit })
            if (result == null) {
                callback(ImResult.Failure(-1, "conversation列表加载失败：原生层未返回数据"))
            } else {
                callback(
                    result.toImResult { data ->
                        ImJsonCodec.decodeConversations(
                            data.optJSONArray("items")
                                ?: JSONArray(),
                        )
                    }
                )
            }

        }
    }

    override fun deleteC2CConversation(conversationId: String, callback: (ImResult<Unit>) -> Unit) {

        val params = JSONObject().apply {
            put(PARAM_ConversationId, conversationId)
        }

        callNative(
            method = ImMethod.DeleteC2CConversation,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层未返回发送结果",
                    ),
                )
                return@callNative
            }
            callback(result.toImResult { Unit })
        }
    }

    override fun deleteGroupConversation(
        conversationId: String,
        callback: (ImResult<Unit>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put(PARAM_ConversationId, conversationId)
        }

        callNative(
            method = ImMethod.DeleteGroupConversation,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层未返回发送结果",
                    ),
                )
                return@callNative
            }
            callback(result.toImResult { Unit })
        }
    }

    override fun pinConversation(
        conversationId: String,
        pin: Boolean,
        callback: (ImResult<Unit>) -> Unit,
    ) {

        val params = JSONObject().apply {
            put(PARAM_ConversationId, conversationId)
            put(PARAM_PinConversation, pin)
        }

        callNative(
            method = ImMethod.PinConversation,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层未返回发送结果",
                    ),
                )
                return@callNative
            }
            callback(result.toImResult { Unit })
        }
    }

    override fun refreshTotalUnreadMessageCount(callback: (ImResult<Long>) -> Unit) {

        callNative(
            method = ImMethod.PinConversation,
            params = JSONObject(),
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层未返回发送结果",
                    ),
                )
                return@callNative
            }
            callback( result.toImResult { data -> data.optLong("nums")})
        }
    }

    override fun loadHistory(
        chat: Chat,
        lastMessage: Message?,
        callback: (ImResult<LoadMessageResult>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put("chatId", chat.id)
            put("chatType", chat.toBridgeType())

            if (lastMessage != null) {
                put(
                    "lastNativeMessageId",
                    lastMessage.detail.nativeMessageId,
                )
            }
        }
        "callnative LoadHistory ".logNative()
        callNative(
            method = ImMethod.LoadHistory,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层未返回历史消息",
                    ),
                )
                return@callNative
            }

            callback(
                result.toImResult { data ->
                    val messages =
                        ImJsonCodec.decodeMessages(
                            data.optJSONArray("items")
                                ?: JSONArray(),
                        )

                    LoadMessageResult.Success(
                        messageList = messages,
                        isLoadFinished =
                            data.optBoolean(
                                "isLoadFinished",
                            ),
                    )
                },
            )
        }
    }

    override fun sendText(
        chat: Chat,
        text: String,
        callback: (ImResult<List<Message>>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put("chatId", chat.id)
            put("chatType", chat.toBridgeType())
            put("text", text)
        }

        callNative(
            method = ImMethod.SendText,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层未返回发送结果",
                    ),
                )
                return@callNative
            }

            callback(
                result.toImResult { data ->
                    ImJsonCodec.decodeMessages(
                        data.optJSONArray("items")
                            ?: JSONArray(),
                    )
                },
            )
        }
    }

    override fun sendImage(
        chat: Chat,
        imagePath: String,
        callback: (ImResult<List<Message>>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put("chatId", chat.id)
            put("chatType", chat.toBridgeType())
            put("imagePath", imagePath)
        }

        callNative(
            method = ImMethod.SendImage,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层未返回发送结果",
                    ),
                )
                return@callNative
            }

            callback(
                result.toImResult { data ->
                    ImJsonCodec.decodeMessages(
                        data.optJSONArray("items")
                            ?: JSONArray(),
                    )
                },
            )
        }
    }

    override fun cleanUnread(
        chat: Chat,
        callback: (ImResult<Unit>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put("chatId", chat.id)
            put("chatType", chat.toBridgeType())
        }

        callNative(
            method = ImMethod.CleanUnread,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层没返回任何内容",
                    ),
                )
                return@callNative
            }
            callback(result.toImResult { Unit })
        }
    }

    //friendProfile

    override fun LoadFriendProfile(
        friendId: String,
        callback: (ImResult<PersonProfile>) -> Unit,
    ) {

        val params = JSONObject().apply {
            put(PARAM_FriendId, friendId)
        }
        callNative(
            method = ImMethod.LoadFriendProfile,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层没返回任何内容",
                    ),
                )
                return@callNative
            }

            callback(
                result.toImResult { data ->
                    val profileJson = data.optJSONObject(PARAM_FriendProfile)
                        ?: JSONObject()

                    ImJsonCodec.decodePersonProfile(profileJson)
                },

                )
        }
    }


    override fun setFriendRemark(
        friendId: String,
        remark: String,
        callback: (ImResult<Unit>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put(PARAM_FriendId, friendId)
            put(PARAM_Remark, remark)
        }

        callNative(
            method = ImMethod.SetFriendRemark,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层没返回任何内容",
                    ),
                )
                return@callNative
            }
            callback(result.toImResult { Unit })
        }
    }

    override fun addFriend(
        friendId: String,
        callback: (ImResult<Unit>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put(PARAM_FriendId, friendId)
        }

        callNative(
            method = ImMethod.AddFriend,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层没返回任何内容",
                    ),
                )
                return@callNative
            }
            callback(result.toImResult { Unit })
        }
    }

    override fun deleteFriend(
        friendId: String,
        callback: (ImResult<Unit>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put(PARAM_FriendId, friendId)
        }

        callNative(
            method = ImMethod.DeleteFriend,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层没返回任何内容",
                    ),
                )
                return@callNative
            }
            callback(result.toImResult { Unit })
        }
    }

    override fun loadGroupProfileByCallback(
        groupId: String,
        callback: (ImResult<GroupProfile>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put(PARAM_GroupId, groupId)
        }
        callNative(
            method = ImMethod.LoadGroupProfile,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层没返回任何内容",
                    ),
                )
                return@callNative
            }

            callback(
                result.toImResult { data ->
                    val profileJson = data.optJSONObject(PARAM_FriendProfile)
                        ?: JSONObject()

                    ImJsonCodec.decodeGroupProfile(profileJson)
                }
            )
        }
    }


    override fun loadGroupMembersByCallback(
        groupId: String,
        callback: (ImResult<List<GroupMemberProfile>>) -> Unit,
    ) {
        val params = JSONObject().apply {
            put(PARAM_GroupId, groupId)
        }

        callNative(
            method = ImMethod.LoadGroupMembers,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层未返回发送结果",
                    ),
                )
                return@callNative
            }

            callback(
                result.toImResult { data ->
                    ImJsonCodec.decodeGroupMemberProfiles(
                        data.optJSONArray("items")
                            ?: JSONArray(),
                    )
                },
            )
        }
    }


    // 将同步回调包装为挂起函数
    override suspend fun loadGroupMembers(
        groupId: String,
    ): ImResult<List<GroupMemberProfile>> =
        suspendCancellableCoroutine { continuation ->
            loadGroupMembersByCallback(groupId) { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        }


    override suspend fun loadGroupProfile(
        groupId: String,
    ): ImResult<GroupProfile> =
        suspendCancellableCoroutine { continuation ->
            loadGroupProfileByCallback(groupId) { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        }


    override fun quitGroup(groupId: String, callback: (ImResult<Unit>) -> Unit) {
        val params = JSONObject().apply {
            put(PARAM_GroupId, groupId)
        }

        callNative(
            method = ImMethod.QuitGroup,
            params = params,
        ) { result ->
            if (result == null) {
                callback(
                    ImResult.Failure(
                        code = -1,
                        message = "原生层没返回任何内容",
                    ),
                )
                return@callNative
            }
            callback(result.toImResult { Unit })
        }
    }

    companion object {
        const val MODULE_NAME = "KRImModule" //对应 androidapp\im\bridge\KRImModule.kt
    }


}