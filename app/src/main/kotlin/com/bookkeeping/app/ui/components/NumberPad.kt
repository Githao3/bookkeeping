package com.bookkeeping.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.ui.theme.liquidGlass

@Composable
fun NumberPad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonShape = RoundedCornerShape(16.dp)
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3", "C"),
            listOf("4", "5", "6", "+"),
            listOf("7", "8", "9", "-"),
            listOf(".", "0", "DEL", "OK")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    val isAction = key == "OK"
                    val isOp = key in listOf("C", "DEL", "+", "-")

                    val boxModifier = if (isAction) {
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(buttonShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF00E599), Color(0xFF00B075))
                                )
                            )
                            .clickable(onClick = onCompleteClick)
                    } else if (isOp) {
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .liquidGlass(shape = buttonShape, elevation = 2.dp, alphaMultiplier = 0.8f)
                            .clickable {
                                when (key) {
                                    "DEL" -> onDeleteClick()
                                    "C" -> onClearClick()
                                    else -> onDigitClick(key)
                                }
                            }
                    } else {
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .liquidGlass(shape = buttonShape, elevation = 3.dp)
                            .clickable { onDigitClick(key) }
                    }

                    Box(
                        modifier = boxModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            "DEL" -> {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "删除",
                                    tint = if (isDark) Color(0xFFE5E7EB) else Color(0xFF374151),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            "OK" -> {
                                Text(
                                    text = "完成",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            else -> {
                                Text(
                                    text = key,
                                    fontSize = if (key in listOf("+", "-")) 22.sp else 21.sp,
                                    fontWeight = if (key in listOf("+", "-", "C")) FontWeight.Bold else FontWeight.Medium,
                                    color = if (key in listOf("+", "-", "C")) {
                                        if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                                    } else {
                                        if (isDark) Color.White else Color(0xFF111827)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
