package com.mentorme.app.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mentorme.app.core.utils.copyUriToCache
import com.mentorme.app.ui.auth.BigGlassButton
import com.mentorme.app.ui.auth.FloatingLogo
import com.mentorme.app.ui.auth.GlassFormContainer
import com.mentorme.app.ui.auth.GlassInput
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext

@Composable
fun MentorOnboardingScreen(
    onBack: () -> Unit,
    onDoneGoHome: () -> Unit,
    onGoToReview: () -> Unit
) {
    val vm: OnboardingViewModel = hiltViewModel()
    val ui by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Chọn ảnh -> copy vào cache -> set avatarPath
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    val pickImage = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        pickedUri = uri
        uri?.let {
            copyUriToCache(context, it, "avatar_${System.currentTimeMillis()}.jpg")
                ?.let { path -> vm.setAvatarPath(path) }
        }
    }

    // Điều hướng theo next từ server (mentor thường next = /onboarding/review)
    LaunchedEffect(ui.success, ui.next, ui.isLoading) {
        if (!ui.isLoading && ui.success == true) {
            if (ui.next == "/onboarding/review") onGoToReview() else onDoneGoHome()
        }
    }

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

        Text("Đăng ký Mentor", fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Color.White)
        Text("Hoàn tất hồ sơ để gửi duyệt 📩", color = Color.White.copy(.8f), fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))

        GlassFormContainer {

            // 1) Avatar
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                AvatarPicker(
                    pickedUri = pickedUri,
                    localPath = ui.avatarPath,
                    onPick = { pickImage.launch("image/*") },
                    onClear = {
                        pickedUri = null
                        vm.setAvatarPath(null)
                    }
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Ảnh đại diện", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Bắt buộc • Giúp tăng độ tin cậy", color = Color.White.copy(.7f), fontSize = 12.sp)
                }
            }

            Divider(Modifier.padding(vertical = 10.dp), color = Color.White.copy(.08f))

            // 2) Thông tin cơ bản (chung)
            SectionTitle("Thông tin cơ bản")
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
            GlassInput(
                value = ui.languages,
                onValueChange = { vm.update { st -> st.copy(languages = it) } },
                label = "Ngôn ngữ * (phẩy phân tách)",
                placeholder = "vi,en"
            )
            ChipsPreview(csv = ui.languages, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(6.dp))
            GlassInput(
                value = ui.skills,
                onValueChange = { vm.update { st -> st.copy(skills = it) } },
                label = "Kỹ năng * (phẩy phân tách)",
                placeholder = "kotlin,android,leadership"
            )
            ChipsPreview(csv = ui.skills, modifier = Modifier.padding(top = 6.dp))

            // 3) Thông tin mentor (bắt buộc)
            SectionTitle("Thông tin Mentor")
            GlassInput(
                value = ui.jobTitle ?: "",
                onValueChange = { vm.update { st -> st.copy(jobTitle = it) } },
                label = "Chức danh *",
                placeholder = "Senior Android Engineer"
            )
            GlassInput(
                value = ui.experience ?: "",
                onValueChange = { vm.update { st -> st.copy(experience = it) } },
                label = "Kinh nghiệm *",
                placeholder = "8+ năm Android, 3 năm mentoring"
            )
            GlassInput(
                value = ui.headline ?: "",
                onValueChange = { vm.update { st -> st.copy(headline = it) } },
                label = "Tiêu đề nổi bật *",
                placeholder = "Giúp bạn lên Senior Android trong 6 tháng"
            )
            GlassInput(
                value = ui.mentorReason ?: "",
                onValueChange = { vm.update { st -> st.copy(mentorReason = it) } },
                label = "Lý do muốn mentor *",
                placeholder = "Đam mê chia sẻ, xây đội ngũ..."
            )
            GlassInput(
                value = ui.greatestAchievement ?: "",
                onValueChange = { vm.update { st -> st.copy(greatestAchievement = it) } },
                label = "Thành tựu lớn nhất *",
                placeholder = "Dẫn dắt team 10 người, top 1 app ..."
            )
            // Tuỳ chọn
            GlassInput(
                value = ui.bio ?: "",
                onValueChange = { vm.update { st -> st.copy(bio = it) } },
                label = "Giới thiệu ngắn (tuỳ chọn)",
                placeholder = "Tóm tắt phong cách mentoring, lĩnh vực mạnh..."
            )
            GlassInput(
                value = ui.introVideo ?: "",
                onValueChange = { vm.update { st -> st.copy(introVideo = it) } },
                label = "Intro video URL (tuỳ chọn)",
                placeholder = "https://..."
            )

            // Error
            AnimatedVisibility(visible = !ui.error.isNullOrBlank(), modifier = Modifier.padding(top = 8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = .15f),
                    tonalElevation = 0.dp,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(ui.error ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
                }
            }

            // Submit
            Spacer(Modifier.height(6.dp))
            BigGlassButton(
                text = if (ui.isLoading) "Đang gửi duyệt..." else "Gửi duyệt mentor",
                subText = null,
                enabled = !ui.isLoading,
                icon = {
                    if (ui.isLoading) {
                        CircularProgressIndicator(strokeWidth = 2.2.dp, modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Check, null, tint = Color.White)
                    }
                },
                onClick = { vm.submit() } // ViewModel sẽ gửi cùng endpoint /profile/required
            )
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack) { Text("← Quay lại", color = Color.White.copy(0.8f)) }
        Spacer(Modifier.height(6.dp))
    }
}
