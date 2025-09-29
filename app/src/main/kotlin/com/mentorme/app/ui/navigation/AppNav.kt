package com.mentorme.app.ui.navigation

import android.util.Log
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.layout.GlassBottomBar

// Screens
import com.mentorme.app.ui.auth.AuthScreen
import com.mentorme.app.ui.home.HomeScreen
import com.mentorme.app.ui.dashboard.MentorDashboardScreen
import com.mentorme.app.ui.calendar.MentorCalendarScreen
import com.mentorme.app.ui.chat.MentorMessagesScreen
import com.mentorme.app.ui.profile.MentorProfileScreen
import com.mentorme.app.ui.search.SearchMentorScreen
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.calendar.CalendarScreen
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mentorme.app.ui.auth.AuthScreen
import com.mentorme.app.ui.auth.RegisterPayload
import com.mentorme.app.ui.chat.ChatScreen
import com.mentorme.app.ui.chat.MessagesScreen
import com.mentorme.app.ui.profile.*
import androidx.navigation.compose.composable
import com.mentorme.app.ui.wallet.TopUpScreen
import com.mentorme.app.ui.wallet.WithdrawScreen
import com.mentorme.app.ui.wallet.BankInfo
import com.mentorme.app.ui.wallet.PaymentMethod
import com.mentorme.app.ui.wallet.mockPaymentMethods
import com.mentorme.app.ui.wallet.initialPaymentMethods
import com.mentorme.app.ui.wallet.PaymentMethodScreen
import com.mentorme.app.ui.wallet.AddPaymentMethodScreen
import com.mentorme.app.ui.wallet.EditPaymentMethodScreen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.onboarding.MenteeOnboardingScreen
import com.mentorme.app.ui.onboarding.MentorOnboardingScreen
import com.mentorme.app.ui.onboarding.PendingApprovalScreen

object Routes {
    const val Auth = "auth"
    const val Home = "home"

    // Mentor routes
    const val MentorDashboard = "mentor_dashboard"
    const val MentorCalendar = "mentor_calendar"
    const val MentorMessages = "mentor_messages"
    const val MentorProfile = "mentor_profile"

    const val Calendar = "calendar"
    const val Messages = "messages"
    const val Profile = "profile"
    const val Chat = "chat"
    const val search = "search"
    const val TopUp = "wallet/topup"
    const val Withdraw = "wallet/withdraw"
    const val PaymentMethods = "wallet/payment_methods"
    const val AddPaymentMethod = "wallet/add_method"
    const val EditPaymentMethod = "wallet/edit_method"
    const val PendingApproval = "pending_approval"
    const val Onboarding = "onboarding/{role}"
    fun onboardingFor(role: String) = "onboarding/$role"
}

private fun goToSearch(nav: NavHostController) {
    if (nav.currentDestination?.route != Routes.search) {
        nav.navigate(Routes.search) {
            // giữ lại state của tab trước đó, tránh tạo bản sao Search
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

private fun backOrHome(nav: NavHostController, userRole: String = "mentee") {
    // Thử pop về màn trước; nếu không có gì để pop thì về Home tương ứng với user role
    val popped = nav.popBackStack()
    if (!popped) {
        val homeRoute = if (userRole == "mentor") Routes.MentorDashboard else Routes.Home  // Thay đổi từ MentorHome sang MentorDashboard
        nav.navigate(homeRoute) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
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
    var userRole by rememberSaveable { mutableStateOf("mentee") } // Track user role
    var payMethods by remember { mutableStateOf(initialPaymentMethods()) }
    var authToken by rememberSaveable { mutableStateOf<String?>(null) }

    // Debug current route
    LaunchedEffect(currentRoute) {
        Log.d("AppNav", "Current route changed to: $currentRoute")
    }

    val selMethodId = nav.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("payment_method_id", null)
        ?.collectAsState(initial = null)?.value

    LaunchedEffect(selMethodId) {
        selMethodId?.let { id ->
            payMethods = payMethods.map { it.copy(isDefault = it.id == id) }
            nav.currentBackStackEntry?.savedStateHandle?.set("payment_method_id", null)
        }
    }

    val newMethod = nav.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<PaymentMethod?>("new_payment_method", null)
        ?.collectAsState(initial = null)?.value

    LaunchedEffect(newMethod) {
        newMethod?.let { m ->
            // nếu là phương thức đầu tiên → set mặc định luôn
            val toAdd = if (payMethods.isEmpty()) m.copy(isDefault = true) else m
            payMethods = payMethods + toAdd
            nav.currentBackStackEntry?.savedStateHandle?.set("new_payment_method", null)
        }
    }

    val editedMethod = nav.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<PaymentMethod?>("edited_payment_method", null)
        ?.collectAsState(initial = null)?.value

    LaunchedEffect(editedMethod) {
        editedMethod?.let { m ->
            payMethods = payMethods.map { if (it.id == m.id) m.copy(isDefault = it.isDefault) else it }
            nav.currentBackStackEntry?.savedStateHandle?.set("edited_payment_method", null)
        }
    }

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
                val hideForChat = currentRoute?.startsWith("${Routes.Chat}/") == true
                val hideForBooking =
                    currentRoute?.startsWith("booking/") == true ||
                            currentRoute?.startsWith("bookingSummary/") == true
                val hideForWallet = currentRoute?.startsWith("wallet/") == true

                if (
                    isLoggedIn &&
                    currentRoute != Routes.Auth &&
                    !hideForChat &&
                    !hideForBooking &&
                    !hideForWallet
                ){
                    GlassBottomBar(navController = nav, userRole = userRole)
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = nav,
                    startDestination = when {
                        !isLoggedIn -> Routes.Auth
                        userRole == "mentor" -> Routes.MentorDashboard  // Thay đổi từ MentorHome sang MentorDashboard
                        else -> Routes.Home
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ---------- AUTH ----------
                    composable(Routes.Auth) {
                        AuthScreen(
                            onLogin = { email, pass ->
                                // TODO: gọi repo thực tế
                                val ok = email.isNotBlank() && pass.isNotBlank()
                                if (ok) isLoggedIn = true
                                ok
                            },
                            onRegister = { p: RegisterPayload ->
                                // TODO: gọi API tạo user
                                val ok =
                                    p.fullName.isNotBlank() && p.email.isNotBlank() && p.password.length >= 6
                                if (ok) isLoggedIn = true
                                ok
                            },
                            onResetPassword = { /* TODO */ },
                            onNavigateToMenteeHome = {
                                isLoggedIn = true
                                userRole = "mentee"  // Set role
                                nav.navigate(Routes.Home) {
                                    popUpTo(Routes.Auth) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToMentorHome = {
                                isLoggedIn = true
                                userRole = "mentor"  // Set role
                                Log.d("AppNav", "Navigating to MentorDashboard route")
                                nav.navigate(Routes.MentorDashboard) {  // Thay đổi từ MentorHome sang MentorDashboard
                                    popUpTo(Routes.Auth) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToOnboarding = { tokenFromAuth: String?, role: String? ->
                                authToken = tokenFromAuth
                                val roleSafe = role ?: "mentee"
                                userRole = roleSafe // optional, vẫn set state để bottom bar, startDestination... biết
                                Log.d("AppNav", "Navigating to Onboarding, token=$tokenFromAuth, role=$roleSafe")

                                nav.navigate(Routes.onboardingFor(roleSafe)) {
                                    popUpTo(Routes.Auth) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToReview = {
                                Log.d("AppNav", "Navigating to PendingApproval screen")
                                nav.navigate(Routes.PendingApproval) {
                                    popUpTo(Routes.Auth) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // ---------- ONBOARDING ----------
                    composable(Routes.Onboarding) { backStackEntry ->
                        val roleArg = backStackEntry.arguments?.getString("role") ?: userRole

                        if (roleArg == "mentor") {
                            MentorOnboardingScreen(
                                onBack = { nav.popBackStack() },
                                onDoneGoHome = {
                                    isLoggedIn = true
                                    userRole = "mentor"
                                    nav.navigate(Routes.MentorDashboard) {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                onGoToReview = {
                                    nav.navigate(Routes.PendingApproval) {
                                        popUpTo(Routes.Auth) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        } else {
                            MenteeOnboardingScreen(
                                onBack = { nav.popBackStack() },
                                onDoneGoHome = {
                                    isLoggedIn = true
                                    userRole = "mentee"
                                    nav.navigate(Routes.Home) {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                onGoToReview = {
                                    nav.navigate(Routes.PendingApproval) {
                                        popUpTo(Routes.Auth) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }

                    // ---------- PENDING APPROVAL ----------
                    composable(Routes.PendingApproval) {
                        PendingApprovalScreen(
                            onRefreshStatus = {
                                // TODO: Gọi API kiểm tra trạng thái → nếu đã duyệt thì điều hướng sang dashboard
                            },
                            onBackToLogin = {
                                nav.navigate(Routes.Auth) {
                                    popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }


                    // ---------- MAIN APP ----------
                    composable(Routes.Home) {
                        HomeScreen(
                            onNavigateToMentors = { goToSearch(nav) },
                            onSearch = { _ -> goToSearch(nav) }
                        )
                    }

                    // ---------- MENTOR SCREENS ----------
                    composable(Routes.MentorDashboard) {
                        MentorDashboardScreen(
                            onViewSchedule = { nav.navigate(Routes.MentorCalendar) },
                            onViewStudents = {
                                Log.d("AppNav", "Navigate to students list - TODO")
                            },
                            onViewEarnings = {
                                Log.d("AppNav", "Navigate to earnings - TODO")
                            },
                            onViewReviews = {
                                Log.d("AppNav", "Navigate to reviews - TODO")
                            },
                            onJoinSession = { sessionId ->
                                Log.d("AppNav", "Join session $sessionId - TODO")
                            },
                            onViewAllSessions = { nav.navigate(Routes.MentorCalendar) },
                            onUpdateProfile = { nav.navigate(Routes.MentorProfile) }
                        )
                    }

                    composable(Routes.MentorCalendar) {
                        MentorCalendarScreen(
                            onViewSession = { sessionId ->
                                Log.d("AppNav", "View session $sessionId - TODO")
                            },
                            onCreateSession = {
                                Log.d("AppNav", "Create session - TODO")
                            },
                            onUpdateAvailability = {
                                Log.d("AppNav", "Update availability - TODO")
                            },
                            onCancelSession = { sessionId ->
                                Log.d("AppNav", "Cancel session $sessionId - TODO")
                            }
                        )
                    }

                    composable(Routes.MentorMessages) {
                        MentorMessagesScreen(
                            onOpenConversation = { convId ->
                                nav.navigate("${Routes.Chat}/$convId")
                            },
                            onFilterStudents = {
                                Log.d("AppNav", "Filter students - TODO")
                            },
                            onSearchConversations = { query ->
                                Log.d("AppNav", "Search conversations: $query - TODO")
                            }
                        )
                    }

                    composable(Routes.MentorProfile) {
                        MentorProfileScreen(
                            onEditProfile = {
                                Log.d("AppNav", "Edit mentor profile - TODO")
                            },
                            onViewEarnings = {
                                Log.d("AppNav", "View earnings - TODO")
                            },
                            onViewReviews = {
                                Log.d("AppNav", "View reviews - TODO")
                            },
                            onUpdateAvailability = { nav.navigate(Routes.MentorCalendar) },
                            onManageServices = {
                                Log.d("AppNav", "Manage services - TODO")
                            },
                            onViewStatistics = {
                                Log.d("AppNav", "View statistics - TODO")
                            },
                            onSettings = {
                                Log.d("AppNav", "Settings - TODO")
                            },
                            onLogout = {
                                isLoggedIn = false
                                userRole = "mentee"
                                nav.navigate(Routes.Auth) {
                                    popUpTo(nav.graph.findStartDestination().id) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(Routes.search) {
                        SearchMentorScreen(
                            onOpenProfile = { backOrHome(nav, userRole) },
                            onBook = { backOrHome(nav, userRole) }
                        )
                    }

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

                    composable(Routes.Profile) {
                        ProfileScreen(
                            user = UserHeader(fullName = "Nguyễn Văn A", email = "a@example.com", role = UserRole.MENTEE),
                            onOpenTopUp = { nav.navigate(Routes.TopUp) },
                            onOpenWithdraw = { nav.navigate(Routes.Withdraw) },
                            onOpenChangeMethod = { nav.navigate(Routes.PaymentMethods) },
                            onAddMethod = { nav.navigate(Routes.AddPaymentMethod) },
                            methods = payMethods,
                            onLogout = {
                                isLoggedIn = false
                                nav.navigate(Routes.Auth) {
                                    popUpTo(nav.graph.findStartDestination().id) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // ---------- BOOKING ----------
                    composable("booking/{mentorId}") { backStackEntry ->
                        val mentorId = backStackEntry.arguments?.getString("mentorId") ?: return@composable
                        val mentor = MockData.mockMentors.find { it.id == mentorId }
                        mentor?.let { m ->
                            BookingChooseTimeScreen(
                                mentor = m,
                                availableDates = listOf("2025-09-20","2025-09-21","2025-09-22","2025-09-23","2025-09-24"),
                                availableTimes = listOf("09:00","10:00","11:00","14:00","15:00","16:00","17:00"),
                                onNext = { d: BookingDraft ->
                                    nav.navigate("bookingSummary/${m.id}/${d.date}/${d.time}/${d.durationMin}")
                                },
                                onClose = { nav.popBackStack() }
                            )
                        }
                    }

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
                                currentUserId = "current-user-id",
                                onConfirmed = {
                                    nav.popBackStack(route = Routes.Home, inclusive = false)
                                },
                                onBack = { nav.popBackStack() }
                            )
                        }
                    }

                    // ---------- WALLET ----------
                    composable(Routes.TopUp) {
                        TopUpScreen(
                            balance = 8_500_000L,
                            onBack = { nav.popBackStack() },
                            onSubmit = { amount, method ->
                                nav.popBackStack()
                            }
                        )
                    }

                    composable(Routes.Withdraw) {
                        WithdrawScreen(
                            balance = 8_500_000L,
                            bankInfo = BankInfo(
                                bankName = "Vietcombank",
                                accountNumber = "0123456789",
                                accountName = "NGUYEN VAN A"
                            ),
                            onBack = { nav.popBackStack() },
                            onSubmit = { amount, bank ->
                                nav.popBackStack()
                            }
                        )
                    }

                    composable(Routes.PaymentMethods) {
                        PaymentMethodScreen(
                            methods = payMethods,
                            onBack = { nav.popBackStack() },
                            onChosen = { chosen ->
                                nav.previousBackStackEntry?.savedStateHandle?.set("payment_method_id", chosen.id)
                                nav.popBackStack()
                            },
                            onAddNew = { nav.navigate(Routes.AddPaymentMethod) },
                            onEditSelected = { toEdit ->
                                nav.currentBackStackEntry?.savedStateHandle?.set("editing_method", toEdit)
                                nav.navigate(Routes.EditPaymentMethod)
                            }
                        )
                    }

                    composable(Routes.AddPaymentMethod) {
                        AddPaymentMethodScreen(
                            onBack = { nav.popBackStack() },
                            onSaved = { method ->
                                nav.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("new_payment_method", method)
                                nav.popBackStack()
                            }
                        )
                    }

                    composable(Routes.EditPaymentMethod) {
                        val toEdit = nav.previousBackStackEntry
                            ?.savedStateHandle?.get<PaymentMethod>("editing_method")
                        if (toEdit == null) {
                            LaunchedEffect(Unit) { nav.popBackStack() }
                        } else {
                            EditPaymentMethodScreen(
                                method = toEdit,
                                onBack = { nav.popBackStack() },
                                onSaved = { updated ->
                                    nav.previousBackStackEntry?.savedStateHandle?.set("edited_payment_method", updated)
                                    nav.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
