package com.mentorme.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mentorme.app.ui.calendar.CalendarScreen
import com.mentorme.app.ui.home.HomeScreen
import com.mentorme.app.ui.layout.GlassBottomBar
import com.mentorme.app.ui.layout.HeaderBar
import com.mentorme.app.ui.layout.UserUi
import com.mentorme.app.ui.profile.ProfileScreen
import com.mentorme.app.ui.theme.LiquidBackground

object Routes {

    const val Mentors = "mentors"
    const val Home = "home"
    const val Calendar = "calendar"
    const val Messages = "messages"
    const val Profile = "profile"
}

@Composable
fun AppNav(
    nav: NavHostController = rememberNavController(),
    user: UserUi? = UserUi("Alice", avatar = null, role = "mentee")
) {
    Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
    ) {
        // Nền liquid gradient bao phủ toàn màn hình - sẽ hiển thị qua các bar trong suốt
        LiquidBackground(
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        )

        // Scaffold với nền trong suốt hoàn toàn
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets(0), // Loại bỏ padding mặc định
//            topBar = {
//                // Header bar hoàn toàn trong suốt, nổi trên nội dung
//                HeaderBar(
//                    user = user,
//                    onLoginClick = { /* TODO: open login screen */ },
//                    onRegisterClick = { /* TODO: open register screen */ },
//                    onProfileClick = { nav.navigate(Routes.Profile) },
//                    onLogout = { /* TODO: handle logout */ },
//                    modifier = androidx.compose.ui.Modifier.statusBarsPadding()
//                )
//            },
            bottomBar = {
                // Bottom nav hoàn toàn trong suốt, nổi trên nội dung
                GlassBottomBar(navController = nav)
            },
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        ) { paddingValues ->
            // Nội dung màn hình với padding để tránh che khuất bởi các bar
            Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NavHost(
                    navController = nav,
                    startDestination = Routes.Home,
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                ) {
                    composable(Routes.Home) { HomeScreen() }
                    composable(Routes.Calendar) { CalendarScreen() }
                    composable(Routes.Profile) { ProfileScreen() }
                }
            }
        }
    }
}
