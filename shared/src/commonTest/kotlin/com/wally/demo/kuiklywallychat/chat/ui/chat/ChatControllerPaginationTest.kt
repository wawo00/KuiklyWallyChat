package com.wally.demo.kuiklywallychat.chat.ui.chat

import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.base.model.LoadMessageResult
import com.wally.demo.kuiklywallychat.chat.base.model.Message
import com.wally.demo.kuiklywallychat.chat.base.model.MessageDetail
import com.wally.demo.kuiklywallychat.chat.base.model.MessageState
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.TextMessage
import com.wally.demo.kuiklywallychat.chat.im.ChatGateway
import com.wally.demo.kuiklywallychat.chat.im.ImResult
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatControllerPaginationTest {

    @Test
    fun loadingSecondPageDoesNotCreateDuplicateLazyColumnKeys() {
        val gateway = FakeChatGateway(
            pages = mutableListOf(
                listOf(message("m1", 300_000L), message("m2", 200_000L)),
                listOf(message("m3", 100_000L), message("m4", 0L)),
            ),
        )
        val controller = ChatController(
            gateway = gateway,
            chat = Chat.C2C("user-1"),
        )
        controller.onLoadMoreMessage()
        controller.onLoadMoreMessage()

        val keys = controller.chatPageViewState.messageList.map { it.detail.msgId }
        assertEquals(keys.distinct().size, keys.size, "LazyColumn keys must be unique")
    }

    private fun message(id: String, milliseconds: Long): Message = TextMessage(
        messageDetail = MessageDetail(
            msgId = id,
            nativeMessageId = id,
            milliseconds = milliseconds,
            state = MessageState.Success,
            sender = PersonProfile.Empty,
            isOwnMessage = false,
        ),
        text = id,
    )

    private class FakeChatGateway(
        private val pages: MutableList<List<Message>>,
    ) : ChatGateway {
        override fun loadHistory(
            chat: Chat,
            lastMessage: Message?,
            callback: (ImResult<LoadMessageResult>) -> Unit,
        ) {
            callback(
                ImResult.Success(
                    LoadMessageResult.Success(
                        messageList = pages.removeAt(0),
                        isLoadFinished = pages.isEmpty(),
                    ),
                ),
            )
        }

        override fun sendText(
            chat: Chat,
            text: String,
            callback: (ImResult<List<Message>>) -> Unit,
        ) = Unit

        override fun sendImage(
            chat: Chat,
            imagePath: String,
            callback: (ImResult<List<Message>>) -> Unit,
        ) = Unit

        override fun cleanUnread(
            chat: Chat,
            callback: (ImResult<Unit>) -> Unit,
        ) = callback(ImResult.Success(Unit))
    }
}


