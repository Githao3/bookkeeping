package com.bookkeeping.app.data.repository

import com.bookkeeping.app.data.Money
import com.bookkeeping.app.data.local.entity.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupCodec {
    private fun array(items: List<JSONObject>) = JSONArray().apply { items.forEach { put(it) } }
    fun encode(data: BackupSnapshot): String {
        data.validate()
        return JSONObject().put("format", "CoinFlow").put("version", 1).put("unit", "CNY-cent")
            .put("accounts", array(data.accounts.map { a -> JSONObject().put("id", a.id).put("name", a.name).put("type", a.type.name)
                .put("balance", a.balance).put("iconName", a.iconName).put("colorHex", a.colorHex).put("included", a.isIncludedInTotal).put("archived", a.isArchived) }))
            .put("categories", array(data.categories.map { c -> JSONObject().put("id", c.id).put("name", c.name).put("type", c.type.name)
                .put("iconName", c.iconName).put("colorHex", c.colorHex).put("sortOrder", c.sortOrder) }))
            .put("transactions", array(data.transactions.map { t -> JSONObject().put("id", t.id).put("amount", t.amount).put("type", t.type.name)
                .put("categoryId", t.categoryId ?: JSONObject.NULL).put("sourceAccountId", t.sourceAccountId)
                .put("targetAccountId", t.targetAccountId ?: JSONObject.NULL).put("timestamp", t.timestamp).put("note", t.note) })).toString(2)
    }
    private fun JSONObject.long(key: String): Long {
        val value = get(key)
        require(value is Int || value is Long) { "备份整数格式无效：$key" }
        return (value as Number).toLong()
    }
    private fun JSONObject.optionalLong(key: String) = if (isNull(key)) null else long(key)
    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
    fun decode(text: String): BackupSnapshot {
        val root = JSONObject(text)
        require(root.getString("format") == "CoinFlow" && root.long("version") == 1L && root.getString("unit") == "CNY-cent") { "不支持的备份格式或版本" }
        val data = BackupSnapshot(
            root.getJSONArray("accounts").objects().map { a -> AccountEntity(a.long("id"), a.getString("name"), AccountType.valueOf(a.getString("type")), a.long("balance"), a.getString("iconName"), a.long("colorHex"), a.getBoolean("included"), a.getBoolean("archived")) },
            root.getJSONArray("categories").objects().map { c -> CategoryEntity(c.long("id"), c.getString("name"), TransactionType.valueOf(c.getString("type")), c.getString("iconName"), c.long("colorHex"), Math.toIntExact(c.long("sortOrder"))) },
            root.getJSONArray("transactions").objects().map { t -> TransactionEntity(t.long("id"), t.long("amount"), TransactionType.valueOf(t.getString("type")), t.optionalLong("categoryId"), t.long("sourceAccountId"), t.optionalLong("targetAccountId"), t.long("timestamp"), t.getString("note")) }
        )
        data.validate()
        return data
    }
    fun csv(data: BackupSnapshot): String {
        val accounts = data.accounts.associateBy { it.id }
        val categories = data.categories.associateBy { it.id }
        fun escape(value: String): String {
            // Avoid spreadsheet formula execution, and preserve commas, quotes and newlines.
            val safe = if (value.trimStart().firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')) "'$value" else value
            return "\"${safe.replace("\"", "\"\"")}\""
        }
        return "\uFEFF日期,类型,金额(元),分类,账户,转入账户,备注\r\n" + data.transactions.joinToString("\r\n") { t ->
            listOf(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date(t.timestamp)),
                when(t.type) { TransactionType.EXPENSE -> "支出"; TransactionType.INCOME -> "收入"; TransactionType.TRANSFER -> "转账" },
                Money.decimal(t.amount), categories[t.categoryId]?.name.orEmpty(), accounts[t.sourceAccountId]?.name.orEmpty(),
                accounts[t.targetAccountId]?.name.orEmpty(), t.note).joinToString(",") { escape(it) }
        }
    }
}
