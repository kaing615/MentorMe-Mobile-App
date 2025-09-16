package com.mentorme.app.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mentorme.app.ui.theme.liquidGlass

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
    onLogout: (() -> Unit)? = null
) {
    Surface(color = Color.Transparent) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(56.dp)
                .liquidGlass(), // 👈 glass nhẹ cho header
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "MentorMe",
                    style = MaterialTheme.typography.headlineMedium,
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

            // Right controls
            if (user != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { }) {
                        BadgedBox(badge = { Badge { Text("3") } }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Thông báo")
                        }
                    }
                    IconButton(onClick = { }) {
                        BadgedBox(badge = { Badge { Text("2") } }) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Tin nhắn")
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
                                    .background(Color.Gray, CircleShape)
                            )
                        } else {
                            Icon(Icons.Filled.Person, contentDescription = "Profile")
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
                                    expanded = false
                                    onLogout()
                                },
                                leadingIcon = { Icon(Icons.Filled.Logout, contentDescription = null, tint = Color(0xFFE11D48)) }
                            )
                        }
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onLoginClick) { Text("Đăng nhập") }
                    Button(onClick = onRegisterClick) { Text("Đăng ký") }
                }
            }
        }
    }
}
