package com.mentorme.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    // Main navigation screens
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Mentors : Screen("mentors", "Mentors", Icons.Default.Person)
    object Bookings : Screen("bookings", "Bookings", Icons.Default.DateRange)
    object Messages : Screen("messages", "Messages", Icons.Default.Message)
    object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle)
    object Search : Screen("search", "Search", Icons.Filled.Search)

    // Auth screens
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")

    // Detail screens
    object MentorDetail : Screen("mentor_detail/{mentorId}", "Mentor Details") {
        fun createRoute(mentorId: String) = "mentor_detail/$mentorId"
    }

    object BookingDetail : Screen("booking_detail/{bookingId}", "Booking Details") {
        fun createRoute(bookingId: String) = "booking_detail/$bookingId"
    }

    object Chat : Screen("chat/{bookingId}", "Chat") {
        fun createRoute(bookingId: String) = "chat/$bookingId"
    }

    object VideoCall : Screen("video_call/{bookingId}", "Video Call") {
        fun createRoute(bookingId: String) = "video_call/$bookingId"
    }

    // Additional screens
    object Calendar : Screen("calendar", "Calendar")
    object Notifications : Screen("notifications", "Notifications")
    object Settings : Screen("settings", "Settings")
    object Dashboard : Screen("dashboard", "Dashboard")

    // Wallet screens
    object TopUp : Screen("wallet/topup", "Top Up")
    object Withdraw : Screen("wallet/withdraw", "Withdraw")
}

val bottomNavigationItems = listOf(
    Screen.Home,
    Screen.Mentors,
    Screen.Bookings,
    Screen.Messages,
    Screen.Profile,
    Screen.Search
)
