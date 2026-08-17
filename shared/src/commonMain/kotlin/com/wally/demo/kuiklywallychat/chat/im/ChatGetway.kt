package com.wally.demo.kuiklywallychat.chat.im

import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.base.model.LoadMessageResult
import com.wally.demo.kuiklywallychat.chat.base.model.Message


interface ChatGateway {

    fun loadHistory(
        chat: Chat,
        lastMessage: Message?,
        callback: (ImResult<LoadMessageResult>) -> Unit,
    )

    fun sendText(
        chat: Chat,
        text: String,
        callback: (ImResult<List<Message>>) -> Unit,
    )

    fun sendImage(
        chat: Chat,
        imagePath: String,
        callback: (ImResult<List<Message>>) -> Unit,
    )

    fun cleanUnread(
        chat: Chat,
        callback: (ImResult<Unit>) -> Unit,
    )
}