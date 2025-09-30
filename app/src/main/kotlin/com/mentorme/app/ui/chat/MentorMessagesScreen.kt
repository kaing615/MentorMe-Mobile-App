package com.mentorme.app.ui.chat

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
fun MentorMessagesScreen(
    onOpenConversation: (String) -> Unit = {},
    onFilterStudents: () -> Unit = {},
    onSearchConversations: (String) -> Unit = {},
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
            text = "Tin nhắn Mentor",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Quản lý tin nhắn với học sinh",
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        // TODO: Implement mentor messages components
        // - List of conversations with students
        // - Filter by student/subject
        // - Quick reply functionality
        // - Notification settings
    }
}
