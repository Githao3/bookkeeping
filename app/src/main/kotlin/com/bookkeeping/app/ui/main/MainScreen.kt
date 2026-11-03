package com.bookkeeping.app.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookkeeping.app.BookkeepingApplication
import com.bookkeeping.app.ui.account.AccountScreen
import com.bookkeeping.app.ui.add.AddTransactionContent
import com.bookkeeping.app.ui.ledger.LedgerScreen
import com.bookkeeping.app.ui.stats.StatsScreen

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = "明细",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("明细") }
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = "统计",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("统计") }
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = "资产",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("资产") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "记一笔",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            0 -> LedgerScreen(
                transactions = monthlyTransactions,
                currentMonthTimestamp = currentMonthTimestamp,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onDeleteTransaction = { viewModel.deleteTransaction(it) },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> StatsScreen(
                transactions = allTransactions,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> AccountScreen(
                accounts = accounts,
                onAddAccount = { viewModel.addAccount(it) },
                onDeleteAccount = { viewModel.deleteAccount(it) },
                modifier = Modifier.padding(innerPadding)
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
