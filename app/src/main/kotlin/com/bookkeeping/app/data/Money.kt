package com.bookkeeping.app.data

import java.math.BigDecimal
import java.math.RoundingMode

/** All persisted amounts are cents. Input never silently rounds. */
object Money {
    const val MAX_CENTS = 999_999_999_999L
    private val number = Regex("[0-9]+(?:\\.[0-9]{1,2})?")

    fun parse(text: String, allowNegative: Boolean = false): Long {
        val value = text.trim()
        val unsigned = if (allowNegative && value.startsWith("-")) value.drop(1) else value
        require(number.matches(unsigned)) { "请输入有效金额，最多两位小数" }
        val cents = BigDecimal(value).movePointRight(2).longValueExact()
        require(cents in -MAX_CENTS..MAX_CENTS) { "金额超出范围" }
        return cents
    }

    fun evaluate(expression: String): Long {
        require(expression.length in 1..80) { "表达式过长或为空" }
        val tokens = Regex("[0-9]+(?:\\.[0-9]{1,2})?|[+-]").findAll(expression).map { it.value }.toList()
        require(tokens.joinToString("") == expression && tokens.size % 2 == 1) { "请补全金额表达式" }
        var total = parse(tokens.first())
        for (i in 1 until tokens.size step 2) {
            require(tokens[i] == "+" || tokens[i] == "-") { "金额表达式无效" }
            val cents = parse(tokens[i + 1])
            total = if (tokens[i] == "+") Math.addExact(total, cents) else Math.subtractExact(total, cents)
        }
        require(total in -MAX_CENTS..MAX_CENTS) { "金额超出范围" }
        return total
    }

    fun key(input: String, key: String): String {
        if (key == "DEL") return input.dropLast(1).ifEmpty { "0" }
        if (key == "C") return "0"
        if (input.length >= 80) return input
        if (key in listOf("+", "-")) {
            if (input.last() in "+-") return input.dropLast(1) + key
            if (input.endsWith(".")) return input + "0" + key
            return input + key
        }
        val token = input.split('+', '-').last()
        if (key == ".") return if ('.' in token) input else input + if (token.isEmpty()) "0." else "."
        if (key.length != 1 || !key[0].isDigit()) return input
        if ('.' in token && token.substringAfter('.').length >= 2) return input
        if ('.' !in token && token.length >= 10) return input
        return if (input == "0") key else input + key
    }

    fun decimal(cents: Long): String = BigDecimal.valueOf(cents, 2).toPlainString()
    fun legacy(value: Double): Long = BigDecimal.valueOf(value).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
}
