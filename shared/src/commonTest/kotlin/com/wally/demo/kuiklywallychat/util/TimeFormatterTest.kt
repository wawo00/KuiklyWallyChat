package com.wally.demo.kuiklywallychat.chat.base

import com.wally.demo.kuiklywallychat.chat.base.TimeFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class TimeFormatterTest {

    private val timeZone = TimeZone.UTC

    @Test
    fun formatTimeYMDHMS_usesOriginalYearMonthDayHourMinutePattern() {
        assertEquals(
            expected = "2026年7月24日 09:05",
            actual = TimeFormatter.formatTimeYMDHMS(
                milliseconds = epoch(2026, 7, 24, 9, 5),
                timeZone = timeZone,
            ),
        )
    }

    @Test
    fun formatConversationTime_preservesOriginalDateBoundaries() {
        val now = epoch(2026, 7, 24, 12, 0)

        assertEquals("09:05", TimeFormatter.formatConversationTime(epoch(2026, 7, 24, 9, 5), now, timeZone))
        assertEquals("昨天", TimeFormatter.formatConversationTime(epoch(2026, 7, 23, 9, 5), now, timeZone))
        assertEquals("前天", TimeFormatter.formatConversationTime(epoch(2026, 7, 22, 9, 5), now, timeZone))
        assertEquals("周一", TimeFormatter.formatConversationTime(epoch(2026, 7, 20, 9, 5), now, timeZone))
        assertEquals("7月1日", TimeFormatter.formatConversationTime(epoch(2026, 7, 1, 9, 5), now, timeZone))
        assertEquals("2025年12月31日", TimeFormatter.formatConversationTime(epoch(2025, 12, 31, 9, 5), now, timeZone))
    }

    @Test
    fun formatMessageTime_preservesOriginalDateBoundaries() {
        val now = epoch(2026, 7, 24, 12, 0)

        assertEquals("09:05", TimeFormatter.formatMessageTime(epoch(2026, 7, 24, 9, 5), now, timeZone))
        assertEquals("昨天 09:05", TimeFormatter.formatMessageTime(epoch(2026, 7, 23, 9, 5), now, timeZone))
        assertEquals("前天 09:05", TimeFormatter.formatMessageTime(epoch(2026, 7, 22, 9, 5), now, timeZone))
        assertEquals("周一 09:05", TimeFormatter.formatMessageTime(epoch(2026, 7, 20, 9, 5), now, timeZone))
        assertEquals("7月1日 09:05", TimeFormatter.formatMessageTime(epoch(2026, 7, 1, 9, 5), now, timeZone))
        assertEquals("2025年12月31日 09:05", TimeFormatter.formatMessageTime(epoch(2025, 12, 31, 9, 5), now, timeZone))
    }

    private fun epoch(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        return LocalDateTime(year, month, day, hour, minute).toInstant(timeZone).toEpochMilliseconds()
    }
}

