package com.wally.demo.kuiklywallychat.chat.base


import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime


object TimeFormatter {

    fun formatTimeYMDHMS(
        milliseconds: Long,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val value = localDateTime(milliseconds, timeZone)
        return buildString {
            append(value.year)
            append("年")
            append(value.monthNumber)
            append("月")
            append(value.dayOfMonth)
            append("日 ")
            append(twoDigits(value.hour))
            append(":")
            append(twoDigits(value.minute))
        }
    }

    fun formatConversationTime(
        milliseconds: Long,
        nowMilliseconds: Long = Clock.System.now().toEpochMilliseconds(),
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val messageTime = localDateTime(milliseconds, timeZone)
        val nowTime = localDateTime(nowMilliseconds, timeZone)
        val dayDistance = messageTime.date.daysUntil(nowTime.date)

        return when {
            dayDistance == 0 -> {
                formatHourMinute(messageTime.hour, messageTime.minute)
            }

            dayDistance == 1 -> {
                ImString.Yesterday
            }

            dayDistance == 2 -> {
                ImString.DayBeforeYesterday
            }

            messageTime.year == nowTime.year && dayDistance in 3..6 -> {
                formatWeekDay(messageTime.dayOfWeek)
            }

            messageTime.year == nowTime.year -> {
                "${messageTime.monthNumber}月${messageTime.dayOfMonth}日"
            }

            else -> {
                "${messageTime.year}年${messageTime.monthNumber}月${messageTime.dayOfMonth}日"
            }
        }
    }

    fun formatMessageTime(
        milliseconds: Long,
        nowMilliseconds: Long = Clock.System.now().toEpochMilliseconds(),
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val messageTime = localDateTime(milliseconds, timeZone)
        val nowTime = localDateTime(nowMilliseconds, timeZone)
        val dayDistance = messageTime.date.daysUntil(nowTime.date)
        val hourMinute = formatHourMinute(
            hour = messageTime.hour,
            minute = messageTime.minute,
        )

        return when {
            dayDistance == 0 -> {
                hourMinute
            }

            dayDistance == 1 -> {
                "${ImString.Yesterday} $hourMinute"
            }

            dayDistance == 2 -> {
                "${ImString.DayBeforeYesterday} $hourMinute"
            }

            messageTime.year == nowTime.year && dayDistance in 3..6 -> {
                "${formatWeekDay(messageTime.dayOfWeek)} $hourMinute"
            }

            messageTime.year == nowTime.year -> {
                "${messageTime.monthNumber}月${messageTime.dayOfMonth}日 $hourMinute"
            }

            else -> {
                "${messageTime.year}年${messageTime.monthNumber}月${messageTime.dayOfMonth}日 $hourMinute"
            }
        }
    }

    private fun localDateTime(
        milliseconds: Long,
        timeZone: TimeZone,
    ) = Instant
        .fromEpochMilliseconds(milliseconds)
        .toLocalDateTime(timeZone)

    private fun formatHourMinute(
        hour: Int,
        minute: Int,
    ): String {
        return "${twoDigits(hour)}:${twoDigits(minute)}"
    }

    private fun twoDigits(value: Int): String {
        return value.toString().padStart(length = 2, padChar = '0')
    }

    private fun formatWeekDay(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
            else -> "未知"
        }
    }
}