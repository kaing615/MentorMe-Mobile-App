package com.mentorme.app.ui.layout

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mentorme.app.ui.theme.ActiveNavGreen

// ===== Model =====
private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val showDot: Boolean = false
)

// Navigation items cho Mentee (5 tabs)
private val menteeNavItems = listOf(
    NavItem("home",     "Trang chủ", Icons.Filled.Home),
    NavItem("search",   "Tìm kiếm", Icons.Filled.Search),
    NavItem("calendar", "Lịch hẹn", Icons.Filled.CalendarMonth),
    NavItem("messages", "Tin nhắn", Icons.AutoMirrored.Filled.Chat),
    NavItem("notifications", "Thông báo", Icons.Filled.Notifications),
    NavItem("profile",  "Cá nhân",  Icons.Filled.Person)
)

// Navigation items cho Mentor (4 tabs)
private val mentorNavItems = listOf(
    NavItem("mentor_dashboard", "Dashboard", Icons.Filled.Home),
    NavItem("mentor_calendar",  "Lịch hẹn",  Icons.Filled.CalendarMonth),
    NavItem("mentor_messages",  "Tin nhắn",  Icons.AutoMirrored.Filled.Chat),
    NavItem("notifications",    "Thông báo", Icons.Filled.Notifications),
    NavItem("mentor_profile",   "Cá nhân",   Icons.Filled.Person)
)

// ===== Public API =====
@Composable
fun GlassBottomBar(
    navController: NavHostController,
    userRole: String = "mentee", // Thêm tham số userRole với default là mentee
    notificationUnreadCount: Int = 0,
    calendarPendingCount: Int = 0,
    messageUnreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route
    val currentBaseRoute = currentRoute?.substringBefore("?")

    val showNotificationDot = notificationUnreadCount > 0
    val showCalendarDot = calendarPendingCount > 0
    val showMessagesDot = messageUnreadCount > 0

    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .clip(shape)
                .shadow(20.dp, shape, clip = false)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)), shape)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val baseNavItems = if (userRole == "mentor") mentorNavItems else menteeNavItems
            val navItems = baseNavItems.map { item ->
                val showDot = when (item.route) {
                    "notifications" -> showNotificationDot
                    "calendar", "mentor_calendar" -> showCalendarDot
                    "messages", "mentor_messages" -> showMessagesDot
                    else -> false
                }
                item.copy(showDot = showDot)
            }

            navItems.forEach { item ->
                val selected = currentDestination.isSelected(item.route)

                GlassBarItem(
                    selected = selected,
                    label = item.label,
                    icon = item.icon,
                    showDot = item.showDot
                ) {
                    // ✅ FIX: Không dựa vào `selected` để quyết định navigate.
                    // `selected` có thể bị tính sai khi restoreState/saveState hoặc route có args.
                    val targetRoute = getTargetRoute(item.route, userRole)

                    Log.d(
                        "GlassBottomBar",
                        "tap label='${item.label}' itemRoute='${item.route}' currentRoute='${currentRoute}' targetRoute='${targetRoute}' role='${userRole}'"
                    )

                    // So sánh route hiện tại với route đích để tránh navigate lặp.
                    if (currentBaseRoute != targetRoute) {
                        // ✅ Nếu destination đã tồn tại trong back stack thì pop về luôn (ổn định hơn restoreState).
                        val popped = navController.popBackStack(targetRoute, inclusive = false)
                        if (popped) {
                            Log.d("GlassBottomBar", "popBackStack -> $targetRoute")
                            return@GlassBarItem
                        }

                        Log.d("GlassBottomBar", "navigate -> $targetRoute")
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }
}


// ===== Item (ripple API mới, tô đậm khi chọn) =====
@SuppressLint("RememberInComposition")
@Composable
private fun GlassBarItem(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    showDot: Boolean,
    onClick: () -> Unit
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "icon-scale"
    )
    val activeItemColor = ActiveNavGreen
    val textColor by animateColorAsState(
        if (selected) activeItemColor else Color.White.copy(alpha = 0.78f),
        label = "text-color"
    )
    val iconTint by animateColorAsState(
        if (selected) activeItemColor else Color.White.copy(alpha = 0.88f),
        label = "icon-tint"
    )

    Column( // Thay đổi từ Row thành Column
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp) // Điều chỉnh padding cho layout vertical
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.28f)),
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally, // Center theo chiều ngang
        verticalArrangement = Arrangement.spacedBy(3.dp) // Khoảng cách giữa icon và text
    ) {
        if (showDot) {
            BadgedBox(
                badge = {
                    Badge()
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp).scale(iconScale)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp).scale(iconScale)
            )
        }
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp, // Giảm font size để vừa với layout vertical
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ===== Helpers =====
private fun NavDestination?.isSelected(route: String): Boolean {
    return this?.hierarchy?.any { destination ->
        destination.route?.substringBefore("?") == route
    } == true
}

private fun getTargetRoute(route: String, userRole: String): String {
    return when (route) {
        // Mentor routes
        "mentor_dashboard" -> "mentor_dashboard"
        "mentor_calendar" -> "mentor_calendar"
        "mentor_messages" -> "mentor_messages"
        "mentor_profile" -> "mentor_profile"
        "notifications" -> "notifications"

        // Mentee routes
        "home" -> "home"
        "search" -> "search"
        "calendar" -> "calendar"
        "messages" -> "messages"
        "profile" -> "profile"

        else -> if (userRole == "mentor") "mentor_dashboard" else "home"
    }
}

