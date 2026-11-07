package com.bookkeeping.app.ui.add

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.mutableIntStateOf
import kotlin.math.roundToInt
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.Money
import com.bookkeeping.app.data.local.entity.*
import com.bookkeeping.app.ui.common.FormatHelper
import com.bookkeeping.app.ui.common.IconHelper
import com.bookkeeping.app.ui.components.GlassAlertDialog
import com.bookkeeping.app.ui.components.GlassDialogAction
import com.bookkeeping.app.ui.components.GlassDivider
import com.bookkeeping.app.ui.components.GlassIconButton
import com.bookkeeping.app.ui.components.GlassSegmented
import com.bookkeeping.app.ui.components.NumberPad
import com.bookkeeping.app.ui.components.accountTypeTint
import com.bookkeeping.app.ui.components.glassAccent
import com.bookkeeping.app.ui.components.hairline
import com.bookkeeping.app.ui.components.inkPrimary
import com.bookkeeping.app.ui.components.inkSecondary
import com.bookkeeping.app.ui.components.inkTertiary
import com.bookkeeping.app.ui.components.insetTint
import com.bookkeeping.app.ui.components.panelFill
import com.bookkeeping.app.ui.main.TransactionDraft
import com.bookkeeping.app.ui.theme.LiquidGlassTokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 记一笔面板（iCost/Moze 一屏结构 × 玻璃语言）：
 * 大金额居中 + 分类横滑 + 账户下拉/日期时间胶囊 + 无框备注行 + 玻璃数字键盘。
 * 金额表达式引擎（Money.key/evaluate）与草稿模型保持不变。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionContent(accounts: List<AccountEntity>, expenseCategories: List<CategoryEntity>, incomeCategories: List<CategoryEntity>,
    draft: TransactionDraft, onDraftChange: (TransactionDraft) -> Unit, saving: Boolean, saveError: String?,
    onSaveTransaction: (TransactionEntity) -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val categories = if (draft.type == TransactionType.EXPENSE) expenseCategories else incomeCategories
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("preferences", Context.MODE_PRIVATE) }
    val lastUsedAccountId = remember { prefs.getLong("lastAccountId", 0L) }
    var showDateTime by rememberSaveable { mutableStateOf(false) }
    var noteFocused by remember { mutableStateOf(false) }
    // 金额输入激活态：与备注焦点互斥，同一时刻只有一个光标亮、只有一种键盘
    var amountActive by rememberSaveable { mutableStateOf(true) }
    var inputError by remember { mutableStateOf<String?>(null) }
    val focus = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()
    val accent = glassAccent()
    // 账户默认值：草稿 > 上次使用的账户 > 列表第一个
    val source = draft.sourceId.takeIf { it != 0L }
        ?: accounts.firstOrNull { it.id == lastUsedAccountId }?.id
        ?: accounts.firstOrNull()?.id ?: 0L
    val target = draft.targetId.takeIf { it != 0L } ?: accounts.firstOrNull { it.id != source }?.id ?: 0L
    val category = draft.categoryId ?: categories.firstOrNull()?.id
    fun complete() {
        try {
            val amount = Money.evaluate(draft.amount)
            require(amount > 0) { "金额须大于零" }
            require(accounts.any { it.id == source }) { "请选择有效的账户" }
            require(draft.type != TransactionType.TRANSFER || target != source && accounts.any { it.id == target }) { "转账需要两个不同的有效账户" }
            require(draft.type == TransactionType.TRANSFER || categories.any { it.id == category }) { "请选择分类" }
            inputError = null
            onSaveTransaction(TransactionEntity(draft.id, amount, draft.type,
                if (draft.type == TransactionType.TRANSFER) null else category, source,
                if (draft.type == TransactionType.TRANSFER) target else null, draft.timestamp, draft.note.trim()))
        } catch (e: Exception) { inputError = e.message }
    }

    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // 抓手条
        Box(
            Modifier
                .padding(top = 8.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(insetTint())
                .align(Alignment.CenterHorizontally)
        )
        // 头部：✕ + 类型分段（按类型着色）
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassIconButton(Icons.Default.Close, "关闭", onClick = onDismiss, enabled = !saving)
            val typeAccent = when (draft.type) {
                TransactionType.EXPENSE -> LiquidGlassTokens.ExpenseRed
                TransactionType.INCOME -> LiquidGlassTokens.IncomeGreen
                TransactionType.TRANSFER -> Color(0xFF6366F1)
            }
            GlassSegmented(
                options = listOf("支出", "收入", "转账"),
                selectedIndex = TransactionType.entries.indexOf(draft.type),
                onSelect = {
                    onDraftChange(draft.copy(type = TransactionType.entries[it], categoryId = null))
                    inputError = null
                },
                modifier = Modifier.weight(1f),
                accentColor = typeAccent
            )
        }

        // 金额大字居中 + 闪烁光标 + 表达式求值预览
        val caretAlpha by rememberInfiniteTransition(label = "caret").animateFloat(
            initialValue = 1f, targetValue = 0.12f,
            animationSpec = infiniteRepeatable(tween(650, easing = LinearEasing), RepeatMode.Reverse),
            label = "caretAlpha"
        )
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .fillMaxWidth()
                    // 点金额数字 → 唤回数字键盘（备注失焦、输入法收起）
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !saving
                    ) {
                        amountActive = true
                        focus.clearFocus()
                    }
                    .horizontalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¥", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = inkSecondary())
                    Text(
                        draft.amount,
                        fontSize = 38.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp,
                        color = inkPrimary(), maxLines = 1
                    )
                    Box(
                        Modifier
                            .padding(start = 3.dp)
                            .width(2.5.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                accent.copy(
                                    alpha = when {
                                        saving -> 0.2f
                                        amountActive && !noteFocused -> caretAlpha
                                        else -> 0f
                                    }
                                )
                            )
                    )
                }
            }
            if (draft.amount.any { it == '+' || it == '-' }) {
                val evaluated = runCatching { Money.evaluate(draft.amount) }.getOrNull()
                if (evaluated != null && evaluated > 0) {
                    Text(
                        "= ${FormatHelper.formatCurrency(evaluated)}",
                        fontSize = 14.sp,
                        color = inkSecondary(),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // 中部内容区（自适应高度；极端大字体时内部滚动兜底）
        Column(
            Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 分类横滑行（转账无分类）
            if (draft.type != TransactionType.TRANSFER) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = category == cat.id
                        val tint = Color(cat.colorHex)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) tint.copy(alpha = if (isDark) 0.28f else 0.14f) else insetTint())
                                    .border(
                                        if (selected) 1.5.dp else 0.6.dp,
                                        if (selected) tint else hairline(),
                                        CircleShape
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        enabled = !saving
                                    ) { onDraftChange(draft.copy(categoryId = cat.id)); inputError = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getCategoryIcon(cat.iconName),
                                    contentDescription = cat.name,
                                    tint = tint.copy(alpha = if (selected) 1f else 0.8f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                cat.name,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) inkPrimary() else inkSecondary()
                            )
                        }
                    }
                }
            }

            // 信息胶囊行：账户下拉 / 转出+转入下拉 / 日期时间
            if (draft.type == TransactionType.TRANSFER) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountMenuChip(
                        fallbackLabel = "转出账户",
                        candidates = accounts,
                        selectedId = source,
                        enabled = !saving,
                        modifier = Modifier.weight(1f)
                    ) { picked ->
                        onDraftChange(draft.copy(sourceId = picked.id, targetId = if (picked.id == target) 0L else target))
                    }
                    AccountMenuChip(
                        fallbackLabel = "转入账户",
                        candidates = accounts.filter { it.id != source },
                        selectedId = target,
                        enabled = !saving,
                        modifier = Modifier.weight(1f)
                    ) { picked ->
                        onDraftChange(draft.copy(targetId = picked.id))
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    MetaChip(Icons.Default.CalendarMonth, dateTimeLabel(draft.timestamp), Modifier.weight(1f), enabled = !saving) { showDateTime = true }
                }
                if (target == 0L) Text("请添加另一个账户，不能转入同一账户", color = LiquidGlassTokens.ExpenseRed, fontSize = 12.sp)
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountMenuChip(
                        fallbackLabel = "选择账户",
                        candidates = accounts,
                        selectedId = source,
                        enabled = !saving,
                        modifier = Modifier.weight(1.2f)
                    ) { picked ->
                        prefs.edit().putLong("lastAccountId", picked.id).apply()
                        onDraftChange(draft.copy(sourceId = picked.id, targetId = if (picked.id == target) 0L else target))
                    }
                    MetaChip(Icons.Default.CalendarMonth, dateTimeLabel(draft.timestamp), Modifier.weight(1f), enabled = !saving) { showDateTime = true }
                }
            }

            // 备注行：仅上下发丝线，无边框无底色，左右与上面对齐；占位与光标共用同一文字度量保证对齐
            val noteStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, color = inkPrimary())
            GlassDivider(startIndent = 0.dp, endIndent = 0.dp)
            BasicTextField(
                value = draft.note,
                onValueChange = { if (it.length <= 2000) onDraftChange(draft.copy(note = it)) },
                enabled = !saving,
                textStyle = noteStyle,
                cursorBrush = SolidColor(accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        noteFocused = it.isFocused
                        // 备注失焦后自然回到金额键盘
                        amountActive = !it.isFocused
                    },
                decorationBox = { innerTextField ->
                    Box(Modifier.padding(vertical = 12.dp)) {
                        if (draft.note.isEmpty()) {
                            Text("备注", style = noteStyle.copy(color = inkTertiary()), maxLines = 1)
                        }
                        innerTextField()
                    }
                }
            )
            GlassDivider(startIndent = 0.dp, endIndent = 0.dp)
        }

        // 错误行
        (inputError ?: saveError)?.let { error ->
            Text(error, color = LiquidGlassTokens.ExpenseRed, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
        }

        // 底部：备注聚焦时交给系统输入法（不渲染数字键盘），点金额数字即可切回
        if (!noteFocused) {
            NumberPad(
                onDigitClick = { onDraftChange(draft.copy(amount = Money.key(draft.amount, it))); inputError = null },
                onDeleteClick = { onDraftChange(draft.copy(amount = Money.key(draft.amount, "DEL"))) },
                onClearClick = { onDraftChange(draft.copy(amount = "0")) },
                onCompleteClick = { complete() },
                enabled = !saving
            )
        }
    }
    if (showDateTime) {
        // 单弹窗设置日期和时间：上排 年/月/日 三轮，下排 时:分 两轮
        val initial = remember(showDateTime) { Calendar.getInstance().apply { timeInMillis = draft.timestamp } }
        val minYear = remember(showDateTime) { Calendar.getInstance().get(Calendar.YEAR) - 10 }
        var selYear by remember(showDateTime) { mutableStateOf(initial.get(Calendar.YEAR)) }
        var selMonth by remember(showDateTime) { mutableStateOf(initial.get(Calendar.MONTH) + 1) }
        var selDay by remember(showDateTime) { mutableStateOf(initial.get(Calendar.DAY_OF_MONTH)) }
        var selHour by remember(showDateTime) { mutableStateOf(initial.get(Calendar.HOUR_OF_DAY)) }
        var selMinute by remember(showDateTime) { mutableStateOf(initial.get(Calendar.MINUTE)) }
        // 当年当月实际天数（31/30/29/28），日轮范围随之联动
        val maxDay = java.util.GregorianCalendar(selYear, selMonth - 1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)
        // 月份/年份变化时，选中日收口到当月实际天数
        LaunchedEffect(selYear, selMonth) {
            if (selDay > maxDay) selDay = maxDay
        }
        GlassAlertDialog(
            onDismissRequest = { showDateTime = false },
            title = { Text("设置日期和时间") },
            text = {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                        WheelColumn(
                            count = 12,
                            initialIndex = (selYear - minYear).coerceIn(0, 11),
                            modifier = Modifier.width(64.dp),
                            format = { (minYear + it).toString() }
                        ) { selYear = minYear + it }
                        WheelColumn(count = 12, initialIndex = selMonth - 1, modifier = Modifier.width(52.dp), format = { String.format("%02d", it + 1) }) { selMonth = it + 1 }
                        WheelColumn(count = maxDay, initialIndex = (selDay - 1).coerceIn(0, maxDay - 1), modifier = Modifier.width(52.dp), format = { String.format("%02d", it + 1) }) { selDay = it + 1 }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        WheelColumn(count = 24, initialIndex = selHour, modifier = Modifier.width(52.dp)) { selHour = it }
                        Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = inkSecondary())
                        WheelColumn(count = 60, initialIndex = selMinute, modifier = Modifier.width(52.dp)) { selMinute = it }
                    }
                }
            },
            confirmButton = { GlassDialogAction("设置", emphasized = true, onClick = {
                // 月份天数上限防溢出（如 1月31日 切到 2月）
                val maxDay = java.util.GregorianCalendar(selYear, selMonth - 1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)
                val cal = Calendar.getInstance().apply {
                    clear()
                    set(selYear, selMonth - 1, selDay.coerceAtMost(maxDay), selHour, selMinute, 0)
                }
                onDraftChange(draft.copy(timestamp = cal.timeInMillis))
                showDateTime = false
            }) },
            dismissButton = { GlassDialogAction("取消", onClick = { showDateTime = false }) }
        )
    }
}

/**
 * 闹钟式滚轮列（几何确定版）。
 * flat 索引映射：flat 0 = 顶部占位；flat (1+i) = item i；flat (count+1/2) = 底部占位×2。
 * 居中行 = 视口第 2 行 = flat (firstVisible+1) = item firstVisible
 * → 不变量：「item c 居中 ⇔ firstVisible = c 且 offset = 0」。
 * 滚动结束按连续位置（firstVisible + offset）就近吸附整格：
 * 低速拨半格松手也能落到最近项（rememberSnapFlingBehavior 只接管 fling，不管慢拖），
 * 拨过头同样弹回首/末项，居中行永不落空。
 */
@Composable
private fun WheelColumn(
    count: Int,
    initialIndex: Int,
    modifier: Modifier = Modifier,
    format: (Int) -> String = { String.format(Locale.CHINA, "%02d", it) },
    onSelected: (Int) -> Unit
) {
    val itemHeight = 38.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    var centered by remember { mutableIntStateOf(initialIndex.coerceIn(0, count - 1)) }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = centered // flat centered 顶对齐 → 中行恰好是 item centered
    )
    var userAdjusted by remember { mutableStateOf(false) }

    // 滚动中实时跟随（加粗项；越过首尾占位时 coerce 保持边界值）
    val centeredLive by remember(count) {
        derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, count - 1) }
    }
    LaunchedEffect(centeredLive) {
        if (userAdjusted && centeredLive != centered) {
            centered = centeredLive
            onSelected(centered)
        }
    }

    // 滚动结束：按连续位置就近吸附整格（慢拨半格也能落到最近项；拨过头弹回首/末项）
    LaunchedEffect(listState, count) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (scrolling) {
                    userAdjusted = true
                } else if (userAdjusted) {
                    val continuous = listState.firstVisibleItemIndex +
                        listState.firstVisibleItemScrollOffset / itemHeightPx
                    val target = (continuous + 0.5f).toInt().coerceIn(0, count - 1)
                    if (listState.firstVisibleItemIndex != target || listState.firstVisibleItemScrollOffset != 0) {
                        listState.animateScrollToItem(target)
                    }
                    if (centered != target) {
                        centered = target
                        onSelected(target)
                    }
                }
            }
    }

    // 日轮范围变化（月份天数）时收口：让最后一项精确居中
    LaunchedEffect(count) {
        if (centered > count - 1) {
            centered = count - 1
            listState.animateScrollToItem(count - 1) // flat count-1 顶对齐 → 中行 = item count-1
            onSelected(centered)
        }
    }

    Box(modifier.height(itemHeight * 3)) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            item { Spacer(Modifier.height(itemHeight)) }
            items(count) { index ->
                val distance = kotlin.math.abs(index - centeredLive)
                Box(Modifier.fillMaxWidth().height(itemHeight), contentAlignment = Alignment.Center) {
                    Text(
                        format(index),
                        fontSize = if (distance == 0) 20.sp else 16.sp,
                        fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Medium,
                        color = inkPrimary().copy(
                            alpha = when (distance) {
                                0 -> 1f
                                1 -> 0.35f
                                else -> 0.12f
                            }
                        )
                    )
                }
            }
            item { Spacer(Modifier.height(itemHeight * 2)) }
        }
        // 选中行上下细线
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = itemHeight)
                .fillMaxWidth()
                .height(1.dp)
                .background(hairline())
        )
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = itemHeight * 2 - 1.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(hairline())
        )
    }
}

private fun dateTimeLabel(timestamp: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val now = Calendar.getInstance()
    val pattern = if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) "M月d日 HH:mm" else "yyyy年M月d日 HH:mm"
    return SimpleDateFormat(pattern, Locale.CHINA).format(Date(timestamp))
}

/** 信息胶囊：图标 + 文本（可带尾随箭头），凹陷轨道底。 */
@Composable
private fun MetaChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    trailing: (@Composable () -> Unit)? = null,
    emphasized: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(if (emphasized) panelFill() else insetTint())
            .border(0.6.dp, hairline(), CircleShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, null, Modifier.size(15.dp), tint = iconTint ?: inkSecondary())
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = inkPrimary().copy(alpha = if (enabled) 1f else 0.4f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (trailing != null) trailing()
    }
}

/** 账户下拉选择胶囊：点击弹出玻璃菜单，选中项带对勾。 */
@Composable
private fun AccountMenuChip(
    fallbackLabel: String,
    candidates: List<AccountEntity>,
    selectedId: Long,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (AccountEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = candidates.find { it.id == selectedId }
    Box(modifier) {
        MetaChip(
            icon = selected?.let { IconHelper.getAccountIcon(it.iconName) } ?: Icons.Default.AccountBalanceWallet,
            label = selected?.name ?: fallbackLabel,
            iconTint = selected?.let { accountTypeTint(it.type) },
            trailing = { Icon(Icons.Default.ExpandMore, null, Modifier.size(14.dp), tint = inkTertiary()) },
            emphasized = expanded,
            enabled = enabled
        ) { expanded = true }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = panelFill(),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            candidates.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Text(
                            account.name,
                            fontSize = 14.sp,
                            fontWeight = if (account.id == selectedId) FontWeight.SemiBold else FontWeight.Medium,
                            color = inkPrimary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            IconHelper.getAccountIcon(account.iconName),
                            null,
                            Modifier.size(18.dp),
                            tint = accountTypeTint(account.type)
                        )
                    },
                    trailingIcon = if (account.id == selectedId) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp), tint = glassAccent()) }
                    } else null,
                    onClick = { expanded = false; onSelect(account) }
                )
            }
        }
    }
}
