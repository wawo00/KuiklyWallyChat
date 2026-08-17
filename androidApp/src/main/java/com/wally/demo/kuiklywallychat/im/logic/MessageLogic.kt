package com.wally.demo.kuiklywallychat.im.logic

import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.wally.demo.kuiklywallychat.chat.base.model.Chat
import com.wally.demo.kuiklywallychat.chat.base.model.LoadMessageResult
import com.wally.demo.kuiklywallychat.chat.base.model.Message
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeMap
import com.wally.demo.kuiklywallychat.ext.log
import com.wally.demo.kuiklywallychat.im.ImRuntime
import com.wally.demo.kuiklywallychat.im.logic.AccountLogic.failure
import com.wally.demo.kuiklywallychat.im.logic.AccountLogic.success
import com.wally.demo.timsdk.base.proxy.Converters
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.xml.transform.sax.TransformerHandler

object MessageLogic {

    fun loadHistory(
        params: String?,
        callback: KuiklyRenderCallback?,
    ) {

        "MessageLogic call loadHistory".log()
        val json = parseParams(
            params = params,
            callback = callback,
        ) ?: return

        val chat = decodeChat(
            json = json,
            callback = callback,
        ) ?: return

        /*
         * 暂时只取首次历史消息。
         * 分页游标问题见下文说明。
         */
        ImRuntime.AppCoroutineScope.launch {
            try {
                when (
                    val result =
                        ImRuntime.messageRepos
                            .getHistoryMessage(
                                chat = chat,
                                lastMsg = null,
                            )
                ) {
                    is LoadMessageResult.Success -> {
                        "MessageLogic call loadHistory success".log()

                        callback?.invoke(
                            success(
                                mapOf(
                                    "items" to result.messageList.map {
                                        it.toBridgeMap()
                                    },
                                    "isLoadFinished" to
                                            result.isLoadFinished,
                                ),
                            ),
                        )
                    }

                    is LoadMessageResult.Failed -> {
                        "MessageLogic call loadHistory fail".log()
                        callback?.invoke(
                            failure(
                                code = -1,
                                message = result.reason,
                            ),
                        )
                    }
                }
            } catch (throwable: Throwable) {
                callback?.invoke(
                    failure(
                        code = -1,
                        message = throwable.message
                            ?: "加载历史消息失败",
                    ),
                )
            }
            "MessageLogic call loadHistory done".log()

        }
    }

    fun sendText(
        params: String?,
        callback: KuiklyRenderCallback?,
    ) {
        val json = parseParams(
            params = params,
            callback = callback,
        ) ?: return

        val chat = decodeChat(
            json = json,
            callback = callback,
        ) ?: return

        val text = json
            .optString("text")
            .trim()

        if (text.isEmpty()) {
            callback?.invoke(
                failure(
                    code = -1,
                    message = "消息内容不能为空",
                ),
            )
            return
        }

        ImRuntime.AppCoroutineScope.launch {
            try {
                val messageChannel =
                    ImRuntime.messageRepos.sendText(
                        chat = chat,
                        text = text,
                    )

                /*
                 * sendText 的 Channel 会依次产生：
                 * 1. Sending 状态的本地临时消息
                 * 2. Success 或 Failed 状态的最终消息
                 *
                 * 暂时收集为一个列表，并且只回调一次。
                 */
                val messages = buildList {
                    for (message in messageChannel) {
                        add(message.toBridgeMap())
                    }
                }

                callback?.invoke(
                    success(
                        mapOf(
                            "items" to messages,
                        ),
                    ),
                )
            } catch (throwable: Throwable) {
                callback?.invoke(
                    failure(
                        code = -1,
                        message = throwable.message
                            ?: "发送文本消息失败",
                    ),
                )
            }
        }
    }

    fun sendImage(
        params: String?,
        callback: KuiklyRenderCallback?,
    ) {
        val json = parseParams(
            params = params,
            callback = callback,
        ) ?: return

        val chat = decodeChat(
            json = json,
            callback = callback,
        ) ?: return

        val imagePath =
            json.optString("imagePath")

        if (imagePath.isBlank()) {
            callback?.invoke(
                failure(
                    code = -1,
                    message = "图片路径不能为空",
                ),
            )
            return
        }

        ImRuntime.AppCoroutineScope.launch {
            try {
                val messageChannel = ImRuntime.messageRepos.sendImage(chat = chat, imagePath = imagePath,)

                val messages = buildList {
                    for (message in messageChannel) {
                        add(message.toBridgeMap())
                    }
                }

                callback?.invoke(
                    success(
                        mapOf(
                            "items" to messages,
                        ),
                    ),
                )
            } catch (throwable: Throwable) {
                callback?.invoke(
                    failure(
                        code = -1,
                        message = throwable.message
                            ?: "发送图片消息失败",
                    ),
                )
            }
        }
    }

    private fun parseParams(
        params: String?,
        callback: KuiklyRenderCallback?,
    ): JSONObject? {
        return try {
            JSONObject(params.orEmpty().ifBlank { "{}" })
        } catch (throwable: Throwable) {
            callback?.invoke(
                failure(
                    code = -400,
                    message = "消息参数格式错误",
                ),
            )
            null
        }
    }

    private fun decodeChat(
        json: JSONObject,
        callback: KuiklyRenderCallback?,
    ): Chat? {
        val chatId = json
            .optString("chatId")
            .trim()

        if (chatId.isEmpty()) {
            callback?.invoke(
                failure(
                    code = -400,
                    message = "chatId 不能为空",
                ),
            )
            return null
        }

        return when (json.optString("chatType")) {
            "c2c" -> Chat.C2C(chatId)
            "group" -> Chat.Group(chatId)

            else -> {
                callback?.invoke(
                    failure(
                        code = -400,
                        message = "不支持的聊天类型",
                    ),
                )
                null
            }
        }
    }

    fun cleanUnread(params: String?, callback: KuiklyRenderCallback?) {
        try {
            val json = parseParams(
                params = params,
                callback = callback,
            ) ?: throw IllegalArgumentException("解析参数失败：params 为空或格式错误")

            val chat = decodeChat(
                json = json,
                callback = callback,
            ) ?: throw IllegalArgumentException("chat 转换失败")



            ImRuntime.AppCoroutineScope.launch {
                val result = runCatching {
                    ImRuntime.messageRepos.cleanUnreadMessageCount(chat)
                }


                // 通过 isSuccess 或 isFailure 判断
                if (result.isSuccess) {
                    callback?.invoke(
                        success(
                            mapOf(
                                "result" to true,
                            ),
                        ),
                    )
                } else {
                    val error = result.exceptionOrNull()
                    callback?.invoke(
                        failure(
                            code = -1,
                            message = error?.message
                                ?: "清除已读消息失败",
                            ),
                    )
                }
            }
        } catch (throwable: Throwable) {
            callback?.invoke(
                failure(
                    code = -1,
                    message = throwable.message
                        ?: "清除已读消息失败",
                ),
            )
        }


    }
}