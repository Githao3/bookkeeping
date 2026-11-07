package com.bookkeeping.app.ui.common

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatHelper {
    @androidx.compose.runtime.Composable
    fun currency(amount: Long): String = if (LocalHideAmounts.current) "¥••••" else formatCurrency(amount)
    val LocalHideAmounts = androidx.compose.runtime.staticCompositionLocalOf { false }
    fun formatCurrency(amount: Long): String = currencyFormat.format(java.math.BigDecimal.valueOf(amount, 2))
    fun formatAmount(amount: Long): String = amountOnlyFormat.format(java.math.BigDecimal.valueOf(amount, 2))
    private val currencyFormat = DecimalFormat("¥#,##0.00")
    private val amountOnlyFormat = DecimalFormat("#,##0.00")

    fun formatDayHeader(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val today = Calendar.getInstance()

        val isSameYear = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        val isSameDay = isSameYear && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)

        val prefix = when {
            isSameDay -> "今天"
            isYesterday -> "昨天"
            else -> ""
        }

        val weekDay = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "周日"
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            else -> ""
        }

        val dateStr = SimpleDateFormat("MM月dd日", Locale.CHINA).format(Date(timestamp))
        return if (prefix.isNotEmpty()) "$dateStr $prefix · $weekDay" else "$dateStr $weekDay"
    }

    fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(timestamp))
    }

    fun formatMonth(timestamp: Long): String {
        return SimpleDateFormat("yyyy年MM月", Locale.CHINA).format(Date(timestamp))
    }
}
