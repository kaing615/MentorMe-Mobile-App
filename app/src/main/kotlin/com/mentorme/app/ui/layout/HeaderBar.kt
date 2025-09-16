package com.mentorme.app.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.ui.theme.liquidGlassStrong

data class UserUi(
    val name: String,
    val avatar: String? = null,
    val role: String = "mentee"
)

@Composable
fun HeaderBar(
    user: UserUi?,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)

    // "Pill" floating với liquid glass effect nhưng không có nền che
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
            .clip(shape)
            .liquidGlass(radius = 24.dp, alpha = 0.08f, borderAlpha = 0.15f) // Liquid glass với độ mờ vừa phải
            .shadow(elevation = 8.dp, shape = shape, clip = false),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo + dot
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "MentorMe",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .size(8.dp)
                    .background(Color(0xFF60A5FA), CircleShape)
            )
        }

        // Actions
        if (user != null) {
            Row(
                modifier = Modifier.padding(end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO: notifications */ }) {
                    BadgedBox(badge = { Badge { Text("3") } }) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Thông báo",
                            tint = Color.White
                        )
                    }
                }
                IconButton(onClick = { /* TODO: messages */ }) {
                    BadgedBox(badge = { Badge { Text("2") } }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Message,
                            contentDescription = "Tin nhắn",
                            tint = Color.White
                        )
                    }
                }

                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = { expanded = !expanded }) {
                    if (user.avatar != null) {
                        AsyncImage(
                            model = user.avatar,
                            contentDescription = user.name,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0x40FFFFFF), CircleShape)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White)
                    }
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Profile") },
                        onClick = {
                            expanded = false
                            onProfileClick()
                        },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
                    )
                    if (onLogout != null) {
                        DropdownMenuItem(
                            text = { Text("Đăng xuất", color = Color(0xFFE11D48)) },
                            onClick = {
                                expanded = false; onLogout()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Logout,
                                    contentDescription = null,
                                    tint = Color(0xFFE11D48)
                                )
                            }
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onLoginClick) { Text("Đăng nhập") }
                Button(onClick = onRegisterClick) { Text("Đăng ký") }
            }
        }
    }
}
