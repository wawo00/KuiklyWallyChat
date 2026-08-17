package com.wally.demo.kuiklywallychat.chat.base.model

import androidx.compose.runtime.Stable
import com.wally.demo.kuiklywallychat.chat.base.ImString
import com.wally.demo.kuiklywallychat.chat.base.TimeFormatter

@Stable
sealed class MessageState {

    @Stable
    data object Sending : MessageState()

    @Stable
    data class Failed(val reason: String) : MessageState()

    @Stable
    data object Success : MessageState()

}

@Stable
data class MessageDetail(
    val msgId: String,
    val nativeMessageId: String = msgId,
    val milliseconds: Long,
    val state: MessageState,
    val sender: PersonProfile,
    val isOwnMessage: Boolean,
) {

    val conversationTime = TimeFormatter.formatConversationTime(milliseconds = milliseconds)

}

@Stable
sealed class Message(val detail: MessageDetail) {
    abstract val formatMessage: String
//    var tag:String="" 用于分页

}

@Stable
data class TextMessage(
    private val messageDetail: MessageDetail,
    val text: String,
) : Message(detail = messageDetail) {

    override val formatMessage: String
        get() = text

}

@Stable
data class ImageMessage(
    val messageDetail: MessageDetail,
    val original: ImageElement,
    val large: ImageElement?,
    val thumb: ImageElement?,
) : Message(detail = messageDetail) {

    @Stable
    data class ImageElement(
        val width: Int,
        val height: Int,
        val url: String,
    )

    override val formatMessage: String
        get() = ImString.MessageImage

    val previewImage: ImageElement
        get() = large ?: original

    val previewImageUrl: String
        get() = previewImage.url

}

private fun ImageMessage.ImageElement.toBridgeMap():
        Map<String, Any?> {
    return mapOf(
        "width" to width,
        "height" to height,
        "url" to url,
    )
}

@Stable
data class SystemMessage(
    private val messageDetail: MessageDetail,
    val tips: String,
) : Message(detail = messageDetail) {

    override val formatMessage: String
        get() = tips

}

@Stable
data class TimeMessage(val targetMessage: Message) : Message(
    detail = MessageDetail(
        msgId = (targetMessage.detail.milliseconds + targetMessage.detail.msgId.hashCode()).toString(),
        nativeMessageId = targetMessage.detail.nativeMessageId,
        milliseconds = targetMessage.detail.milliseconds,
        state = MessageState.Success,
        sender = PersonProfile.Empty,
        isOwnMessage = false
    )
) {

    override val formatMessage = TimeFormatter.formatMessageTime(milliseconds = detail.milliseconds)

}

@Stable
sealed class LoadMessageResult {

    @Stable
    data class Success(
        val messageList: List<Message>,
        val isLoadFinished: Boolean,
    ) : LoadMessageResult()

    @Stable
    data class Failed(val reason: String) : LoadMessageResult()

}
private fun MessageState.toBridgeMap(): Map<String, Any?> {
    return when (this) {
        MessageState.Sending -> {
            mapOf(
                "type" to "sending",
            )
        }

        MessageState.Success -> {
            mapOf(
                "type" to "success",
            )
        }

        is MessageState.Failed -> {
            mapOf(
                "type" to "failed",
                "reason" to reason,
            )
        }
    }
}

private fun MessageDetail.toBridgeMap(): Map<String, Any?> {
    return mapOf(
        "msgId" to msgId,
        "nativeMessageId" to nativeMessageId,
        "milliseconds" to milliseconds,
        "state" to state.toBridgeMap(),
        "sender" to sender.toBridgeMap(),
        "isOwnMessage" to isOwnMessage,
    )
}

/**
 * 用于bean->json object传递数据到kuikly层
 */
fun Message.toBridgeMap(): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>(
        "detail" to detail.toBridgeMap(),
    )

    when (this) {
        is TextMessage -> {
            result["type"] = "text"
            result["text"] = text
        }

        is ImageMessage -> {
            result["type"] = "image"
            result["original"] = original.toBridgeMap()
            result["large"] = large?.toBridgeMap()
            result["thumb"] = thumb?.toBridgeMap()
        }

        is SystemMessage -> {
            result["type"] = "system"
            result["tips"] = tips
        }

        is TimeMessage -> {
            result["type"] = "time"
            result["targetMessage"] =
                targetMessage.toBridgeMap()
        }
    }

    return result
}