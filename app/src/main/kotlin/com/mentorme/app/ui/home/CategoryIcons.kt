package com.mentorme.app.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIcons {
    fun getIcon(category: String): ImageVector {
        return when (category.lowercase()) {
            "technology", "tech", "công nghệ" -> Icons.Default.Computer
            "business", "kinh doanh" -> Icons.Default.BusinessCenter
            "design", "thiết kế" -> Icons.Default.Palette
            "marketing", "tiếp thị" -> Icons.Default.Campaign
            "finance", "tài chính" -> Icons.Default.AccountBalance
            "career", "sự nghiệp" -> Icons.AutoMirrored.Filled.TrendingUp
            "data", "data science" -> Icons.Default.Analytics
            "management", "quản lý" -> Icons.Default.ManageAccounts
            "sales", "bán hàng" -> Icons.Default.Storefront
            "education", "giáo dục" -> Icons.Default.School
            else -> Icons.Default.Work
        }
    }
}

