package com.wally.demo.kuiklywallychat.chat.base


object ImString {

    const val MessageImage = "[图片]"
    const val MessageSending = "[发送中]"
    const val MessageSendFailed = "[发送失败]"
    const val MessageUnsupported = "[不支持的消息类型]"

    const val Yesterday = "昨天"
    const val DayBeforeYesterday = "前天"

    const val LoginUserIdRequired = "请输入 UserId"
    const val CannotAddSelf = "不能添加自己为好友"
    const val SetRemarkFailed = "设置失败"
    const val UpdateFailed = "更新失败"

    fun loadMessageError(
        code: Int,
        description: String?,
    ): String {
        return "code: $code desc: ${description.orEmpty()}"
    }

    fun unsupportedMessage(type: String): String {
        return "$MessageUnsupported - $type"
    }

    fun unsupportedGroupTip(type: String): String {
        return "[不支持的系统消息] - $type"
    }

    fun groupMemberJoined(name: String): String {
        return "${name}加入了群聊"
    }

    fun groupMemberQuit(name: String): String {
        return "${name}退出群聊"
    }

    fun groupMemberKicked(name: String): String {
        return "${name}被踢出群聊"
    }

    fun groupMemberSetAdmin(name: String): String {
        return "${name}成为管理员"
    }

    fun groupMemberCancelAdmin(name: String): String {
        return "${name}被取消管理员身份"
    }

    fun groupInfoChanged(name: String): String {
        return "${name}修改了群资料"
    }

    fun groupMemberInfoChanged(name: String): String {
        return "${name}修改了群成员资料"
    }
}