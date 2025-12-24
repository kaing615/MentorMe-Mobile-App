package com.mentorme.app.ui.search.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mentorme.app.data.dto.availability.slotPriceVndOrNull
import com.mentorme.app.data.dto.profile.ProfileDto
import com.mentorme.app.domain.usecase.availability.GetPublicCalendarUseCase
import com.mentorme.app.domain.usecase.profile.GetPublicProfileUseCase
import com.mentorme.app.ui.common.glassOutlinedTextFieldColors
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.home.Mentor as HomeMentor
import com.mentorme.app.ui.theme.LiquidGlassCard
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

private data class SlotChoice(
    val occurrenceId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val durationMins: Int,
    val priceVnd: Long
)

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BookSessionDeps {
    fun getPublicCalendarUseCase(): GetPublicCalendarUseCase
    fun getPublicProfileUseCase(): GetPublicProfileUseCase
}

@Composable
fun BookSessionContent(
    mentor: HomeMentor,
    onClose: () -> Unit,
    onConfirm: (
        occurrenceId: String,
        date: String,
        startTime: String,
        endTime: String,
        priceVnd: Long,
        note: String
    ) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val deps = remember(context) { EntryPointAccessors.fromApplication(context, BookSessionDeps::class.java) }
    val getCalendar = remember { deps.getPublicCalendarUseCase() }
    val getProfile = remember { deps.getPublicProfileUseCase() }

    var slots by remember { mutableStateOf<List<SlotChoice>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedSlot by remember { mutableStateOf<SlotChoice?>(null) }
    var note by remember { mutableStateOf("") }
    var profile by remember { mutableStateOf<ProfileDto?>(null) }

    val nf = remember { java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }
    val dateFmt = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val timeFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }

    LaunchedEffect(mentor.id) {
        loading = true
        errorMessage = null
        val zone = ZoneId.systemDefault()
        val todayStartLocal = java.time.LocalDate.now(zone).atStartOfDay(zone)
        val fromIsoUtc = todayStartLocal.toInstant().toString()
        val toIsoUtc = todayStartLocal.plusDays(30).withHour(23).withMinute(59).withSecond(59).toInstant().toString()

        when (val res = getCalendar(mentor.id, fromIsoUtc, toIsoUtc, includeClosed = true)) {
            is com.mentorme.app.core.utils.AppResult.Success -> {
                val items = res.data
                slots = items.mapNotNull { item ->
                    val s = item.start ?: return@mapNotNull null
                    val e = item.end ?: return@mapNotNull null
                    if (item.status?.lowercase() != "open") return@mapNotNull null
                    val startZ = runCatching { Instant.parse(s).atZone(zone) }.getOrNull() ?: return@mapNotNull null
                    val endZ = runCatching { Instant.parse(e).atZone(zone) }.getOrNull() ?: return@mapNotNull null
                    val duration = java.time.Duration.between(startZ, endZ).toMinutes().toInt()
                    val occurrenceId = item.id?.ifBlank { null } ?: "${s}_${e}"
                    val fallbackRate = mentor.hourlyRate.takeIf { it > 0 }
                    val priceVnd = item.slotPriceVndOrNull()?.toLong()
                        ?: fallbackRate?.let { rate ->
                            val hours = duration.toDouble() / 60.0
                            (rate * hours).roundToLong()
                        }
                        ?: 0L
                    SlotChoice(
                        occurrenceId = occurrenceId,
                        date = dateFmt.format(startZ),
                        startTime = timeFmt.format(startZ),
                        endTime = timeFmt.format(endZ),
                        durationMins = duration,
                        priceVnd = priceVnd
                    )
                }.sortedWith(compareBy({ it.date }, { it.startTime }))
                loading = false
            }
            is com.mentorme.app.core.utils.AppResult.Error -> {
                slots = emptyList()
                errorMessage = res.throwable
                loading = false
            }
            com.mentorme.app.core.utils.AppResult.Loading -> Unit
        }
    }

    LaunchedEffect(mentor.id) {
        val idSafe = mentor.id.trim()
        if (idSafe.isBlank()) {
            profile = null
            return@LaunchedEffect
        }
        profile = null
        when (val res = getProfile(idSafe)) {
            is com.mentorme.app.core.utils.AppResult.Success -> {
                profile = res.data
            }
            is com.mentorme.app.core.utils.AppResult.Error -> {
            }
            com.mentorme.app.core.utils.AppResult.Loading -> Unit
        }
    }

    LaunchedEffect(slots) {
        val current = selectedSlot
        if (current != null && slots.none { it.occurrenceId == current.occurrenceId }) {
            selectedSlot = null
        }
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Đặt lịch tư vấn",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
            }

            MentorSummaryCard(mentor = mentor, profile = profile, priceFormatter = nf)

            SectionHeader(
                title = "Bước 1: Chọn khung giờ",
                caption = "Hiển thị theo múi giờ thiết bị."
            )

            if (!loading && errorMessage == null && slots.isNotEmpty()) {
                Text(
                    "${slots.size} khung giờ trống trong 30 ngày tới",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            when {
                loading -> LoadingSlotsCard()
                errorMessage != null -> ErrorSlotsCard(message = errorMessage!!)
                slots.isEmpty() -> EmptySlotsCard()
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        slots.forEach { slot ->
                            SlotChoiceCard(
                                slot = slot,
                                selected = selectedSlot?.occurrenceId == slot.occurrenceId,
                                priceFormatter = nf,
                                onSelect = { selectedSlot = slot }
                            )
                        }
                    }
                }
            }

            SectionHeader(
                title = "Bước 2: Ghi chú cho mentor",
                caption = "Giúp mentor hiểu mục tiêu của bạn (tùy chọn)."
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Nội dung ghi chú") },
                colors = glassOutlinedTextFieldColors(),
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedSlot != null) {
                SelectedSlotCard(slot = selectedSlot!!, priceFormatter = nf)
            } else if (!loading && errorMessage == null) {
                SelectionHintCard()
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MMGhostButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp)
                ) { Text("Hủy") }
                MMPrimaryButton(
                    onClick = {
                        val slot = selectedSlot ?: return@MMPrimaryButton
                        onConfirm(
                            slot.occurrenceId,
                            slot.date,
                            slot.startTime,
                            slot.endTime,
                            slot.priceVnd,
                            note
                        )
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                    enabled = selectedSlot != null
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Xác nhận đặt lịch")
                }
                if (selectedSlot == null) {
                    Text(
                        "Chọn một khung giờ để tiếp tục.",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, caption: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (!caption.isNullOrBlank()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MentorSummaryCard(
    mentor: HomeMentor,
    profile: ProfileDto?,
    priceFormatter: java.text.NumberFormat
) {
    val profileName = profile?.fullName?.trim().orEmpty()
    val displayName = if (profileName.isNotBlank()) profileName else mentor.name.ifBlank { "Mentor" }
    val profileRole = profile?.jobTitle?.trim().orEmpty()
    val displayRole = if (profileRole.isNotBlank()) profileRole else mentor.role.ifBlank { "Mentor" }
    val company = mentor.company.trim().takeIf { it.isNotBlank() }
    val headline = profile?.headline?.trim().takeIf { it.isNotBlank() }
    val skills = (profile?.skills?.filter { it.isNotBlank() } ?: mentor.skills).filter { it.isNotBlank() }
    val languages = profile?.languages?.filter { it.isNotBlank() } ?: emptyList()
    val avatarUrl = profile?.avatarUrl?.trim().takeIf { it.isNotBlank() }
        ?: mentor.imageUrl.trim().takeIf { it.isNotBlank() }
    val initials = displayName
        .trim()
        .split(Regex("\s+"))
        .filter { it.isNotBlank() }
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
    val subtitle = listOfNotNull(displayRole.takeIf { it.isNotBlank() }, company).joinToString(" • ")

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 24.dp,
        alpha = 0.18f,
        borderAlpha = 0.35f
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!headline.isNullOrBlank()) {
                        Text(
                            headline,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            String.format(Locale("vi", "VN"), "%.1f", mentor.rating),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Text(
                        "${mentor.totalReviews} đánh giá",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            InfoPill(
                icon = Icons.Default.AttachMoney,
                text = "${priceFormatter.format(mentor.hourlyRate.toLong())}/giờ"
            )

            if (skills.isNotEmpty()) {
                Text(
                    skills.take(4).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (languages.isNotEmpty()) {
                Text(
                    "Ngôn ngữ: ${languages.take(3).joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SlotChoiceCard(
    slot: SlotChoice,
    selected: Boolean,
    priceFormatter: java.text.NumberFormat,
    onSelect: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        radius = 20.dp,
        alpha = if (selected) 0.22f else 0.12f,
        borderAlpha = if (selected) 0.45f else 0.28f
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        slot.date,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${slot.startTime} - ${slot.endTime}",
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                SelectionIndicator(selected = selected)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoPill(icon = Icons.Default.Schedule, text = "${slot.durationMins} phút")
                InfoPill(icon = Icons.Default.AttachMoney, text = priceFormatter.format(slot.priceVnd))
            }
        }
    }
}

@Composable
private fun SelectionIndicator(selected: Boolean) {
    val icon = if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked
    val tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
    Icon(icon, contentDescription = null, tint = tint)
}

@Composable
private fun SelectedSlotCard(slot: SlotChoice, priceFormatter: java.text.NumberFormat) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 22.dp,
        alpha = 0.2f,
        borderAlpha = 0.38f
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E))
                Text(
                    "Tóm tắt đặt lịch",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text("${slot.date} • ${slot.startTime} - ${slot.endTime}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoPill(icon = Icons.Default.Schedule, text = "${slot.durationMins} phút")
                InfoPill(icon = Icons.Default.AttachMoney, text = priceFormatter.format(slot.priceVnd))
            }
        }
    }
}

@Composable
private fun SelectionHintCard() {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 20.dp,
        alpha = 0.14f,
        borderAlpha = 0.28f
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.85f))
            Text("Chọn một khung giờ để xem tóm tắt và xác nhận.", color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun LoadingSlotsCard() {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 20.dp,
        alpha = 0.14f,
        borderAlpha = 0.28f
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
            Text("Đang tải lịch trống...", color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun EmptySlotsCard() {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 20.dp,
        alpha = 0.14f,
        borderAlpha = 0.28f
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Chưa có lịch trống trong 30 ngày tới", fontWeight = FontWeight.SemiBold)
            Text(
                "Hãy quay lại sau hoặc nhắn mentor để hỏi thêm.",
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun ErrorSlotsCard(message: String) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 20.dp,
        alpha = 0.14f,
        borderAlpha = 0.28f
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Không thể tải lịch trống", fontWeight = FontWeight.SemiBold)
            Text(message, color = Color.White.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun InfoPill(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(14.dp)
            )
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}
