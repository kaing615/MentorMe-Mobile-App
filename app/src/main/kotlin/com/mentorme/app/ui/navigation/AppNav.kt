package com.mentorme.app.ui.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mentorme.app.ui.auth.AuthScreen
import com.mentorme.app.ui.auth.RegisterPayload
import com.mentorme.app.ui.chat.ChatScreen
import com.mentorme.app.ui.chat.MessagesScreen


object Routes {
    const val Auth = "auth"
    const val Home = "home"
    const val Mentors = "mentors"
    const val Calendar = "calendar"
    const val Messages = "messages"
    const val Profile = "profile"
    const val Chat = "chat"
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNav(
    nav: NavHostController = rememberNavController(),
    user: UserUi? = UserUi("Alice", avatar = null, role = "mentee")
) {
    val backstack by nav.currentBackStackEntryAsState()
    val currentRoute = backstack?.destination?.route
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

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

            bottomBar = {
                val hideForChat =
                    currentRoute?.startsWith("${Routes.Chat}/") == true
                if (isLoggedIn && currentRoute != Routes.Auth && !hideForChat) {
                    GlassBottomBar(navController = nav)
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = nav,
                    startDestination = if (isLoggedIn) Routes.Home else Routes.Auth,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ---------- AUTH ----------
                    composable(Routes.Auth) {
                        AuthScreen(
                            onLogin = { _, _ ->
                                // Remove mock logic - let AuthScreen handle real authentication
                                false
                            },
                            onRegister = { p: RegisterPayload ->
                                // Remove mock logic - let AuthScreen handle real registration
                                false
                            },
                            onResetPassword = { email ->
                                // TODO: gọi API gửi mail reset, ví dụ:
                                // authRepository.sendResetLink(email)
                                // Có thể hiện snackbar/toast ở đây
                            },
                            onAuthed = {
                                isLoggedIn = true
                                nav.navigate(Routes.Home) {
                                    popUpTo(Routes.Auth) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // ---------- MAIN APP ----------
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

                    // AppNav.kt  (trong NavHost)
                    // Route danh sách
                    composable(Routes.Messages) {
                        MessagesScreen(onOpenConversation = { convId ->
                            nav.navigate("${Routes.Chat}/$convId")
                        })
                    }

// Route khung chat + ẩn bottom bar
                    composable("${Routes.Chat}/{conversationId}") { backStackEntry ->
                        val convId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
                        ChatScreen(
                            conversationId = convId,
                            onBack = { nav.popBackStack() },
                            onJoinSession = { /* TODO: VideoCall */ }
                        )
                    }



                    composable(Routes.Profile) { /* ProfileScreen() */ }


                        // Step 1: chọn thời gian
                        composable("booking/{mentorId}") { backStackEntry ->
                            val mentorId =
                                backStackEntry.arguments?.getString("mentorId")
                                    ?: return@composable
                            val mentor =
                                MockData.mockMentors.find { it.id == mentorId }
                            mentor?.let { m ->
                                BookingChooseTimeScreen(
                                    mentor = m,
                                    // ✅ truyền đủ 2 tham số còn thiếu
                                    availableDates = listOf(
                                        "2025-09-20",
                                        "2025-09-21",
                                        "2025-09-22",
                                        "2025-09-23",
                                        "2025-09-24"
                                    ),
                                    availableTimes = listOf(
                                        "09:00",
                                        "10:00",
                                        "11:00",
                                        "14:00",
                                        "15:00",
                                        "16:00",
                                        "17:00"
                                    ),
                                    onNext = { d: BookingDraft ->
                                        nav.navigate("bookingSummary/${m.id}/${d.date}/${d.time}/${d.durationMin}")
                                    },
                                    onClose = { nav.popBackStack() }
                                )
                            }
                        }

                        // Step 2: xác nhận
                        composable("bookingSummary/{mentorId}/{date}/{time}/{duration}") { backStackEntry ->
                            val mentorId =
                                backStackEntry.arguments?.getString("mentorId")
                                    ?: return@composable
                            val date =
                                backStackEntry.arguments?.getString("date")
                                    ?: ""
                            val time =
                                backStackEntry.arguments?.getString("time")
                                    ?: ""
                            val duration =
                                backStackEntry.arguments?.getString("duration")
                                    ?.toIntOrNull() ?: 60
                            val mentor =
                                MockData.mockMentors.find { it.id == mentorId }
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
                                        nav.popBackStack(
                                            route = Routes.Home,
                                            inclusive = false
                                        )
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

