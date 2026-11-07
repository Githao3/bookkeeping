package com.bookkeeping.app

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.bookkeeping.app.data.local.entity.*
import com.bookkeeping.app.ui.add.AddTransactionContent
import com.bookkeeping.app.ui.main.TransactionDraft
import com.bookkeeping.app.ui.theme.BookkeepingTheme
import org.junit.*
import org.junit.Assert.*

class EntryUiTest {
    @get:Rule val compose = createComposeRule()
    private var saved: TransactionEntity? = null
    private var viewportHeight by mutableStateOf<Int?>(null)
    private val accounts = listOf(AccountEntity(1,"现金",AccountType.CASH),AccountEntity(2,"银行",AccountType.BANK_CARD))
    private fun render(fontScale: Float = 1f, transfer: Boolean = false, error: String? = null) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                BookkeepingTheme {
                    var draft by remember { mutableStateOf(TransactionDraft(type=if(transfer) TransactionType.TRANSFER else TransactionType.EXPENSE)) }
                    AddTransactionContent(accounts, (1L..10).map { CategoryEntity(it,"分类$it",TransactionType.EXPENSE,"Restaurant",0) },
                        emptyList(), draft, { draft=it }, false, error, { saved=it }, {},
                        (if (viewportHeight == null) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(viewportHeight!!.dp)).statusBarsPadding().navigationBarsPadding())
                }
            }
        }
    }
    @Test fun allKeypadRowsVisibleOnSmallScreen() {
        render(); listOf("7","8","9",".","删除","完成").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
    }
    @Test fun keyboardVisibleWithLargeFontAndFailedSave() {
        render(1.3f,error="存储失败，请重试"); compose.onNodeWithText("完成").assertIsDisplayed(); compose.onNodeWithText("7").assertIsDisplayed(); compose.onNodeWithText("存储失败，请重试").assertIsDisplayed()
    }
    @Test fun mixedExpressionSavesExactCents() {
        render(); listOf("1","0","0","-","2","0","+","5").forEach { key -> compose.onAllNodesWithText(key).onLast().performClick() }
        compose.onNodeWithText("100-20+5").assertIsDisplayed(); compose.onNodeWithText("完成").performClick()
        compose.runOnIdle { assertEquals(8500L,saved!!.amount) }
    }
    @Test fun transferDefaultsToDifferentAccounts() {
        render(transfer=true); compose.onNodeWithText("1").performClick(); compose.onNodeWithText("完成").performClick()
        compose.runOnIdle { assertEquals(1L,saved!!.sourceAccountId); assertEquals(2L,saved!!.targetAccountId) }
    }
    @Test fun incompleteAmountShowsErrorAndDoesNotSave() {
        render(); compose.onNodeWithText("1").performClick(); compose.onNodeWithText("+").performClick(); compose.onNodeWithText("完成").performClick()
        compose.onNodeWithText("请补全金额表达式").assertIsDisplayed(); compose.runOnIdle { assertNull(saved) }
    }
    @Test fun noteAndSaveRemainUsableInKeyboardSizedViewport() {
        render(fontScale=1.3f)
        compose.onNodeWithText("1").performClick()
        compose.onNode(hasSetTextAction()).performScrollTo().performClick().performTextInput("键盘测试备注")
        // A real IME reduces available height after focus, when the amount keypad
        // has already been replaced by the compact save row.
        compose.runOnIdle { viewportHeight = 340 }
        compose.onNodeWithText("金额键盘").assertIsDisplayed()
        compose.onNodeWithText("保存").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("键盘测试备注",saved!!.note); assertEquals(100L,saved!!.amount) }
    }
}
