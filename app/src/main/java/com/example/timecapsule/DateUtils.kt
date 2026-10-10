package com.example.timecapsule

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatDateTime(millis: Long): String =
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))

    fun formatDate(millis: Long): String =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

    fun formatDuration(durationMillis: Long): String {
        var remaining = durationMillis / 1000
        val days = remaining / 86400; remaining %= 86400
        val hours = remaining / 3600; remaining %= 3600
        val minutes = remaining / 60
        val seconds = remaining % 60
        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    fun countdownText(unlockDate: Long): String {
        val diff = unlockDate - System.currentTimeMillis()
        return if (diff <= 0) "Ready to open!" else "Opens in ${formatDuration(diff)}"
    }
}