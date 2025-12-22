package com.mentorme.app.ui.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.auth.AuthScreen
import com.mentorme.app.ui.auth.RegisterPayload
import com.mentorme.app.ui.booking.BookingChooseTimeScreen
import com.mentorme.app.ui.booking.BookingDraft
import com.mentorme.app.ui.booking.BookingSummaryScreen
import com.mentorme.app.ui.calendar.CalendarScreen
import com.mentorme.app.ui.calendar.MentorCalendarScreen
import com.mentorme.app.ui.chat.ChatScreen
import com.mentorme.app.ui.chat.MessagesScreen
import com.mentorme.app.ui.chat.MentorMessagesScreen
import com.mentorme.app.ui.dashboard.MentorDashboardScreen
import com.mentorme.app.ui.home.HomeScreen
import com.mentorme.app.ui.layout.GlassBottomBar
import com.mentorme.app.ui.layout.UserUi
import com.mentorme.app.ui.onboarding.MenteeOnboardingScreen
import com.mentorme.app.ui.onboarding.MentorOnboardingScreen
import com.mentorme.app.ui.onboarding.PendingApprovalScreen
import com.mentorme.app.ui.profile.ProfileScreen
import com.mentorme.app.ui.profile.UserHeader
import com.mentorme.app.ui.profile.UserRole
import com.mentorme.app.ui.profile.MentorProfileScreen
import com.mentorme.app.ui.search.SearchMentorScreen
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.wallet.*
import java.util.Calendar
import java.util.Locale

object Routes {
    const val Auth = "auth"
    const val Home = "home"

    // Mentor routes
    const val MentorDashboard = "mentor_dashboard"
    const val MentorCalendar = "mentor_calendar"
    const val MentorMessages = "mentor_messages"

    // ✅ base route (vẫn giữ để GlassBottomBar / nơi khác dùng không vỡ)
    const val MentorProfile = "mentor_profile"

    // ✅ route có query param để mở đúng tab
    const val MentorProfileWithTarget = "mentor_profile?target={target}"
    fun mentorProfile(target: String? = null): String {
        return if (target.isNullOrBlank()) MentorProfile else "mentor_profile?target=$target"
    }

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
    var userRole by rememberSaveable { mutableStateOf("mentee") }
    var payMethods by remember { mutableStateOf(initialPaymentMethods()) }
    var authToken by rememberSaveable { mutableStateOf<String?>(null) }

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

    var overlayVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    !overlayVisible &&
                    isLoggedIn &&
                    currentRoute != Routes.Auth &&
                    !hideForChat &&
                    !hideForBooking &&
                    !hideForWallet
                ) {
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
                        userRole == "mentor" -> Routes.MentorDashboard
                        else -> Routes.Home
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ---------- AUTH ----------
                    composable(Routes.Auth) {
                        AuthScreen(
                            onLogin = { email, pass ->
                                val ok = email.isNotBlank() && pass.isNotBlank()
                                if (ok) isLoggedIn = true
                                ok
                            },
                            onRegister = { p: RegisterPayload ->
                                val ok = p.fullName.isNotBlank() && p.email.isNotBlank() && p.password.length >= 6
                                if (ok) isLoggedIn = true
                                ok
                            },
                            onResetPassword = { /* TODO */ },
                            onNavigateToMenteeHome = {
                                isLoggedIn = true
                                userRole = "mentee"
                                nav.navigate(Routes.Home) {
                                    popUpTo(Routes.Auth) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToMentorHome = {
                                isLoggedIn = true
                                userRole = "mentor"
                                nav.navigate(Routes.MentorDashboard) {
                                    popUpTo(Routes.Auth) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToOnboarding = { tokenFromAuth: String?, role: String? ->
                                authToken = tokenFromAuth
                                val roleSafe = role ?: "mentee"
                                userRole = roleSafe
                                nav.navigate(Routes.onboardingFor(roleSafe)) {
                                    popUpTo(Routes.Auth) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToReview = {
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
                            onRefreshStatus = { /* TODO */ },
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
                            onViewSchedule = {
                                nav.navigate(Routes.MentorCalendar) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onViewStudents = { Log.d("AppNav", "Navigate to students list - TODO") },
                            onViewEarnings = { Log.d("AppNav", "Navigate to earnings - TODO") },
                            onViewReviews = { Log.d("AppNav", "Navigate to reviews - TODO") },
                            onJoinSession = { sessionId -> Log.d("AppNav", "Join session $sessionId - TODO") },
                            onViewAllSessions = {
                                nav.navigate(Routes.MentorCalendar) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },

                            // ✅ “Hồ sơ”
                            onUpdateProfile = {
                                nav.navigate(Routes.mentorProfile("profile")) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },

                            // ✅ “Cài đặt” (đi thẳng tab settings)
                            onOpenSettings = {
                                nav.navigate(Routes.mentorProfile("settings")) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    composable(Routes.MentorCalendar) {
                        MentorCalendarScreen(
                            onViewSession = { sessionId -> Log.d("AppNav", "View session $sessionId - TODO") },
                            onCreateSession = { Log.d("AppNav", "Create session - TODO") },
                            onUpdateAvailability = { Log.d("AppNav", "Update availability - TODO") },
                            onCancelSession = { sessionId -> Log.d("AppNav", "Cancel session $sessionId - TODO") }
                        )
                    }

                    composable(Routes.MentorMessages) {
                        MentorMessagesScreen(
                            onOpenConversation = { convId -> nav.navigate("${Routes.Chat}/$convId") },
                            onFilterStudents = { Log.d("AppNav", "Filter students - TODO") },
                            onSearchConversations = { query -> Log.d("AppNav", "Search conversations: $query - TODO") }
                        )
                    }

                    // ✅ (A) composable base route: mentor_profile (default tab profile)
                    composable(Routes.MentorProfile) {
                        MentorProfileScreen(
                            startTarget = "profile",
                            onEditProfile = { Log.d("AppNav", "MentorProfile: onEditProfile - TODO") },
                            onViewEarnings = { Log.d("AppNav", "MentorProfile: onViewEarnings - TODO") },
                            onViewReviews = { Log.d("AppNav", "MentorProfile: onViewReviews - TODO") },
                            onUpdateAvailability = {
                                nav.navigate(Routes.MentorCalendar) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onManageServices = { Log.d("AppNav", "MentorProfile: onManageServices - TODO") },
                            onViewStatistics = { Log.d("AppNav", "MentorProfile: onViewStatistics - TODO") },
                            onSettings = { Log.d("AppNav", "MentorProfile: onSettings - TODO") },
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

                    // ✅ (B) composable query route: mentor_profile?target=...
                    composable(
                        route = Routes.MentorProfileWithTarget,
                        arguments = listOf(
                            navArgument("target") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = "profile"
                            }
                        )
                    ) { backStackEntry ->
                        val target = backStackEntry.arguments?.getString("target") ?: "profile"

                        MentorProfileScreen(
                            startTarget = target,
                            onEditProfile = { Log.d("AppNav", "MentorProfile: onEditProfile - TODO") },
                            onViewEarnings = { Log.d("AppNav", "MentorProfile: onViewEarnings - TODO") },
                            onViewReviews = { Log.d("AppNav", "MentorProfile: onViewReviews - TODO") },
                            onUpdateAvailability = {
                                nav.navigate(Routes.MentorCalendar) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onManageServices = { Log.d("AppNav", "MentorProfile: onManageServices - TODO") },
                            onViewStatistics = { Log.d("AppNav", "MentorProfile: onViewStatistics - TODO") },
                            onSettings = { Log.d("AppNav", "MentorProfile: onSettings - TODO") },
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
                        val ctx = LocalContext.current
                        SearchMentorScreen(
                            onOpenProfile = { /* handled by sheet inside SearchMentorScreen */ },
                            onBook = { id ->
                                val targetId = if (id.startsWith("m") && id.drop(1).all { it.isDigit() }) id.drop(1) else id
                                val exists = MockData.mockMentors.any { it.id == targetId }
                                if (exists) {
                                    nav.navigate("booking/$targetId")
                                } else {
                                    Toast.makeText(ctx, "Mentor này chưa có dữ liệu đặt lịch (mock)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onOverlayOpened = { overlayVisible = true },
                            onOverlayClosed = { overlayVisible = false }
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

                    composable(Routes.Messages) {
                        MessagesScreen(onOpenConversation = { convId ->
                            nav.navigate("${Routes.Chat}/$convId")
                        })
                    }

                    composable("${Routes.Chat}/{conversationId}") { backStackEntry ->
                        val convId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
                        ChatScreen(
                            conversationId = convId,
                            onBack = { nav.popBackStack() },
                            onJoinSession = { /* TODO */ }
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

                        val vm = hiltViewModel<com.mentorme.app.ui.booking.BookingFlowViewModel>()
                        LaunchedEffect(mentorId) { vm.load(mentorId) }
                        val uiState by vm.state.collectAsState()
                        val grouped = (uiState as? com.mentorme.app.ui.booking.BookingFlowViewModel.UiState.Success<
                                Map<String, List<com.mentorme.app.ui.booking.BookingFlowViewModel.TimeSlotUi>>
                                >)?.data ?: emptyMap()
                        val availableDates = remember(grouped) { grouped.keys.sorted() }
                        val availableTimes = remember(grouped, availableDates) {
                            val first = availableDates.firstOrNull()
                            if (first != null) grouped[first]?.map { it.startLabel } ?: emptyList() else emptyList()
                        }

                        mentor?.let { m ->
                            BookingChooseTimeScreen(
                                mentor = m,
                                availableDates = availableDates,
                                availableTimes = availableTimes,
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

                        val vm = hiltViewModel<com.mentorme.app.ui.booking.BookingFlowViewModel>()
                        val ctx = LocalContext.current

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
                                    val endTime = run {
                                        try {
                                            val parts = time.split(":")
                                            val cal = Calendar.getInstance()
                                            cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                                            cal.set(Calendar.MINUTE, parts[1].toInt())
                                            cal.add(Calendar.MINUTE, duration)
                                            String.format(Locale.US, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                                        } catch (_: Exception) {
                                            "00:00"
                                        }
                                    }

                                    when (val res = vm.createBooking(
                                        mentorId = mentorId,
                                        date = date,
                                        startTime = time,
                                        endTime = endTime,
                                        topic = "Mentor Session",
                                        notes = ""
                                    )) {
                                        is com.mentorme.app.core.utils.AppResult.Success -> {
                                            nav.popBackStack(route = Routes.Home, inclusive = false)
                                        }
                                        is com.mentorme.app.core.utils.AppResult.Error -> {
                                            val msg = res.throwable
                                            val code = msg.substringAfter("HTTP ", "").substringBefore(":").toIntOrNull()
                                            when (code) {
                                                401 -> {
                                                    nav.navigate(Routes.Auth) {
                                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = false }
                                                        launchSingleTop = true
                                                    }
                                                }
                                                409, 422 -> {
                                                    Toast.makeText(ctx, "Khung giờ đã được đặt. Vui lòng chọn thời gian khác.", Toast.LENGTH_LONG).show()
                                                }
                                                else -> {
                                                    Toast.makeText(ctx, msg.ifBlank { "Có lỗi xảy ra, vui lòng thử lại." }, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                        com.mentorme.app.core.utils.AppResult.Loading -> Unit
                                    }
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
                            onSubmit = { _, _ -> nav.popBackStack() }
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
                            onSubmit = { _, _ -> nav.popBackStack() }
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
                                nav.previousBackStackEntry?.savedStateHandle?.set("new_payment_method", method)
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
