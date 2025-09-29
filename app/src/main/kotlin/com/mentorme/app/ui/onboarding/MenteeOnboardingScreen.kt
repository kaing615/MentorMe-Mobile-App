package com.mentorme.app.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mentorme.app.core.utils.copyUriToCache
import com.mentorme.app.ui.auth.BigGlassButton
import com.mentorme.app.ui.auth.FloatingLogo
import com.mentorme.app.ui.auth.GlassFormContainer
import com.mentorme.app.ui.auth.GlassInput
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun MenteeOnboardingScreen(
    onBack: () -> Unit,
    onDoneGoHome: () -> Unit,
    onGoToReview: () -> Unit
) {
    val vm: OnboardingViewModel = hiltViewModel()
    val ui by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    val pickImage = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        pickedUri = uri
        uri?.let {
            copyUriToCache(context, it, "avatar_${System.currentTimeMillis()}.jpg")
                ?.let { path -> vm.setAvatarPath(path) }
        }
    }


    // Điều hướng sau khi tạo profile
    LaunchedEffect(ui.success, ui.next, ui.isLoading) {
        if (!ui.isLoading && ui.success == true) {
            if (ui.next == "/onboarding/review") onGoToReview() else onDoneGoHome()
        }
    }

    // -------- UI --------
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Spacer(Modifier.height(8.dp))
        FloatingLogo(size = 76.dp)
        Spacer(Modifier.height(10.dp))

        Text(
            "Hoàn tất hồ sơ",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp,
            color = Color.White
        )
        Text(
            "Chỉ mất 1 phút — để mọi người biết bạn là ai ✨",
            color = Color.White.copy(.8f),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(18.dp))

        // Card “glass” chính
        GlassFormContainer {
            // 1) Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AvatarPicker(
                    pickedUri = pickedUri,           // Uri? người dùng vừa chọn
                    localPath = ui.avatarPath,       // String? path trong cache
                    onPick = { pickImage.launch("image/*") },
                    onClear = {
                        pickedUri = null
                        vm.setAvatarPath(null)
                    }
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Ảnh đại diện", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Tăng độ tin cậy và tỷ lệ tương tác",
                        color = Color.White.copy(.7f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { pickImage.launch("image/*") }) {
                            Text(
                                if (ui.avatarPath == null) "Chọn ảnh" else "Đổi ảnh",
                                color = Color.White
                            )
                        }
                        if (ui.avatarPath != null) {
                            TextButton(onClick = {
                                pickedUri = null
                                vm.setAvatarPath(null)
                            }) { Text("Xoá", color = Color.White.copy(0.8f)) }
                        }
                    }
                }
            }

            Divider(Modifier.padding(vertical = 10.dp), color = Color.White.copy(.08f))

            // 2) Thông tin bắt buộc
            SectionTitle("Thông tin cơ bản")
            GlassInput(
                value = ui.fullName,
                onValueChange = { vm.update { st -> st.copy(fullName = it) } },
                label = "Họ và tên *",
                placeholder = "Nguyễn Văn A"
            )
            GlassInput(
                value = ui.location,
                onValueChange = { vm.update { st -> st.copy(location = it) } },
                label = "Địa điểm *",
                placeholder = "Hồ Chí Minh, Việt Nam"
            )
            GlassInput(
                value = ui.category,
                onValueChange = { vm.update { st -> st.copy(category = it) } },
                label = "Chuyên mục *",
                placeholder = "Software, Design…"
            )

            // 3) Ngôn ngữ & Kỹ năng
            SectionTitle("Ngôn ngữ & Kỹ năng")
            Column(Modifier.fillMaxWidth()) {
                GlassInput(
                    value = ui.languages,
                    onValueChange = { vm.update { st -> st.copy(languages = it) } },
                    label = "Ngôn ngữ * (phẩy phân tách)",
                    placeholder = "vi,en"
                )
                ChipsPreview(csv = ui.languages, modifier = Modifier.padding(top = 6.dp))
            }

            Spacer(Modifier.height(6.dp))

            Column(Modifier.fillMaxWidth()) {
                GlassInput(
                    value = ui.skills,
                    onValueChange = { vm.update { st -> st.copy(skills = it) } },
                    label = "Kỹ năng * (phẩy phân tách)",
                    placeholder = "kotlin,compose,android"
                )
                ChipsPreview(csv = ui.skills, modifier = Modifier.padding(top = 6.dp))
            }

            // 4) Error Display - Enhanced
            AnimatedVisibility(
                visible = !ui.error.isNullOrBlank(),
                modifier = Modifier.padding(top = 8.dp),
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Có lỗi xảy ra",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = ui.error ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        IconButton(
                            onClick = { vm.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close error",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            SectionTitle("Thông tin bổ sung")
            GlassInput(
                value = ui.description ?: "",
                onValueChange = { vm.update { st -> st.copy(description = it) } },
                label = "Mô tả bản thân *",
                placeholder = "Giới thiệu ngắn về bạn..."
            )
            GlassInput(
                value = ui.goal ?: "",
                onValueChange = { vm.update { st -> st.copy(goal = it) } },
                label = "Mục tiêu *",
                placeholder = "Bạn mong muốn đạt được điều gì..."
            )
            GlassInput(
                value = ui.education ?: "",
                onValueChange = { vm.update { st -> st.copy(education = it) } },
                label = "Học vấn *",
                placeholder = "Ví dụ: Cử nhân CNTT - Đại học ABC"
            )

            // 5) Submit
            Spacer(Modifier.height(6.dp))
            BigGlassButton(
                text = if (ui.isLoading) "Đang lưu..." else "Hoàn tất",
                subText = null,
                enabled = !ui.isLoading,
                icon = {
                    if (ui.isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.2.dp,
                            modifier = Modifier.size(18.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Check, null, tint = Color.White)
                    }
                },
                onClick = { vm.submit() }
            )
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack) { Text("← Quay lại", color = Color.White.copy(0.8f)) }
        Spacer(Modifier.height(6.dp))
    }
}

/* ---------- Helpers: Avatar + Section + Chips preview ---------- */

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
    )
}

@Composable
fun AvatarPicker(
    pickedUri: Uri?,           // ưu tiên hiển thị cái user vừa chọn
    localPath: String?,        // fallback: path đã lưu trong state
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    val model: Any? = when {
        pickedUri != null -> pickedUri
        !localPath.isNullOrBlank() -> java.io.File(localPath)
        else -> null
    }

    Box(
        modifier = Modifier
            .size(96.dp)
            .combinedClickable(
                onClick = onPick,
                onLongClick = { if (model != null) onClear() } // long-press để xoá nhanh
            ),
        contentAlignment = Alignment.Center
    ) {
        // viền
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .borderGradient()
        )

        if (model != null) {
            // Có ảnh -> hiển thị
            AsyncImage(
                model = model,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
            )

            // overlay “đổi ảnh”
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Thay ảnh", color = Color.White, fontSize = 12.sp)
            }

            // nút xoá nhỏ
            TextButton(
                onClick = onClear,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 4.dp)
            ) { Text("Xoá", color = Color.White.copy(0.9f), fontSize = 12.sp) }
        } else {
            // Chưa có ảnh -> placeholder
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Chọn ảnh", color = Color.White.copy(0.9f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ChipsPreview(csv: String, modifier: Modifier = Modifier) {
    val items = remember(csv) { csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(12) }
    if (items.isEmpty()) return
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { chip ->
            Surface(
                color = Color.White.copy(.08f),
                shape = CircleShape,
                tonalElevation = 0.dp
            ) {
                Text(
                    chip,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/* ---- tiny util: gradient border for avatar ---- */
private fun Modifier.borderGradient(): Modifier = drawBehind {
    val stroke = 3.dp.toPx()
    val radius = size.minDimension / 2f
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0x66FFFFFF),
            Color(0x33FFFFFF),
            Color(0x66FFFFFF)
        )
    )
    drawCircle(
        brush = gradient,
        radius = radius,
        style = Stroke(width = stroke)
    )
}
