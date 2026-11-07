package com.bookkeeping.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.BuildConfig
import com.bookkeeping.app.data.repository.*
import com.bookkeeping.app.ui.components.GlassAlertDialog
import com.bookkeeping.app.ui.components.GlassDialogAction
import com.bookkeeping.app.ui.components.GlassDivider
import com.bookkeeping.app.ui.components.GlassIconButton
import com.bookkeeping.app.ui.components.GlassListRow
import com.bookkeeping.app.ui.components.GlassListSection
import com.bookkeeping.app.ui.components.GlassProgressIndicator
import com.bookkeeping.app.ui.components.glassAccent
import com.bookkeeping.app.ui.components.glassSwitchColors
import com.bookkeeping.app.ui.components.inkPrimary
import com.bookkeeping.app.ui.components.inkSecondary
import com.bookkeeping.app.ui.components.inkTertiary
import com.bookkeeping.app.ui.theme.LiquidGlassTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(transactionCount: Int, accountCount: Int, repository: BookkeepingRepository,
    hideAmounts: Boolean, onHideAmounts: (Boolean) -> Unit,
    themeMode: String, onThemeChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    var busy by remember { mutableStateOf(false) }
    var showThemePage by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var preview by remember { mutableStateOf<BackupSnapshot?>(null) }
    var recoveryPath by rememberSaveable { mutableStateOf<String?>(null) }
    fun operation(action: suspend () -> String) {
        if (busy) return
        busy = true; message = null
        scope.launch {
            try { message = withContext(Dispatchers.IO) { action() } }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { message = "操作失败：${e.message ?: "请重试"}" }
            finally { busy = false }
        }
    }
    fun export(uri: Uri, csv: Boolean) = operation {
        val snapshot = repository.snapshot()
        val text = if (csv) BackupCodec.csv(snapshot) else BackupCodec.encode(snapshot)
        val output = context.contentResolver.openOutputStream(uri, "wt") ?: error("无法打开保存文件")
        output.bufferedWriter(Charsets.UTF_8).use { it.write(text) }
        if (csv) "已导出 ${snapshot.transactions.size} 笔账单" else "完整备份已保存：${snapshot.accounts.size} 个账户、${snapshot.categories.size} 个分类、${snapshot.transactions.size} 笔账单"
    }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri -> if (uri != null) export(uri, true) }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) export(uri, false) }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) operation {
            val input = context.contentResolver.openInputStream(uri) ?: error("无法打开备份文件")
            val bytes = input.use { stream ->
                val buffer = java.io.ByteArrayOutputStream()
                val chunk = ByteArray(8192)
                while (true) {
                    val count = stream.read(chunk)
                    if (count < 0) break
                    require(buffer.size() + count <= 20 * 1024 * 1024) { "备份文件超过20MB" }
                    buffer.write(chunk, 0, count)
                }
                buffer.toByteArray()
            }
            require(bytes.size <= 20 * 1024 * 1024) { "备份文件超过20MB" }
            val snapshot = BackupCodec.decode(bytes.toString(Charsets.UTF_8))
            withContext(Dispatchers.Main) { preview = snapshot }
            "备份已验证，请核对后恢复"
        }
    }
    var recoveryExportPath by rememberSaveable { mutableStateOf<String?>(null) }
    val recoveryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) operation {
            val path = recoveryExportPath ?: error("恢复前备份不存在")
            val text = File(path).readText(Charsets.UTF_8)
            val output = context.contentResolver.openOutputStream(uri, "wt") ?: error("无法保存文件")
            output.bufferedWriter(Charsets.UTF_8).use { it.write(text) }
            "恢复前备份已另存"
        }
    }
    fun filename(ext: String) = "CoinFlow-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA).format(Date())}.$ext"

    val inkPrimary = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
    val inkSecondary = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
    val inkTertiary = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight

    AnimatedContent(
        targetState = showThemePage,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
            } else {
                (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "settingsNav"
    ) { onThemePage ->
        if (onThemePage) {
            // 外观子页面
            Column(modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconButton(Icons.Default.ChevronLeft, "返回", onClick = { showThemePage = false })
                    Text(
                        "外观",
                        fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        color = inkPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.size(40.dp))
                }
                Spacer(Modifier.height(6.dp))
                GlassListSection(shape = RoundedCornerShape(20.dp), elevation = 6.dp) {
                    ThemeOptionRow("跟随系统", themeMode == "system") { onThemeChange("system") }
                    GlassDivider()
                    ThemeOptionRow("亮色", themeMode == "light") { onThemeChange("light") }
                    GlassDivider()
                    ThemeOptionRow("暗色", themeMode == "dark") { onThemeChange("dark") }
                }
            }
        } else {
        Column(modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── 数据与安全 ──
            item {
                Text("数据与安全", Modifier.padding(start = 6.dp, top = 14.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = inkSecondary)
            }
            item {
                GlassListSection(shape = RoundedCornerShape(20.dp), elevation = 6.dp) {
                    GlassListRow(
                        title = "隐藏金额",
                        subtitle = "隐藏明细、统计和资产中的金额",
                        icon = Icons.Filled.VisibilityOff,
                        onClick = { onHideAmounts(!hideAmounts) },
                        trailing = { Switch(hideAmounts, onCheckedChange = { onHideAmounts(it) }, colors = glassSwitchColors()) }
                    )
                    GlassDivider(startIndent = 58.dp)
                    GlassListRow(
                        title = "账单导出（CSV）",
                        subtitle = "导出全部时间的流水，供查看和交换，不能用于完整恢复",
                        icon = Icons.Filled.FileDownload,
                        onClick = { csvLauncher.launch(filename("csv")) },
                        enabled = !busy
                    )
                    GlassDivider(startIndent = 58.dp)
                    GlassListRow(
                        title = "完整备份（JSON）",
                        subtitle = "包含账户、分类、账单、余额和转账关联",
                        icon = Icons.Filled.Backup,
                        onClick = { backupLauncher.launch(filename("json")) },
                        enabled = !busy
                    )
                    GlassDivider(startIndent = 58.dp)
                    GlassListRow(
                        title = "从备份恢复",
                        subtitle = "恢复会替换当前数据，恢复前自动保留一份本机备份",
                        icon = Icons.Filled.Restore,
                        onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                        enabled = !busy
                    )
                }
            }
            if (busy) {
                item {
                    Column(Modifier.padding(horizontal = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        GlassProgressIndicator()
                        Text("正在处理…", fontSize = 12.sp, color = inkTertiary)
                    }
                }
            }
            message?.let { msg ->
                item { Text(msg, fontSize = 13.sp, color = glassAccent(), modifier = Modifier.padding(horizontal = 6.dp)) }
            }
            recoveryPath?.let { path ->
                item {
                    GlassListSection(shape = RoundedCornerShape(20.dp), elevation = 4.dp) {
                        GlassListRow(
                            title = "另存恢复前备份",
                            subtitle = "恢复前备份已保留，可另存到文件",
                            icon = Icons.Filled.SaveAlt,
                            onClick = {
                                recoveryExportPath = path
                                recoveryLauncher.launch(filename("json"))
                            },
                            enabled = !busy
                        )
                    }
                }
            }
            // ── 显示 ──
            item {
                Text("显示", Modifier.padding(start = 6.dp, top = 12.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = inkSecondary)
            }
            item {
                GlassListSection(shape = RoundedCornerShape(20.dp), elevation = 4.dp) {
                    GlassListRow(
                        title = "外观",
                        icon = Icons.Filled.DarkMode,
                        onClick = { showThemePage = true },
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(themeLabel(themeMode), fontSize = 13.sp, color = inkSecondary)
                                Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = inkTertiary)
                            }
                        }
                    )
                }
            }
            // ── 关于应用 ──
            item {
                Text("关于应用", Modifier.padding(start = 6.dp, top = 12.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = inkSecondary)
            }
            item {
                GlassListSection(shape = RoundedCornerShape(20.dp), elevation = 4.dp) {
                    GlassListRow(
                        title = "CoinFlow 简记账",
                        subtitle = "v${BuildConfig.VERSION_NAME}（${BuildConfig.BUILD_TYPE}）· 版本号 ${BuildConfig.VERSION_CODE}",
                        icon = Icons.Filled.Info
                    )
                }
            }
        }
        }
        }
    }

    preview?.let { snapshot -> GlassAlertDialog(onDismissRequest = { if (!busy) preview = null }, title = { Text("恢复完整备份？") },
        text = { Text("备份包含 ${snapshot.accounts.size} 个账户、${snapshot.categories.size} 个分类、${snapshot.transactions.size} 笔账单。确认后替换当前数据，原数据会先保存为本机备份。") },
        confirmButton = { GlassDialogAction("确认恢复", enabled = !busy, emphasized = true, onClick = { operation {
            val file = File(context.filesDir, "before-restore-${System.currentTimeMillis()}.json")
            repository.restore(snapshot) { previous -> file.writeText(BackupCodec.encode(previous), Charsets.UTF_8) }
            withContext(Dispatchers.Main) { preview = null; recoveryPath = file.absolutePath }
            "恢复成功，余额和关联已保留"
        } }) }, dismissButton = { GlassDialogAction("取消", onClick = { preview = null }, enabled = !busy) }) }
}

private fun themeLabel(mode: String) = when (mode) {
    "light" -> "亮色"; "dark" -> "暗色"; else -> "跟随系统"
}

/** 外观主题单选行：右侧小圆圈，选中亮起。 */
@Composable
private fun ThemeOptionRow(title: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) inkPrimary() else inkSecondary()
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .then(
                    if (selected) Modifier.background(glassAccent())
                    else Modifier.border(1.5.dp, inkTertiary().copy(alpha = 0.6f), CircleShape)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Color.White))
            }
        }
    }
}
