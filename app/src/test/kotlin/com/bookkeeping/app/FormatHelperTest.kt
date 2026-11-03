package com.bookkeeping.app

import com.bookkeeping.app.ui.common.FormatHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatHelperTest {

    @Test
    fun testFormatCurrency() {
        val result = FormatHelper.formatCurrency(1234.56)
        assertTrue(result.contains("1,234.56"))
    }

    @Test
    fun testFormatAmount() {
        val result = FormatHelper.formatAmount(50.0)
        assertEquals("50.00", result)
    }

    @Test
    fun testFormatDayHeader() {
        val now = System.currentTimeMillis()
        val header = FormatHelper.formatDayHeader(now)
        assertTrue(header.contains("今天"))
    }
}
