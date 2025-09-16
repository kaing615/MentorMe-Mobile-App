package com.mentorme.app.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mentorme.app.ui.navigation.Routes
import com.mentorme.app.ui.theme.liquidGlassStrong

@Composable
fun BottomNavigationBar(
    nav: NavHostController,
    role: String = "mentee",
    unreadMessages: Int = 0,
    upcomingSessions: Int = 0
) {
    data class Tab(
        val route: String,
        val label: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val badge: Int = 0
    )

    val mentorTabs = listOf(
        Tab(Routes.Home, "Dashboard", Icons.Filled.BarChart),
        Tab(Routes.Calendar, "Lịch hẹn", Icons.Filled.DateRange, badge = upcomingSessions),
        Tab(Routes.Messages, "Tin nhắn", Icons.AutoMirrored.Filled.Message, badge = unreadMessages)
    )
    val menteeTabs = listOf(
        Tab(Routes.Home, "Trang chủ", Icons.Filled.Home),
        Tab(Routes.Mentors, "Tìm kiếm", Icons.Filled.Search),
        Tab(Routes.Calendar, "Lịch hẹn", Icons.Filled.DateRange, badge = upcomingSessions),
        Tab(Routes.Messages, "Tin nhắn", Icons.AutoMirrored.Filled.Message, badge = unreadMessages)
    )
    val tabs = if (role == "mentor") mentorTabs else menteeTabs

    Box(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .liquidGlassStrong() // 👈 hiệu ứng glass mạnh
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            val backStack by nav.currentBackStackEntryAsState()
            val current = backStack?.destination?.route

            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = current == tab.route,
                    onClick = {
                        nav.navigate(tab.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Routes.Home) { saveState = true }
                        }
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (tab.badge > 0) {
                                    Badge {
                                        Text(tab.badge.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(tab.icon, contentDescription = tab.label)
                        }
                    },
                    label = { Text(tab.label) }
                )
            }
        }
    }
}
