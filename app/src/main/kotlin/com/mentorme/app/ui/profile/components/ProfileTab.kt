package com.mentorme.app.ui.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.profile.UserProfile
import com.mentorme.app.ui.profile.formatDateVi
import com.mentorme.app.ui.theme.LiquidGlassCard

@Composable
fun ProfileTab(
    profile: UserProfile,
    isEditing: Boolean,
    edited: UserProfile,
    onEditToggle: () -> Unit,
    onChange: (UserProfile) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            radius = 22.dp
        ) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thông tin cơ bản", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = onEditToggle,
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = .5f)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Outlined.Close else Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isEditing) "Hủy" else "Chỉnh sửa",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AvatarPicker(
                        avatarUrl = if (isEditing) edited.avatar else profile.avatar,
                        initial = profile.fullName.firstOrNull()?.uppercaseChar() ?: 'U',
                        size = 96.dp,
                        enabled = isEditing,
                        onPick = { uri -> onChange(edited.copy(avatar = uri)) }
                    )

                    AnimatedVisibility(visible = !isEditing) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                profile.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Thành viên từ ${formatDateVi(profile.joinDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledField(
                        label = "Họ và tên",
                        leading = { Icon(Icons.Outlined.Person, null) },
                        value = profile.fullName,
                        editing = isEditing,
                        editedValue = edited.fullName,
                        onValueChange = { onChange(edited.copy(fullName = it)) }
                    )
                    LabeledField(
                        label = "Email",
                        leading = { Icon(Icons.Outlined.Email, null) },
                        value = profile.email,
                        editing = isEditing,
                        editedValue = edited.email,
                        onValueChange = { onChange(edited.copy(email = it)) },
                        type = TextFieldType.Email
                    )
                    LabeledField(
                        label = "Số điện thoại",
                        leading = { Icon(Icons.Outlined.Phone, null) },
                        value = profile.phone ?: "Chưa cập nhật",
                        editing = isEditing,
                        editedValue = edited.phone.orEmpty(),
                        onValueChange = { onChange(edited.copy(phone = it)) }
                    )
                    LabeledField(
                        label = "Địa chỉ",
                        leading = { Icon(Icons.Outlined.Place, null) },
                        value = profile.location ?: "Chưa cập nhật",
                        editing = isEditing,
                        editedValue = edited.location.orEmpty(),
                        onValueChange = { onChange(edited.copy(location = it)) }
                    )
                    LabeledMultiline(
                        label = "Giới thiệu bản thân",
                        value = profile.bio ?: "Chưa có mô tả",
                        editing = isEditing,
                        editedValue = edited.bio.orEmpty(),
                        onValueChange = { onChange(edited.copy(bio = it)) }
                    )
                }

                Column {
                    Text("Lĩnh vực quan tâm", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.padding(top = 8.dp))
                    FlowRowWrap(horizontalGap = 8.dp, verticalGap = 8.dp) {
                        profile.interests.forEach {
                            AssistChip(
                                onClick = {},
                                label = { Text(it) },
                                leadingIcon = { Icon(Icons.Outlined.Star, null) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = Color.White,
                                    leadingIconContentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = isEditing) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MMPrimaryButton(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Lưu thay đổi") }
                        MMGhostButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Hủy") }
                    }
                }
            }
        }

        // Spacer để nội dung không bị bottom bar đè khi scroll xuống
        Spacer(Modifier.height(80.dp))
    }
}
