package com.mentorme.app.ui.dashboard

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
fun MentorDashboardScreen(
    onViewSchedule: () -> Unit = {},
    onViewStudents: () -> Unit = {},
    onViewEarnings: () -> Unit = {},
    onViewReviews: () -> Unit = {},
    onJoinSession: (String) -> Unit = {},
    onViewAllSessions: () -> Unit = {},
    onUpdateProfile: () -> Unit = {},
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
            text = "Mentor Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Dashboard cho Mentor sẽ được implement tại đây",
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        // TODO: Implement mentor dashboard components
        // - Overview stats (students, sessions, earnings)
        // - Upcoming sessions
        // - Quick actions
        // - Performance metrics
    }
}
