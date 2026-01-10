package com.mentorme.app.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.ui.components.ui.GlassOverlay
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.theme.LiquidGlassCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel để load profile data
@HiltViewModel
class ProfileSheetViewModel @Inject constructor(
    private val api: MentorMeApi
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    fun loadProfile(userId: String, role: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                if (role == "mentor") {
                    // Load mentor info từ mentors API
                    android.util.Log.d("ProfileSheet", "📡 Loading mentor profile for userId: $userId")
                    val response = api.getMentor(userId)
                    android.util.Log.d("ProfileSheet", "📥 Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")

                    if (response.isSuccessful) {
                        val envelope = response.body()
                        android.util.Log.d("ProfileSheet", "📦 Envelope: $envelope")
                        val mentorData = envelope?.data
                        android.util.Log.d("ProfileSheet", "👤 Mentor data: $mentorData")

                        if (mentorData != null) {
                            android.util.Log.d("ProfileSheet", "✅ Mentor profile loaded:")
                            android.util.Log.d("ProfileSheet", "  - name: ${mentorData.name}")
                            android.util.Log.d("ProfileSheet", "  - avatarUrl: ${mentorData.avatarUrl}")
                            android.util.Log.d("ProfileSheet", "  - phone: ${mentorData.phone}")
                            android.util.Log.d("ProfileSheet", "  - bio: ${mentorData.bio}")
                            android.util.Log.d("ProfileSheet", "  - hourlyRate: ${mentorData.hourlyRate}")
                            android.util.Log.d("ProfileSheet", "  - skills: ${mentorData.skills}")
                            android.util.Log.d("ProfileSheet", "  - languages: ${mentorData.languages}")
                            android.util.Log.d("ProfileSheet", "  - category: ${mentorData.category}")

                            _profileState.value = ProfileState.Success(
                                name = mentorData.name ?: "Mentor",
                                role = "Mentor",
                                avatar = mentorData.avatarUrl,
                                bio = mentorData.bio, // ✅ Giờ có từ backend
                                skills = mentorData.skills ?: emptyList(),
                                languages = mentorData.languages ?: emptyList(), // ✅ Giờ có từ backend
                                rating = mentorData.rating?.toDouble(),
                                reviewCount = mentorData.ratingCount,
                                sessionCount = null,
                                yearsOfExperience = null,
                                company = mentorData.company,
                                title = mentorData.role,
                                hourlyRate = mentorData.hourlyRateVnd ?: mentorData.hourlyRate, // ✅ Ưu tiên hourlyRateVnd
                                phone = mentorData.phone, // ✅ Giờ có từ backend
                                category = mentorData.category // ✅ Giờ có từ backend
                            )
                        } else {
                            android.util.Log.e("ProfileSheet", "❌ Mentor data is null")
                            _profileState.value = ProfileState.Error("Không tìm thấy thông tin")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("ProfileSheet", "❌ API error: ${response.message()}, body: $errorBody")
                        _profileState.value = ProfileState.Error("Không thể tải thông tin: ${response.message()}")
                    }
                } else {
                    // Load mentee info từ profile API
                    android.util.Log.d("ProfileSheet", "📡 Loading mentee profile for userId: $userId")
                    val response = api.getProfile(userId)
                    android.util.Log.d("ProfileSheet", "📥 Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")

                    if (response.isSuccessful) {
                        val envelope = response.body()
                        android.util.Log.d("ProfileSheet", "📦 Envelope: $envelope")
                        val profile = envelope?.data
                        android.util.Log.d("ProfileSheet", "👤 Profile data: $profile")

                        if (profile != null) {
                            android.util.Log.d("ProfileSheet", "✅ Profile loaded:")
                            android.util.Log.d("ProfileSheet", "  - fullName: ${profile.fullName}")
                            android.util.Log.d("ProfileSheet", "  - avatarUrl: ${profile.avatarUrl}")
                            android.util.Log.d("ProfileSheet", "  - phone: ${profile.phone}")
                            android.util.Log.d("ProfileSheet", "  - category: ${profile.category}")
                            android.util.Log.d("ProfileSheet", "  - skills: ${profile.skills}")
                            android.util.Log.d("ProfileSheet", "  - languages: ${profile.languages}")

                            _profileState.value = ProfileState.Success(
                                name = profile.fullName ?: "Mentee",
                                role = "Mentee",
                                avatar = profile.avatarUrl,
                                bio = profile.bio,
                                skills = profile.skills ?: emptyList(),
                                languages = profile.languages ?: emptyList(),
                                rating = null, // Mentee không có rating
                                reviewCount = null,
                                sessionCount = null,
                                yearsOfExperience = null,
                                company = null,
                                title = profile.jobTitle,
                                hourlyRate = null, // Mentee không có hourly rate
                                phone = profile.phone,
                                category = profile.category
                            )
                        } else {
                            android.util.Log.e("ProfileSheet", "❌ Profile data is null")
                            _profileState.value = ProfileState.Error("Không tìm thấy thông tin")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("ProfileSheet", "❌ API error: ${response.message()}, body: $errorBody")
                        _profileState.value = ProfileState.Error("Không thể tải thông tin: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Lỗi: ${e.message}")
            }
        }
    }

    sealed class ProfileState {
        object Loading : ProfileState()
        data class Success(
            val name: String,
            val role: String,
            val avatar: String?,
            val bio: String?,
            val skills: List<String>,
            val languages: List<String>,
            val rating: Double? = null,
            val reviewCount: Int? = null,
            val sessionCount: Int? = null,
            val yearsOfExperience: Int? = null,
            val company: String? = null,
            val title: String? = null,
            val hourlyRate: Int? = null,
            val phone: String? = null,
            val category: String? = null // Lĩnh vực quan tâm (mentee)
        ) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }
}

@Composable
fun ProfileSheet(
    name: String,
    role: String,
    peerId: String? = null,
    onClose: () -> Unit
) {
    val viewModel = hiltViewModel<ProfileSheetViewModel>()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()

    LaunchedEffect(peerId, role) {
        if (peerId != null) {
            viewModel.loadProfile(peerId, role)
        }
    }

    GlassOverlay(
        visible = true,
        onDismiss = onClose,
        formModifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp),
            radius = 24.dp
        ) {
            when (val state = profileState) {
                is ProfileSheetViewModel.ProfileState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is ProfileSheetViewModel.ProfileState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                state.message,
                                color = Color.White.copy(0.7f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            MMButton(text = "Đóng", onClick = onClose)
                        }
                    }
                }

                is ProfileSheetViewModel.ProfileState.Success -> {
                    ProfileContent(profile = state, onClose = onClose)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileContent(
    profile: ProfileSheetViewModel.ProfileState.Success,
    onClose: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header với Avatar
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.avatar != null) {
                        AsyncImage(
                            model = profile.avatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val initials = profile.name
                            .split(" ")
                            .mapNotNull { it.firstOrNull() }
                            .take(2)
                            .joinToString("")
                        Text(
                            text = initials,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Name
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Row: Role badge + Rating (nếu có) - Haze effect
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Role badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.35f))
                            .border(
                                BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.6f)),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = profile.role,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Rating badge (nếu có) - Nhỏ hơn, nổi bật
                    profile.rating?.let { rating ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFBBF24).copy(alpha = 0.4f))
                                .border(
                                    BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.6f)),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "%.1f".format(rating),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                if (profile.reviewCount != null && profile.reviewCount > 0) {
                                    Text(
                                        text = "(${profile.reviewCount})",
                                        color = Color.White.copy(0.7f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stats Row (Sessions, Experience) - Bỏ Rating vì đã ở trên
        if (profile.sessionCount != null || profile.yearsOfExperience != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.sessionCount?.let { count ->
                        StatBox(
                            icon = Icons.Default.VideoChat,
                            value = "$count",
                            label = "phiên tư vấn",
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    profile.yearsOfExperience?.let { years ->
                        StatBox(
                            icon = Icons.Default.WorkspacePremium,
                            value = "$years năm",
                            label = "kinh nghiệm",
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Hourly Rate & Phone Row - Thông tin quan trọng
        if (profile.hourlyRate != null || profile.phone != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.hourlyRate?.let { rate ->
                        InfoBox(
                            icon = Icons.Default.CreditCard,
                            label = "Giá/giờ",
                            value = "${rate.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")}₫",
                            color = Color(0xFFFBBF24),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    profile.phone?.let { phone ->
                        InfoBox(
                            icon = Icons.Default.Phone,
                            label = "Điện thoại",
                            value = phone,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Company & Title
        if (profile.company != null || profile.title != null) {
            item {
                InfoSection(
                    icon = Icons.Default.Work,
                    title = "Công việc",
                    content = buildString {
                        profile.title?.let { append(it) }
                        if (profile.title != null && profile.company != null) append(" tại ")
                        profile.company?.let { append(it) }
                    }
                )
            }
        }

        // Category (Lĩnh vực quan tâm) - Cho mentee
        if (!profile.category.isNullOrBlank()) {
            item {
                InfoSection(
                    icon = Icons.Default.Category,
                    title = "Lĩnh vực quan tâm",
                    content = profile.category
                )
            }
        }

        // Bio
        if (!profile.bio.isNullOrBlank()) {
            item {
                InfoSection(
                    icon = Icons.Default.Person,
                    title = "Giới thiệu",
                    content = profile.bio
                )
            }
        }

        // Skills
        if (profile.skills.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Kỹ năng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profile.skills.forEach { skill ->
                            SkillChip(text = skill)
                        }
                    }
                }
            }
        }

        // Languages
        if (profile.languages.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Ngôn ngữ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profile.languages.forEach { lang ->
                            LanguageChip(text = lang)
                        }
                    }
                }
            }
        }

        // Close button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            MMButton(
                text = "Đóng",
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatBox(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.35f))
            .border(
                BorderStroke(1.dp, color.copy(alpha = 0.6f)),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(0.7f)
            )
        }
    }
}

// InfoBox - Tương tự StatBox nhưng layout khác cho phone và hourly rate
@Composable
private fun InfoBox(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.35f))
            .border(
                BorderStroke(1.dp, color.copy(alpha = 0.6f)),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon nổi bật với background riêng
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun InfoSection(
    icon: ImageVector,
    title: String,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF60A5FA),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.9f)
            )
        }
    }
}

@Composable
private fun SkillChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF3B82F6).copy(alpha = 0.35f))
            .border(
                BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LanguageChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF10B981).copy(alpha = 0.35f))
            .border(
                BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
