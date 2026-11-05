package com.bookkeeping.app.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.ui.theme.LiquidGlassTokens
import com.bookkeeping.app.ui.theme.liquidGlass

@Composable
fun SettingsScreen(
    transactionCount: Int,
    accountCount: Int,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Hero Glass Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(24.dp),
                    elevation = 8.dp
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x3360A5FA) else Color(0x18007AFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = LiquidGlassTokens.AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "偏好与设置",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "CoinFlow · 极简液态离线记账",
                        fontSize = 12.sp,
                        color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Data & Backup
            item {
                Text(
                    text = "数据与安全",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(20.dp),
                            elevation = 3.dp
                        )
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Security,
                            iconColor = LiquidGlassTokens.IncomeGreen,
                            title = "隐私模式",
                            subtitle = "100% 本地离线，数据不出手机",
                            trailing = "已保护"
                        )

                        SettingsDivider(isDark)

                        SettingsRow(
                            icon = Icons.Default.FileDownload,
                            iconColor = LiquidGlassTokens.AccentBlue,
                            title = "账单导出 (CSV)",
                            subtitle = "一键导出当前 $transactionCount 笔流水记录",
                            trailing = "导出",
                            onClick = {
                                Toast.makeText(context, "已就绪 $transactionCount 笔账单，导出功能就绪", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Section: Visual & Experience
            item {
                Text(
                    text = "视觉系统",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(20.dp),
                            elevation = 3.dp
                        )
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.AutoAwesome,
                            iconColor = Color(0xFF8B5CF6),
                            title = "设计质感",
                            subtitle = "iOS 液态玻璃 (Kyant0 Backdrop AGSL)",
                            trailing = "生效中"
                        )
                    }
                }
            }

            // Section: About
            item {
                Text(
                    text = "关于应用",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(20.dp),
                            elevation = 3.dp
                        )
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Info,
                            iconColor = LiquidGlassTokens.TextSecondaryLight,
                            title = "当前版本",
                            subtitle = "CoinFlow v1.0.0 (Release Build)",
                            trailing = "最新版"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    trailing: String? = null,
    onClick: (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = if (isDark) 0.25f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
            )
        }

        if (trailing != null) {
            Text(
                text = trailing,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF60A5FA) else LiquidGlassTokens.AccentBlue
            )
        }

        if (onClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsDivider(isDark: Boolean) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 68.dp, end = 16.dp)
            .height(0.6.dp)
            .background(if (isDark) Color(0x1FFFFFFF) else Color(0x0A000000))
    )
}
