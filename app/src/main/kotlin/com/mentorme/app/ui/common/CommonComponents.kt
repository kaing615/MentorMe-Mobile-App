package com.mentorme.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mentorme.app.data.model.Mentor

@Composable
fun MentorCard(
    mentor: Mentor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Models.kt mới: availability: List<AvailabilitySlot> (isBooked: Boolean)
    val isAvailable = remember(mentor.availability) {
        mentor.availability.any { slot -> !slot.isBooked }
    }

    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: avatar + name + rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = mentor.avatar,                // ✅ was mentor.user.avatar
                    contentDescription = "Mentor Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mentor.fullName,            // ✅ was mentor.user.name
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            // ✅ rating + totalReviews (was totalBookings)
                            text = "${"%.1f".format(mentor.rating)} (${mentor.totalReviews})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Skills (was expertise)
            if (mentor.skills.isNotEmpty()) {
                Text(
                    text = mentor.skills.take(3).joinToString(separator = " • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Experience/bio short
            Text(
                text = mentor.experience,                 // vẫn có trong models mới
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${"%.0f".format(mentor.hourlyRate)}/hour",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isAvailable) {                        // ✅ was mentor.isAvailable
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Available",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonXL(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
