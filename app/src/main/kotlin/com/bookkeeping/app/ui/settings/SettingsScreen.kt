package com.bookkeeping.app.ui.settings

import android.widget.Toast
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
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Hero Glass Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(26.dp),
                    elevation = 12.dp
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .liquidGlass(shape = CircleShape, elevation = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "设置与关于",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "简记账 CoinFlow · 纯本地离线隐私",
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section: Data & Backup
            item {
                Text(
                    text = "数据与安全",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                    modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(22.dp),
                            elevation = 6.dp
                        )
                        .padding(vertical = 8.dp)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Security,
                            iconColor = Color(0xFF10B981),
                            title = "隐私模式",
                            subtitle = "100% 离线运行，数据绝不出手机",
                            trailing = "已启用"
                        )

                        SettingsDivider()

                        SettingsRow(
                            icon = Icons.Default.FileDownload,
                            iconColor = Color(0xFF3B82F6),
                            title = "账单导出 (CSV)",
                            subtitle = "一键导出当前 $transactionCount 笔流水记录",
                            trailing = "导出",
                            onClick = {
                                Toast.makeText(context, "已整理 $transactionCount 笔账单，导出功能就绪", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Section: Visual & Experience
            item {
                Text(
                    text = "视觉与设计",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                    modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(22.dp),
                            elevation = 6.dp
                        )
                        .padding(vertical = 8.dp)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.AutoAwesome,
                            iconColor = Color(0xFFEC4899),
                            title = "设计风格",
                            subtitle = "iOS 液态玻璃（悬浮彩虹胶囊）",
                            trailing = "已生效"
                        )
                    }
                }
            }

            // Section: About
            item {
                Text(
                    text = "关于简记账",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                    modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(22.dp),
                            elevation = 6.dp
                        )
                        .padding(vertical = 8.dp)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Info,
                            iconColor = Color(0xFF8B5CF6),
                            title = "当前版本",
                            subtitle = "CoinFlow v1.0.0 (Release Build)",
                            trailing = "最新"
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
                .size(40.dp)
                .liquidGlass(shape = RoundedCornerShape(12.dp), elevation = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF1F2937)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
            )
        }

        if (trailing != null) {
            Text(
                text = trailing,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
            )
        }

        if (onClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    val isDark = isSystemInDarkTheme()
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .padding(horizontal = 16.dp)
            .clip(CircleShape)
            .liquidGlass(shape = CircleShape, elevation = 0.dp)
    )
}
