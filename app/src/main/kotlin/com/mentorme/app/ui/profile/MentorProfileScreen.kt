package com.mentorme.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MentorProfileScreen(
    onEditProfile: () -> Unit = {},
    onViewEarnings: () -> Unit = {},
    onViewReviews: () -> Unit = {},
    onUpdateAvailability: () -> Unit = {},
    onManageServices: () -> Unit = {},
    onViewStatistics: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hồ sơ Mentor",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Quản lý hồ sơ và cài đặt Mentor",
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        // TODO: Implement mentor profile components
        // - Profile photo and basic info
        // - Mentor credentials and certifications
        // - Service offerings and pricing
        // - Availability settings
        // - Performance statistics
        // - Earnings overview
        // - Account settings
    }
}
