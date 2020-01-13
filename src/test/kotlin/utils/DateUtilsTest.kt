package utils

import org.junit.Assert.assertEquals
import org.junit.Test

internal class DateUtilsTest {

    @Test
    fun zeroPad() {
        assertEquals(DateUtils.zeroPad(1), "01")
        assertEquals(DateUtils.zeroPad(11), "11")
    }

    @Test
    fun toYYYMMDDWithTime() {
        assertEquals(DateUtils.toYYYMMDDWithTime(1577863800000), "2020-01-01 13:00")
    }
}