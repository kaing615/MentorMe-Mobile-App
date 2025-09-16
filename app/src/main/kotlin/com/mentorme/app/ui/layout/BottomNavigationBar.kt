package com.mentorme.app.ui.layout

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.ui.theme.liquidGlassStrong

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

private val navItems = listOf(
    NavItem("home",     "Trang chủ", Icons.Filled.Home),
    NavItem("search",   "Tìm kiếm", Icons.Filled.Search),
    NavItem("calendar", "Lịch hẹn", Icons.Filled.CalendarMonth, badgeCount = 1),
    NavItem("messages", "Tin nhắn", Icons.Filled.Chat,          badgeCount = 2),
    NavItem("profile",  "Cá nhân",  Icons.Filled.Person)
)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Khối bao ngoài để "nâng" thanh lên, bo góc lớn và tránh khu vực system bars.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()     // tránh khu vực gesture bar
            .padding(bottom = 10.dp)     // cách mép dưới cho cảm giác nổi
    ) {
        val shape = RoundedCornerShape(28.dp)

        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .clip(shape)
                .liquidGlassStrong(radius = 28.dp) // Giữ liquid glass effect
                .shadow(elevation = 20.dp, shape = shape, clip = false),
            containerColor = Color.Transparent, // Nền hoàn toàn trong suốt
            tonalElevation = 0.dp,
            contentColor = Color.Transparent // Đảm bảo không có màu nền nào
        ) {
            navItems.forEach { item ->
                val selected = currentDestination isSelected item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        if (item.badgeCount > 0) {
                            BadgedBox(badge = { Badge { Text(item.badgeCount.toString()) } }) {
                                Icon(item.icon, contentDescription = item.label)
                            }
                        } else {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    },
                    label = { Text(item.label) },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor     = Color.White.copy(alpha = 0.10f), // viên highlight nhẹ
                        unselectedIconColor = Color.White.copy(alpha = 0.75f),
                        unselectedTextColor = Color.White.copy(alpha = 0.75f)
                    )
                )
            }
        }
    }
}

private infix fun NavDestination?.isSelected(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}
