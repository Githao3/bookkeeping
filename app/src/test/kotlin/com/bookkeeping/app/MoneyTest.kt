package com.bookkeeping.app

import com.bookkeeping.app.data.Money
import org.junit.Assert.*
import org.junit.Test

class MoneyTest {
    @Test fun mixedOperators() { assertEquals(8500L, Money.evaluate("100-20+5")); assertEquals(11500L, Money.evaluate("100+20-5")) }
    @Test fun decimalIsExact() { assertEquals(30L, Money.evaluate("0.1+0.2")); assertEquals(1L, Money.evaluate("0.03-0.02")) }
    @Test fun repeatedSubtractions() { assertEquals(7500L, Money.evaluate("100-20-5")) }
    @Test fun negativeResultIsPreservedForValidation() { assertEquals(-100L, Money.evaluate("1-2")) }
    @Test fun incompleteAndInvalidExpressionsRejected() {
        listOf("1+", "1-", "1+-2", "1++2", "1.234", "1..2", "NaN", "Infinity", "", "-1", "1 2", "1.").forEach {
            assertThrows("must reject $it", IllegalArgumentException::class.java) { Money.evaluate(it) }
        }
    }
    @Test fun operatorReplacesPreviousOperator() { assertEquals("100-", Money.key("100+", "-")) }
    @Test fun deleteAndClear() { assertEquals("1", Money.key("12", "DEL")); assertEquals("0", Money.key("1", "DEL")); assertEquals("0", Money.key("12+3", "C")) }
    @Test fun decimalKeyboardLimitsPrecision() { assertEquals("1.23", Money.key("1.23", "4")); assertEquals("1.2", Money.key("1.2", ".")); assertEquals("1+0.", Money.key("1+", ".")) }
    @Test fun decimalAtOperatorCompletedExplicitly() { assertEquals("1.0+", Money.key("1.", "+")) }
    @Test fun balancesCanBeNegative() { assertEquals(-123L, Money.parse("-1.23", true)) }
    @Test fun amountLimits() { assertEquals(Money.MAX_CENTS, Money.parse("9999999999.99")); assertThrows(IllegalArgumentException::class.java) { Money.parse("10000000000") } }
    @Test fun legacyRoundingAndDisplay() { assertEquals(101L, Money.legacy(1.005)); assertEquals(-101L, Money.legacy(-1.005)); assertEquals("0.01", Money.decimal(1)) }
}
