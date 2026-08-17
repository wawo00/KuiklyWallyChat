package com.wally.demo.kuiklywallychat.chat.im

import com.tencent.kuikly.compose.ui.input.key.Key.Companion.G
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.base.model.ConversationType
import com.wally.demo.kuiklywallychat.chat.base.model.GroupMemberProfile
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.base.model.ImageMessage
import com.wally.demo.kuiklywallychat.chat.base.model.Message
import com.wally.demo.kuiklywallychat.chat.base.model.MessageDetail
import com.wally.demo.kuiklywallychat.chat.base.model.MessageState
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.SystemMessage
import com.wally.demo.kuiklywallychat.chat.base.model.TextMessage
import com.wally.demo.kuiklywallychat.chat.base.model.TimeMessage
import com.wally.demo.kuiklywallychat.chat.base.model.WallyConversation


/**
  * @author Wally(25054984)
  * @since 2026/7/27
  * @email wanlei@haier.com
  * @desciption 用于将native原本json编解码为固定数据结构  json->entity
 * //todo:未来会优化为每个entity的扩展方法
 */

object  ImJsonCodec{

    fun encodeChat(chat: Chat): JSONObject {
        return JSONObject().apply {
            put("id", chat.id)
            put(
                "type",
                when (chat) {
                    is Chat.C2C -> "c2c"
                    is Chat.Group -> "group"
                },
            )
        }
    }

    fun decodeChat(json: JSONObject): Chat {
        val id = json.optString("id")
        return when (json.optString("type")) {
            "c2c" -> Chat.C2C(id)
            "group" -> Chat.Group(id)
            else -> error("Unsupported chat type: ${json.optString("type")}")
        }
    }

    fun encodePersonProfile(profile: PersonProfile): JSONObject {
        return JSONObject().apply {
            put("id", profile.id)
            put("avatarUrl", profile.avatarUrl)
            put("nickname", profile.nickname)
            put("remark", profile.remark)
            put("signature", profile.signature)
            put("addTime", profile.addTime)
            put("isFriend", profile.isFriend)
        }
    }

    /**
     *交互的数据结构
     * {
     *   "success": true,
     *   "code": 0,
     *   "message": "",
     *   "data": {
     *     "profile": {
     *       "id": "user_001",
     *       "avatarUrl": "https://example.com/avatar.png",
     *       "nickname": "Wally",
     *       "remark": "",
     *       "signature": "Hello",
     *       "addTime": 0,
     *       "isFriend": false
     *     }
     *   }
     * }
     */
    fun decodePersonProfile(json: JSONObject): PersonProfile {
        return PersonProfile(
            id = json.optString("id"),
            avatarUrl = json.optString("avatarUrl"),
            nickname = json.optString("nickname"),
            remark = json.optString("remark"),
            signature = json.optString("signature"),
            addTime = json.optLong("addTime"),
            isFriend = json.optBoolean("isFriend"),
        )
    }

    private fun encodeMessageState(state: MessageState): JSONObject {
        return JSONObject().apply {
            when (state) {
                MessageState.Sending -> {
                    put("type", "sending")
                    put("reason", "")
                }

                MessageState.Success -> {
                    put("type", "success")
                    put("reason", "")
                }

                is MessageState.Failed -> {
                    put("type", "failed")
                    put("reason", state.reason)
                }
            }
        }
    }

    private fun decodeMessageState(json: JSONObject): MessageState {
        return when (json.optString("type")) {
            "sending" -> MessageState.Sending
            "success" -> MessageState.Success
            "failed" -> MessageState.Failed(
                reason = json.optString("reason"),
            )

            else -> MessageState.Failed("Unknown message state")
        }
    }

    private fun encodeMessageDetail(detail: MessageDetail): JSONObject {
        return JSONObject().apply {
            put("msgId", detail.msgId)
            put("nativeMessageId", detail.nativeMessageId)
            put("milliseconds", detail.milliseconds)
            put("state", encodeMessageState(detail.state))
            put("sender", encodePersonProfile(detail.sender))
            put("isOwnMessage", detail.isOwnMessage)
        }
    }

    private fun decodeMessageDetail(json: JSONObject): MessageDetail {
        return MessageDetail(
            msgId = json.optString("msgId"),
            nativeMessageId = json.optString("nativeMessageId"),
            milliseconds = json.optLong("milliseconds"),
            state = decodeMessageState(
                json.optJSONObject("state") ?: JSONObject(),
            ),
            sender = decodePersonProfile(
                json.optJSONObject("sender") ?: JSONObject(),
            ),
            isOwnMessage = json.optBoolean("isOwnMessage"),
        )
    }

    private fun encodeImageElement(
        element: ImageMessage.ImageElement?,
    ): JSONObject? {
        if (element == null) {
            return null
        }

        return JSONObject().apply {
            put("width", element.width)
            put("height", element.height)
            put("url", element.url)
        }
    }

    private fun decodeImageElement(
        json: JSONObject?,
    ): ImageMessage.ImageElement? {
        if (json == null) {
            return null
        }

        return ImageMessage.ImageElement(
            width = json.optInt("width"),
            height = json.optInt("height"),
            url = json.optString("url"),
        )
    }

    fun encodeMessage(message: Message): JSONObject {
        return JSONObject().apply {
            put("detail", encodeMessageDetail(message.detail))

            when (message) {
                is TextMessage -> {
                    put("type", "text")
                    put("text", message.text)
                }

                is ImageMessage -> {
                    put("type", "image")
                    put("original", encodeImageElement(message.original))
                    put("large", encodeImageElement(message.large))
                    put("thumb", encodeImageElement(message.thumb))
                }

                is SystemMessage -> {
                    put("type", "system")
                    put("tips", message.tips)
                }

                is TimeMessage -> {
                    put("type", "time")
                    put("targetMessage", encodeMessage(message.targetMessage))
                }

                else -> {

                }
            }
        }
    }
    fun decodeMessage(jsonString: String): Message {
        require(jsonString.isNotBlank()) {
            "Message JSON 不能为空"
        }

        return decodeMessage(
            JSONObject(jsonString),
        )
    }
    fun decodeMessage(json: JSONObject): Message {
        val detail = decodeMessageDetail(
            json.optJSONObject("detail") ?: JSONObject(),
        )

        return when (json.optString("type")) {
            "text" -> TextMessage(
                messageDetail = detail,
                text = json.optString("text"),
            )

            "image" -> ImageMessage(
                messageDetail = detail,
                original = requireNotNull(
                    decodeImageElement(json.optJSONObject("original")),
                ),
                large = decodeImageElement(json.optJSONObject("large")),
                thumb = decodeImageElement(json.optJSONObject("thumb")),
            )

            "system" -> SystemMessage(
                messageDetail = detail,
                tips = json.optString("tips"),
            )

            "time" -> TimeMessage(
                targetMessage = decodeMessage(
                    json.optJSONObject("targetMessage") ?: JSONObject(),
                ),
            )

            else -> SystemMessage(
                messageDetail = detail,
                tips = "Unsupported message",
            )
        }
    }

    fun encodeMessages(messages: List<Message>): JSONArray {
        return JSONArray().apply {
            messages.forEach { put(encodeMessage(it)) }
        }
    }

    fun decodeMessages(array: JSONArray): List<Message> {
        return buildList {
            for (index in 0 until array.length()) {
                add(decodeMessage(array.optJSONObject(index)!!))
            }
        }
    }

    fun decodeProfiles(array: JSONArray): List<PersonProfile> {
        return buildList {
            for (index in 0 until array.length()) {
                add(decodePersonProfile(array.optJSONObject(index)!!))
            }
        }
    }


    fun decodeGroupProfile(json: JSONObject): GroupProfile {
        return GroupProfile(
            id = json.optString("id"),
            avatarUrl = json.optString("avatarUrl"),
            name = json.optString("name"),
            introduction = json.optString("introduction"),
            createTime = json.optLong("createTime"),
        )
    }


    fun decodeGroupMemberProfiles( array: JSONArray): List<GroupMemberProfile> {
        return buildList {
            for( index in 0 until array.length()){
                add(decodeGroupMemberProfile(array.optJSONObject(index)!!))
            }
        }
    }


    fun decodeGroupMemberProfile(json: JSONObject):GroupMemberProfile{
        return GroupMemberProfile(
            detail = decodePersonProfile(json.optJSONObject("detail")!!),
            isOwner = json.optBoolean("isOwner"),
            joinTime = json.optLong("joinTime")
        )
    }


    fun decodeGroupProfiles(array: JSONArray): List<GroupProfile>{
        return buildList {
            for(index in 0 until array.length()){
                val item=array.optJSONObject(index)?:continue
                add(decodeGroupProfile(item))
            }
        }
    }

    fun decodeConversation(
        json: JSONObject,
    ): WallyConversation {
        val lastMessageJson =
            json.optString("lastMessage")

        require(lastMessageJson.isNotBlank()) {
            "会话 ${json.optString("id")} 缺少 lastMessage"
        }

        return WallyConversation(
            id = json.optString("id"),
            name = json.optString("name"),
            avatarUrl = json.optString("avatarUrl"),
            unreadMessageCount =
                json.optLong("unreadMessageCount"),

            lastMessage = decodeMessage(
                lastMessageJson,
            ),

            isPinned = json.optBoolean("isPinned"),

            type = when (json.optString("type")) {
                "c2c" -> ConversationType.C2C
                "group" -> ConversationType.Group

                else -> error(
                    "不支持的会话类型: ${
                        json.optString("type")
                    }",
                )
            },
        )
    }


    fun decodeConversations(
        array: JSONArray,
    ): List<WallyConversation> {
        return buildList {
            for (index in 0 until array.length()) {
                val item =
                    array.optJSONObject(index)
                        ?: continue

                add(
                    decodeConversation(item),
                )
            }
        }
    }

    fun decodeMessageOrNull(
        jsonString: String,
    ): Message? {
        if (jsonString.isBlank()) {
            return null
        }

        return runCatching {
            decodeMessage(
                JSONObject(jsonString),
            )
        }.getOrNull()
    }


}



