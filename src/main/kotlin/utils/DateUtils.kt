package utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val YYYYMMDDHHmm = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")

    fun zeroPad(number: Int): String {
        return if (number > 9) number.toString() else "0$number"
    }

    fun toYYYMMDDWithTime(lastTime: Long): String {
        return toYYYMMDDWithTime(Date(lastTime))
    }

    fun toYYYMMDDWithTime(date: Date): String {
        return YYYYMMDDHHmm.format(date)!!
    }

}