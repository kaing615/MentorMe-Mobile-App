package com.mentorme.app.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Locale
import com.mentorme.app.ui.theme.liquidGlass

/**
 * Dialog for submitting a review for a booking
 * @param bookingId The booking ID to review
 * @param mentorName The mentor's name
 * @param onDismiss Called when dialog is dismissed
 * @param onSubmit Called when review is submitted (rating, comment)
 * @param isSubmitting Whether the review is being submitted
 * @param errorMessage Error message to display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(
    bookingId: String,
    mentorName: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String?) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .liquidGlass()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đánh giá phiên tư vấn",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (!isSubmitting) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Mentor name
                Text(
                    text = "Mentor: $mentorName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )

                // Star rating
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Đánh giá của bạn",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "$i sao",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable(enabled = !isSubmitting) { rating = i },
                                tint = if (i <= rating) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                    if (rating > 0) {
                        Text(
                            text = when (rating) {
                                1 -> "Rất tệ"
                                2 -> "Tệ"
                                3 -> "Trung bình"
                                4 -> "Tốt"
                                5 -> "Xuất sắc"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFD700)
                        )
                    }
                }

                // Comment field
                TextField(
                    value = comment,
                    onValueChange = { if (it.length <= 1000) comment = it },
                    label = { Text("Nhận xét (không bắt buộc)", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Chia sẻ trải nghiệm của bạn với mentor...", color = Color.White.copy(alpha = 0.5f)) },
                    readOnly = isSubmitting,
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                        disabledContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = "${comment.length}/1000 ký tự",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )

                // Error message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFFF6B6B).copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }

                // Submit button
                Button(
                    onClick = {
                        if (rating > 0) {
                            onSubmit(rating, comment.ifBlank { null })
                        }
                    },
                    enabled = rating > 0 && !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667eea),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isSubmitting) "Đang gửi..." else "Gửi đánh giá",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Star rating display (read-only)
 */
@Composable
fun StarRating(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: androidx.compose.ui.unit.Dp = 16.dp,
    showRatingText: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(starSize)
        )
        if (showRatingText) {
            Text(
                text = String.format(Locale.US, "%.1f", rating),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

