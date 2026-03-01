package com.block.goose.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object ClipboardUtil {
    fun copyToClipboard(context: Context, text: String, label: String = "Copied text"): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun copyMessageToClipboard(context: Context, message: String) {
        if (copyToClipboard(context, message, "Goose message")) {
            Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}

object TimeUtil {
    fun formatTimestamp(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        val hours = ChronoUnit.HOURS.between(instant, now)
        val days = ChronoUnit.DAYS.between(instant, now)

        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
                    .withZone(ZoneId.systemDefault())
                formatter.format(instant)
            }
        }
    }

    fun formatDetailedTimestamp(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochMilli(timestamp))
    }

    fun formatTimeOnly(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochMilli(timestamp))
    }
}

object RetryUtil {
    fun calculateDelay(attempt: Int, baseDelayMs: Long = 1000, maxDelayMs: Long = 300000): Long {
        // Exponential backoff with jitter
        val exponentialDelay = baseDelayMs * Math.pow(2.0, attempt.toDouble()).toLong()
        val jitter = (Math.random() * 1000).toLong()
        return minOf(exponentialDelay + jitter, maxDelayMs)
    }
}
