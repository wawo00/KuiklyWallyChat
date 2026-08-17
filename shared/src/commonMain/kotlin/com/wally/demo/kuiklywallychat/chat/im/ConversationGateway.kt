package com.wally.demo.kuiklywallychat.chat.im

import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.WallyConversation

interface ConversationGateway {
    fun loadConversations(
        callback: (ImResult<List<WallyConversation>>) -> Unit,
    )

    fun deleteC2CConversation(
        conversationId: String,
        callback: (ImResult<Unit>) -> Unit,
    )
    fun deleteGroupConversation(
        conversationId: String,
        callback: (ImResult<Unit>) -> Unit,
    )
    fun pinConversation(
        conversationId: String,
        pin: Boolean,
        callback: (ImResult<Unit>) -> Unit,
    )
    fun refreshTotalUnreadMessageCount(
        callback: (ImResult<Long>) -> Unit,
    )
}

/**
 *
 * 数据结构、
 *
 * 文本：
 * {
 *   "type": "text",
 *   "detail": {
 *     "msgId": "msg_001",
 *     "nativeMessageId": "native_001",
 *     "milliseconds": 1720000,
 *     "state": {
 *       "type": "success"
 *     },
 *     "sender": {
 *       "id": "user_001",
 *       "avatarUrl": "https://example.com/avatar.png",
 *       "nickname": "Wally",
 *       "remark": "",
 *       "signature": "Hello",
 *       "addTime": 0,
 *       "isFriend": false
 *     },
 *     "isOwnMessage": false
 *   },
 *   "text": "你好"
 * }
 *
 * 图片
 * {
 *   "type": "image",
 *   "detail": {
 *     "...": "..."
 *   },
 *   "original": {
 *     "width": 1080,
 *     "height": 1920,
 *     "url": "https://example.com/original.png"
 *   },
 *   "large": {
 *     "width": 720,
 *     "height": 1280,
 *     "url": "https://example.com/large.png"
 *   },
 *   "thumb": {
 *     "width": 180,
 *     "height": 320,
 *     "url": "https://example.com/thumb.png"
 *   }
 * }
 */