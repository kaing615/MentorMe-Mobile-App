package com.mentorme.app.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Upcoming", "Completed", "Cancelled")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "My Bookings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(MockData.mockBookings) { booking ->
                BookingCard(
                    booking = booking,
                    onClick = {
                        navController.navigate(Screen.BookingDetail.createRoute(booking.id))
                    }
                )
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.topic,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                StatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "with ${booking.mentor?.user?.name ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = booking.scheduledAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${booking.duration} minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (booking.status == BookingStatus.CONFIRMED) {
                    Row {
                        TextButton(onClick = { /* Join meeting */ }) {
                            Text("Join")
                        }
                        TextButton(onClick = { /* Chat */ }) {
                            Text("Chat")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: BookingStatus) {
    val (color, text) = when (status) {
        BookingStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer to "Pending"
        BookingStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer to "Confirmed"
        BookingStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer to "In Progress"
        BookingStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer to "Completed"
        BookingStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to "Cancelled"
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
