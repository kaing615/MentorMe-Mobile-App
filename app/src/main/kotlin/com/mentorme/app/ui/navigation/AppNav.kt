package com.mentorme.app.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.booking.BookingChooseTimeScreen
import com.mentorme.app.ui.booking.BookingDraft
import com.mentorme.app.ui.booking.BookingSummaryScreen
import com.mentorme.app.ui.calendar.CalendarScreen
import com.mentorme.app.ui.home.HomeScreen
import com.mentorme.app.ui.layout.GlassBottomBar
import com.mentorme.app.ui.layout.UserUi
import com.mentorme.app.ui.theme.LiquidBackground

object Routes {
    const val Auth = "auth"
    const val Home = "home"
    const val Mentors = "mentors"
    const val Calendar = "calendar"
    const val Messages = "messages"
    const val Profile = "profile"
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNav(
    nav: NavHostController = rememberNavController(),
    user: UserUi? = UserUi("Alice", avatar = null, role = "mentee")
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Nền liquid
        LiquidBackground(
            modifier = Modifier
                .matchParentSize()
                .zIndex(-1f)
        )

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            bottomBar = { GlassBottomBar(navController = nav) },
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = nav,
                    startDestination = Routes.Home,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Routes.Home) { HomeScreen() }

                    composable(Routes.Calendar) {
                        CalendarScreen(
                            bookings = MockData.mockBookings,
                            onJoinSession = { /* TODO */ },
                            onRate = { /* TODO */ },
                            onRebook = { b -> nav.navigate("booking/${b.mentorId}") },
                            onCancel = { /* TODO */ }
                        )
                    }

                    composable(Routes.Profile) { /* ProfileScreen() */ }

                    // Step 1: chọn thời gian
                    composable("booking/{mentorId}") { backStackEntry ->
                        val mentorId = backStackEntry.arguments?.getString("mentorId") ?: return@composable
                        val mentor = MockData.mockMentors.find { it.id == mentorId }
                        mentor?.let { m ->
                            BookingChooseTimeScreen(
                                mentor = m,
                                // ✅ truyền đủ 2 tham số còn thiếu
                                availableDates = listOf("2025-09-20","2025-09-21","2025-09-22","2025-09-23","2025-09-24"),
                                availableTimes = listOf("09:00","10:00","11:00","14:00","15:00","16:00","17:00"),
                                onNext = { d: BookingDraft ->
                                    nav.navigate("bookingSummary/${m.id}/${d.date}/${d.time}/${d.durationMin}")
                                },
                                onClose = { nav.popBackStack() }
                            )
                        }
                    }

                    // Step 2: xác nhận
                    composable("bookingSummary/{mentorId}/{date}/{time}/{duration}") { backStackEntry ->
                        val mentorId = backStackEntry.arguments?.getString("mentorId") ?: return@composable
                        val date = backStackEntry.arguments?.getString("date") ?: ""
                        val time = backStackEntry.arguments?.getString("time") ?: ""
                        val duration = backStackEntry.arguments?.getString("duration")?.toIntOrNull() ?: 60
                        val mentor = MockData.mockMentors.find { it.id == mentorId }
                        mentor?.let { m ->
                            BookingSummaryScreen(
                                mentor = m,
                                draft = BookingDraft(
                                    mentorId = mentorId,
                                    date = date,
                                    time = time,
                                    durationMin = duration,
                                    hourlyRate = m.hourlyRate
                                ),
                                // ✅ truyền currentUserId để hết lỗi
                                currentUserId = "current-user-id",
                                onConfirmed = {
                                    // TODO: save booking
                                    nav.popBackStack(route = Routes.Home, inclusive = false)
                                },
                                onBack = { nav.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
