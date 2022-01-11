package com.tyehooney.myvideos.common.util

import com.tyehooney.myvideos.common.Constants.FORMAT_DATE
import com.tyehooney.myvideos.common.Constants.FORMAT_DAYS_AGO
import com.tyehooney.myvideos.common.Constants.FORMAT_HOURS_AGO
import com.tyehooney.myvideos.common.Constants.FORMAT_MINUTES_AGO
import com.tyehooney.myvideos.common.Constants.FORMAT_MONTHS_AGO
import com.tyehooney.myvideos.common.Constants.FORMAT_YEARS_AGO
import com.tyehooney.myvideos.common.Constants.LAST_MONTH
import com.tyehooney.myvideos.common.Constants.LAST_YEAR
import com.tyehooney.myvideos.common.Constants.NOW
import com.tyehooney.myvideos.common.Constants.YESTERDAY
import java.text.SimpleDateFormat
import java.util.*

val videoDateFormat get() = SimpleDateFormat(FORMAT_DATE, Locale.getDefault())

fun setCreatedAtText(currentTimestamp: Long, postTimestamp: Long): String {

    val currentTime = Calendar.getInstance().apply { timeInMillis = currentTimestamp }
    val postTime = Calendar.getInstance().apply { timeInMillis = postTimestamp }

    getTimeDiff(
        currentTime,
        postTime,
        Calendar.YEAR,
        LAST_YEAR,
        FORMAT_YEARS_AGO
    )?.let { return it }

    getTimeDiff(
        currentTime,
        postTime,
        Calendar.MONTH,
        LAST_MONTH,
        FORMAT_MONTHS_AGO
    )?.let { return it }

    getTimeDiff(
        currentTime,
        postTime,
        Calendar.DAY_OF_MONTH,
        YESTERDAY,
        FORMAT_DAYS_AGO
    )?.let { return it }

    val hourDiff = currentTime.get(Calendar.HOUR_OF_DAY) - postTime.get(Calendar.HOUR_OF_DAY)
    if (hourDiff > 0) return String.format(FORMAT_HOURS_AGO, hourDiff)

    val minuteDiff = currentTime.get(Calendar.MINUTE) - postTime.get(Calendar.MINUTE)
    return if (minuteDiff < 3) NOW else String.format(FORMAT_MINUTES_AGO, minuteDiff)
}

private fun getTimeDiff(
    current: Calendar,
    past: Calendar,
    scope: Int,
    strLast: String,
    strFormat: String
): String? {
    val diff = current.get(scope) - past.get(scope)
    return when {
        diff == 1 -> strLast
        diff > 1 -> String.format(strFormat, diff)
        else -> null
    }
}