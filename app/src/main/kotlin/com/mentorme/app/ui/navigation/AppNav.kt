package com.mentorme.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mentorme.app.ui.calendar.CalendarScreen
import com.mentorme.app.ui.home.HomeScreen
import com.mentorme.app.ui.layout.BottomNavigationBar
import com.mentorme.app.ui.layout.HeaderBar
import com.mentorme.app.ui.layout.UserUi
import com.mentorme.app.ui.mentors.MentorsScreen
import com.mentorme.app.ui.messages.MessagesScreen
import com.mentorme.app.ui.profile.ProfileScreen

object Routes {
    const val Home = "home"
    const val Mentors = "mentors"
    const val Calendar = "calendar"
    const val Messages = "messages"
    const val Profile = "profile"
}

@Composable
fun AppNav(
    nav: NavHostController = rememberNavController(),
    user: UserUi? = UserUi("Alice", avatar = null, role = "mentee")
) {
    Scaffold(
        topBar = {
            HeaderBar(
                user = user,
                onLoginClick = { /* TODO: open login screen */ },
                onRegisterClick = { /* TODO: open register screen */ },
                onProfileClick = { nav.navigate(Routes.Profile) },
                onLogout = { /* TODO: handle logout */ }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                nav = nav,
                role = user?.role ?: "mentee",
                unreadMessages = 2,
                upcomingSessions = 1
            )
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Routes.Home,
            modifier = Modifier.padding(inner)
        ) {
            composable(Routes.Home) { HomeScreen() }
            composable(Routes.Mentors) { MentorsScreen() }
            composable(Routes.Calendar) { CalendarScreen() }
            composable(Routes.Messages) { MessagesScreen() }
            composable(Routes.Profile) { ProfileScreen() }
        }
    }
}
