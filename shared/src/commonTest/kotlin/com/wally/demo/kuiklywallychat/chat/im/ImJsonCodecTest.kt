package com.wally.demo.kuiklywallychat.chat.im

import com.wally.demo.kuiklywallychat.chat.base.model.MessageDetail
import com.wally.demo.kuiklywallychat.chat.base.model.MessageState
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.TextMessage
import kotlin.test.Test
import kotlin.test.assertEquals

class ImJsonCodecTest {

    @Test
    fun textMsgRoundTrip() {

    val source = TextMessage(
        messageDetail = MessageDetail(
            msgId = "local-1",
            nativeMessageId = "native-1",
            milliseconds = 1000L,
            state = MessageState.Success,
            sender = PersonProfile.Empty,
            isOwnMessage = true,
        ),
        text = "hello",
    )

    val result = ImJsonCodec.decodeMessage(
        ImJsonCodec.encodeMessage(source),
    )

    assertEquals(source, result)

    }
}


