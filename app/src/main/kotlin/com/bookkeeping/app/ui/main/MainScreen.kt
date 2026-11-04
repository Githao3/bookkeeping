package com.bookkeeping.app.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookkeeping.app.BookkeepingApplication
import com.bookkeeping.app.ui.account.AccountScreen
import com.bookkeeping.app.ui.add.AddTransactionContent
import com.bookkeeping.app.ui.components.LiquidBottomBar
import com.bookkeeping.app.ui.ledger.LedgerScreen
import com.bookkeeping.app.ui.settings.SettingsScreen
import com.bookkeeping.app.ui.stats.StatsScreen
import com.bookkeeping.app.ui.theme.AmbientAtmosphereBackground

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

    var currentTab by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val monthlyTransactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val currentMonthTimestamp by viewModel.currentMonthTimestamp.collectAsStateWithLifecycle()

    AmbientAtmosphereBackground(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Main content depending on active tab
            when (currentTab) {
                0 -> LedgerScreen(
                    transactions = monthlyTransactions,
                    currentMonthTimestamp = currentMonthTimestamp,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                )
                1 -> StatsScreen(
                    transactions = allTransactions
                )
                2 -> AccountScreen(
                    accounts = accounts,
                    onAddAccount = { viewModel.addAccount(it) },
                    onDeleteAccount = { viewModel.deleteAccount(it) }
                )
                3 -> SettingsScreen(
                    transactionCount = allTransactions.size,
                    accountCount = accounts.size
                )
            }

            // Floating Liquid Glass Navigation Bar (Aligned to Bottom Center)
            LiquidBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onAddClick = { showAddSheet = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState
        ) {
            AddTransactionContent(
                accounts = accounts,
                expenseCategories = expenseCategories,
                incomeCategories = incomeCategories,
                onSaveTransaction = { viewModel.addTransaction(it) },
                onDismiss = { showAddSheet = false }
            )
        }
    }
}
