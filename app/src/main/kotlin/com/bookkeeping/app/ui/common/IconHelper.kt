package com.bookkeeping.app.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    fun getCategoryIcon(iconName: String): ImageVector {
        return when (iconName) {
            "Restaurant" -> Icons.Default.Restaurant
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "SportsEsports" -> Icons.Default.SportsEsports
            "Home" -> Icons.Default.Home
            "LocalHospital" -> Icons.Default.LocalHospital
            "School" -> Icons.Default.School
            "Smartphone" -> Icons.Default.Smartphone
            "CardGiftcard", "RedPacket" -> Icons.Default.CardGiftcard
            "Work" -> Icons.Default.Work
            "Laptop" -> Icons.Default.Laptop
            "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp
            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            "Transfer", "swap_horiz" -> Icons.Default.SwapHoriz
            else -> Icons.Default.MoreHoriz
        }
    }

    fun getAccountIcon(iconName: String): ImageVector {
        return when (iconName) {
            "Wechat", "payments" -> Icons.Default.Payments
            "Alipay" -> Icons.Default.AccountBalanceWallet
            "BankCard", "credit_card" -> Icons.Default.CreditCard
            "Cash", "attach_money" -> Icons.Default.AttachMoney
            else -> Icons.Default.AccountBalance
        }
    }
}
