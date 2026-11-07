package com.bookkeeping.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.edit
import androidx.compose.material3.Text
import com.bookkeeping.app.ui.components.GlassAlertDialog
import com.bookkeeping.app.ui.components.GlassDialogAction
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch
import com.bookkeeping.app.ui.common.FormatHelper
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Activity
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import com.bookkeeping.app.BookkeepingApplication
import com.bookkeeping.app.ui.account.AccountScreen
import com.bookkeeping.app.ui.add.AddTransactionContent
import com.bookkeeping.app.ui.components.LiquidBottomBar
import com.bookkeeping.app.ui.ledger.LedgerScreen
import com.bookkeeping.app.ui.settings.SettingsScreen
import com.bookkeeping.app.ui.stats.StatsScreen
import com.bookkeeping.app.ui.theme.LiquidGlassTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as BookkeepingApplication
    val viewModel: BookkeepingViewModel = viewModel(
        factory = BookkeepingViewModel.Factory(app.repository)
    )

    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val prefs = remember { context.getSharedPreferences("preferences", android.content.Context.MODE_PRIVATE) }
    var hideAmounts by rememberSaveable { mutableStateOf(prefs.getBoolean("hideAmounts", false)) }
    // 外观主题：system 跟随系统 / light 亮色 / dark 暗色，默认跟随系统
    var themeMode by rememberSaveable { mutableStateOf(prefs.getString("themeMode", "system") ?: "system") }
    val baseConfiguration = LocalConfiguration.current
    val configuration = remember(themeMode, baseConfiguration) {
        Configuration(baseConfiguration).apply {
            uiMode = when (themeMode) {
                "light" -> (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
                "dark" -> (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
                else -> uiMode
            }
        }
    }
    fun dismissDraft() { if (!viewModel.saving) { if (viewModel.draft.hasContent) confirmDiscard = true else showAddSheet = false } }
    LaunchedEffect(viewModel.saveError) { viewModel.saveError?.let { if (!showAddSheet) snackbar.showSnackbar(it) } }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val monthlyTransactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val currentMonthTimestamp by viewModel.currentMonthTimestamp.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        FormatHelper.LocalHideAmounts provides hideAmounts
    ) {
    // 生效主题（覆写后的 uiMode），全树 isSystemInDarkTheme() 统一跟随
    val isDark = isSystemInDarkTheme()
    // 状态栏 / 导航栏图标明暗随生效主题切换
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) LiquidGlassTokens.GroundDark else LiquidGlassTokens.GroundLight)
    ) {
        // Main view content based on selected tab
        when (currentTab) {
            0 -> LedgerScreen(
                transactions = monthlyTransactions,
                currentMonthTimestamp = currentMonthTimestamp,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onEditTransaction = { viewModel.editTransaction(it); showAddSheet = true },
                onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) { scope.launch {
                    if (snackbar.showSnackbar("账单已删除", "撤销", duration = SnackbarDuration.Long) == SnackbarResult.ActionPerformed) viewModel.undoDelete(tx)
                } } }
            )
            1 -> StatsScreen(
                transactions = allTransactions,
                currentMonthTimestamp = currentMonthTimestamp,
                onPreviousMonth = { viewModel.previousMonth() }, onNextMonth = { viewModel.nextMonth() },
                onPreviousYear = { viewModel.changeYear(-1) }, onNextYear = { viewModel.changeYear(1) }
            )
            2 -> AccountScreen(
                accounts = accounts,
                transactions = allTransactions,
                saving = viewModel.saving, error = viewModel.saveError,
                onAddAccount = { account, done -> viewModel.addAccount(account, done) },
                onDeleteAccount = { account, done -> viewModel.deleteAccount(account) { done(); scope.launch {
                    if (snackbar.showSnackbar("账户已删除", "撤销", duration = SnackbarDuration.Long) == SnackbarResult.ActionPerformed) viewModel.undoDeleteAccount(account)
                } } },
                onArchiveAccount = { account, done -> viewModel.archiveAccount(account, done) }
            )
            3 -> SettingsScreen(
                transactionCount = allTransactions.size,
                accountCount = accounts.size, repository = viewModel.repository,
                hideAmounts = hideAmounts, onHideAmounts = { hideAmounts = it; prefs.edit { putBoolean("hideAmounts", it) } },
                themeMode = themeMode, onThemeChange = { themeMode = it; prefs.edit { putString("themeMode", it) } }
            )
        }

        // 独立悬浮加号按钮：仅在「明细」页面（currentTab == 0）出现，切到统计/资产/设置时优雅退场
        AnimatedVisibility(
            visible = currentTab == 0,
            enter = fadeIn(animationSpec = tween(150)) +
                    scaleIn(
                        initialScale = 0.75f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
            exit = fadeOut(animationSpec = tween(150)) +
                    scaleOut(
                        targetScale = 0.75f,
                        animationSpec = tween(150)
                    ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 84.dp)
        ) {
            FloatingAddButton(
                onClick = { viewModel.beginNewTransaction(); showAddSheet = true }
            )
        }

        // 扁平悬浮胶囊底栏（纯净 4 槽位 + 丝滑滑动药丸）
        LiquidBottomBar(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 106.dp))
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { dismissDraft() },
            dragHandle = null,
            sheetState = sheetState,
            containerColor = if (isDark) LiquidGlassTokens.CardSurfaceDark else LiquidGlassTokens.CardSurfaceLight
        ) {
            AddTransactionContent(
                accounts = accounts.filter { !it.isArchived || viewModel.draft.id != 0L && it.id in listOf(viewModel.draft.sourceId, viewModel.draft.targetId) },
                expenseCategories = expenseCategories,
                incomeCategories = incomeCategories,
                draft = viewModel.draft, onDraftChange = viewModel::updateDraft,
                saving = viewModel.saving, saveError = viewModel.saveError,
                onSaveTransaction = { viewModel.addTransaction(it) { showAddSheet = false; currentTab = 0; scope.launch { snackbar.showSnackbar("账单已保存") } } },
                onDismiss = { dismissDraft() }
            )
        }
    }
    if (confirmDiscard) GlassAlertDialog(onDismissRequest = { confirmDiscard = false }, title = { Text("保留这份草稿？") },
        text = { Text("下次记账可继续填写。") },
        confirmButton = { GlassDialogAction("保留草稿", onClick = { confirmDiscard = false; showAddSheet = false }, emphasized = true) },
        dismissButton = { GlassDialogAction("放弃草稿", onClick = { viewModel.clearDraft(); confirmDiscard = false; showAddSheet = false }) })
    }
}

/**
 * 屏幕右下角悬浮纯加号按钮（FAB）
 * 位于底栏上方，高质感翡翠绿渐变，纯白色加号，无多余文字
 */
@Composable
private fun FloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color(0x3300C853),
                spotColor = Color(0x5500C853)
            )
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00E676),
                        Color(0xFF00C853)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                color = Color.White.copy(alpha = 0.6f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "记账",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}
