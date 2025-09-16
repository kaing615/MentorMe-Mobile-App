// CalendarScreen.kt
package com.mentorme.app.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalendarScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Calendar", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text("Placeholder – sẽ thay bằng UI thật.")
    }
}
