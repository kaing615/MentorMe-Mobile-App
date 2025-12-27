package com.mentorme.app.ui.navigation

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mentorme.app.core.appstate.AppForegroundTracker
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
import com.mentorme.app.core.notifications.NotificationDeduper
import com.mentorme.app.core.notifications.NotificationPreferencesStore
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.auth.AuthScreen
import com.mentorme.app.ui.auth.RegisterPayload
import com.mentorme.app.ui.booking.BookingChooseTimeScreen
import com.mentorme.app.ui.booking.BookingDetailScreen
import com.mentorme.app.ui.booking.BookingDraft
import com.mentorme.app.ui.booking.BookingSummaryScreen
import com.mentorme.app.ui.calendar.CalendarTab
import com.mentorme.app.ui.calendar.MenteeBookingsViewModel
import com.mentorme.app.ui.calendar.MenteeCalendarScreen
import com.mentorme.app.ui.calendar.MentorBookingsViewModel
import com.mentorme.app.ui.calendar.MentorCalendarScreen
import com.mentorme.app.ui.chat.ChatScreen
import com.mentorme.app.ui.chat.MentorMessagesScreen
import com.mentorme.app.ui.chat.MessagesScreen
import com.mentorme.app.ui.dashboard.MentorDashboardScreen
import com.mentorme.app.ui.home.HomeScreen
import com.mentorme.app.ui.layout.GlassBottomBar
import com.mentorme.app.ui.onboarding.MenteeOnboardingScreen
import com.mentorme.app.ui.onboarding.MentorOnboardingScreen
import com.mentorme.app.ui.onboarding.PendingApprovalScreen
import com.mentorme.app.ui.notifications.NotificationDetailScreen
import com.mentorme.app.ui.notifications.NotificationsScreen
import com.mentorme.app.ui.notifications.NotificationsViewModel
import com.mentorme.app.ui.profile.MentorProfileScreen
import com.mentorme.app.ui.profile.ProfileScreen
import com.mentorme.app.ui.profile.UserHeader
import com.mentorme.app.ui.profile.UserRole
import com.mentorme.app.ui.search.SearchMentorScreen
import com.mentorme.app.ui.session.SessionViewModel
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.wallet.AddPaymentMethodScreen
import com.mentorme.app.ui.wallet.BankInfo
import com.mentorme.app.ui.wallet.EditPaymentMethodScreen
import com.mentorme.app.ui.wallet.PaymentMethod
import com.mentorme.app.ui.wallet.PaymentMethodScreen
import com.mentorme.app.ui.wallet.TopUpScreen
import com.mentorme.app.ui.wallet.WithdrawScreen
import com.mentorme.app.ui.wallet.initialPaymentMethods

object Routes {
    const val Auth = "auth"
    const val Home = "home"
    const val MentorDashboard = "mentor_dashboard"
    const val MentorCalendar = "mentor_calendar"
    const val MentorMessages = "mentor_messages"
    const val MentorProfile = "mentor_profile"
    const val MentorProfileWithTarget = "mentor_profile?target={target}"
    fun mentorProfile(target: String? = null): String {
        return if (target.isNullOrBlank()) MentorProfile else "mentor_profile?target=$target"
    }

    const val Calendar = "calendar"
    const val CalendarWithTab = "calendar?tab={tab}"
    fun calendar(tab: String? = null): String {
        return if (tab.isNullOrBlank()) Calendar else "calendar?tab=$tab"
    }

    const val Notifications = "notifications"
    const val NotificationDetail = "notifications/detail/{notificationId}"
    fun notificationDetail(notificationId: String) = "notifications/detail/$notificationId"
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

// Simple state machine for navigation phase
private sealed class AppPhase {
    object Loading : AppPhase()
    object Auth : AppPhase()
    object Onboarding : AppPhase()
    object Pending : AppPhase()
    object Home : AppPhase()
}

private fun resolvePhase(isLoggedIn: Boolean, status: String?): AppPhase {
    if (!isLoggedIn) return AppPhase.Auth
    if (status == null) return AppPhase.Loading

    return when (status.trim().lowercase().replace('_', '-')) {
        "onboarding" -> AppPhase.Onboarding
        "pending-mentor" -> AppPhase.Pending
        "active" -> AppPhase.Home
        else -> AppPhase.Home
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNav(
    nav: NavHostController = rememberNavController(),
    initialRoute: String? = null,
    onRouteConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionVm = hiltViewModel<SessionViewModel>()
    val sessionState by sessionVm.session.collectAsStateWithLifecycle()
    val authVm = hiltViewModel<com.mentorme.app.ui.auth.AuthViewModel>()
    val notificationsVm: NotificationsViewModel = hiltViewModel()
    val unreadNotificationCount by notificationsVm.unreadCount.collectAsStateWithLifecycle()
    val isForeground by AppForegroundTracker.isForeground.collectAsStateWithLifecycle()
    val menteeBookingsVm: MenteeBookingsViewModel = hiltViewModel()
    val mentorBookingsVm: MentorBookingsViewModel = hiltViewModel()
    val menteeBookings by menteeBookingsVm.bookings.collectAsStateWithLifecycle()
    val mentorBookings by mentorBookingsVm.bookings.collectAsStateWithLifecycle()
    val isMentorRole = sessionState.role.equals("mentor", ignoreCase = true)
    val activeBookings = if (isMentorRole) mentorBookings else menteeBookings
    val pendingBookingCount = remember(activeBookings) {
        activeBookings.count {
            it.status == BookingStatus.PENDING_MENTOR || it.status == BookingStatus.PAYMENT_PENDING
        }
    }
    val messageUnreadCount = 0

    // Local UI state
    var payMethods by remember { mutableStateOf(initialPaymentMethods()) }
    var overlayVisible by remember { mutableStateOf(false) }
    var pendingRoleHint by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingRoute by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Compute phase (single source of navigation decisions)
    val phase by remember(sessionState.isLoggedIn, sessionState.status, pendingRoleHint) {
        derivedStateOf {
            // Prefer server role/status but fall back to pending hint set by AuthScreen flows
            resolvePhase(sessionState.isLoggedIn, sessionState.status)
        }
    }

    val isLoggedInState by rememberUpdatedState(sessionState.isLoggedIn)
    val isForegroundState by rememberUpdatedState(isForeground)

    LaunchedEffect(initialRoute) {
        if (!initialRoute.isNullOrBlank()) {
            pendingRoute = initialRoute
        }
    }

    LaunchedEffect(sessionState.isLoggedIn) {
        if (sessionState.isLoggedIn) {
            notificationsVm.restoreCache()
            notificationsVm.refreshUnreadCount()
            notificationsVm.refreshPreferences()
        } else {
            notificationsVm.clearNotifications()
            NotificationPreferencesStore.reset()
        }
    }

    LaunchedEffect(sessionState.isLoggedIn, sessionState.role) {
        if (sessionState.isLoggedIn) {
            if (sessionState.role.equals("mentor", ignoreCase = true)) {
                mentorBookingsVm.refresh()
            } else {
                menteeBookingsVm.refresh()
            }
        }
    }

    LaunchedEffect(isForeground) {
        if (isForeground && sessionState.isLoggedIn) {
            notificationsVm.refreshUnreadCount()
            notificationsVm.refreshPreferences()
        }
    }

    LaunchedEffect(Unit) {
        RealtimeEventBus.events.collect { event ->
            if (event is RealtimeEvent.NotificationReceived && isLoggedInState) {
                notificationsVm.refreshUnreadCount()
                if (isForegroundState) {
                    if (NotificationDeduper.shouldNotify(
                            event.notification.id,
                            event.notification.title,
                            event.notification.body,
                            event.notification.type
                        )
                    ) {
                        val title = event.notification.title.ifBlank { "New notification" }
                        val action = snackbarHostState.showSnackbar(
                            message = title,
                            actionLabel = "Open"
                        )
                        if (action == SnackbarResult.ActionPerformed) {
                            val route = event.notification.deepLink ?: Routes.Notifications
                            if (nav.currentDestination?.route != route) {
                                nav.navigate(route) { launchSingleTop = true }
                            }
                        }
                    }
                }
            }
        }
    }

    // Single navigation effect: only react to phase changes
    LaunchedEffect(phase) {
        Log.d("AppNav", "Phase changed -> $phase")
        when (phase) {
            AppPhase.Loading -> {
                // do nothing — wait for session to resolve
            }
            AppPhase.Auth -> {
                if (nav.currentDestination?.route != Routes.Auth) {
                    nav.navigate(Routes.Auth) {
                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            AppPhase.Onboarding -> {
                val role = sessionState.role ?: pendingRoleHint ?: "mentee"
                val onboardingRoute = Routes.onboardingFor(role)
                if (nav.currentDestination?.route != onboardingRoute) {
                    nav.navigate(onboardingRoute) {
                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            AppPhase.Pending -> {
                if (nav.currentDestination?.route != Routes.PendingApproval) {
                    nav.navigate(Routes.PendingApproval) {
                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            AppPhase.Home -> {
                val target = if (sessionState.role.equals("mentor", true)) Routes.MentorDashboard else Routes.Home
                if (nav.currentDestination?.route != target) {
                    nav.navigate(target) {
                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    // Backstack info for bottom bar visibility
    val backstack by nav.currentBackStackEntryAsState()
    val currentRoute = backstack?.destination?.route
    val currentBaseRoute = currentRoute?.substringBefore("?")

    LaunchedEffect(phase, pendingRoute, currentBaseRoute) {
        val targetRoute = pendingRoute ?: return@LaunchedEffect
        if (phase == AppPhase.Home) {
            if (currentBaseRoute != targetRoute) {
                nav.navigate(targetRoute) { launchSingleTop = true }
            }
            pendingRoute = null
            onRouteConsumed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LiquidBackground(modifier = Modifier.matchParentSize().zIndex(-1f))

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                val hideForChat = currentRoute?.startsWith("${Routes.Chat}/") == true
                val hideForBooking = currentRoute?.startsWith("booking/") == true || currentRoute?.startsWith("bookingSummary/") == true
                val hideForWallet = currentRoute?.startsWith("wallet/") == true
                val hideForOnboarding = currentRoute?.startsWith("onboarding/") == true || currentRoute == Routes.Onboarding
                val hideForPendingApproval = currentRoute == Routes.PendingApproval

                if (!overlayVisible && sessionState.isLoggedIn && currentRoute != Routes.Auth &&
                    !hideForChat && !hideForBooking && !hideForWallet && !hideForOnboarding && !hideForPendingApproval
                ) {
                    GlassBottomBar(
                        navController = nav,
                        userRole = sessionState.role ?: "mentee",
                        notificationUnreadCount = unreadNotificationCount,
                        calendarPendingCount = pendingBookingCount,
                        messageUnreadCount = messageUnreadCount
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Prevent rendering navigation host until we have at least a determinable start (optional)
                // We still keep NavHost startDestination as Auth — phase effect will redirect as soon as possible.

                NavHost(
                    navController = nav,
                    startDestination = Routes.Auth,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ---------- AUTH ----------
                    composable(Routes.Auth) {
                        AuthScreen(
                            onLogin = { email, pass ->
                                val ok = email.isNotBlank() && pass.isNotBlank()
                                if (ok) {
                                    // Don't navigate from here. Let SessionViewModel / backend change session state.
                                    // Trigger a refresh to pick up new session state once backend returns token/status.
                                    sessionVm.refreshStatus()
                                }
                                ok
                            },
                            onRegister = { p: RegisterPayload ->
                                val ok = p.fullName.isNotBlank() && p.email.isNotBlank() && p.password.length >= 6
                                if (ok) {
                                    sessionVm.refreshStatus()
                                }
                                ok
                            },
                            onResetPassword = { /* TODO */ },
                            onNavigateToMenteeHome = {
                                // user explicitly chose mentee flow during auth -> hint role locally
                                pendingRoleHint = "mentee"
                                sessionVm.refreshStatus()
                            },
                            onNavigateToMentorHome = {
                                pendingRoleHint = "mentor"
                                sessionVm.refreshStatus()
                            },
                            onNavigateToOnboarding = { tokenFromAuth: String?, role: String? ->
                                // Auth flow decided onboarding is next. Set a local hint so we can navigate immediately
                                pendingRoleHint = role ?: pendingRoleHint
                                // backend should set status to onboarding; refresh to pick it up
                                sessionVm.refreshStatus()
                            },
                            onNavigateToReview = {
                                // user wants to go to pending review flow
                                sessionVm.refreshStatus()
                            },
                            onLogout = {
                                authVm.signOut {
                                    nav.navigate(Routes.Auth) {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }

                    // ---------- ONBOARDING ----------
                    composable(Routes.Onboarding) { backStackEntry ->
                        val roleArg = backStackEntry.arguments?.getString("role") ?: sessionState.role ?: pendingRoleHint ?: "mentee"

                        if (roleArg == "mentor") {
                            MentorOnboardingScreen(
                                onBack = { nav.popBackStack() },
                                onDoneGoHome = {
                                    // Do NOT navigate here. Update server / refresh session and let AppNav route.
                                    sessionVm.refreshStatus()
                                },
                                onGoToReview = {
                                    sessionVm.refreshStatus()
                                }
                            )
                        } else {
                            MenteeOnboardingScreen(
                                onBack = { nav.popBackStack() },
                                onDoneGoHome = {
                                    sessionVm.refreshStatus()
                                },
                                onGoToReview = {
                                    sessionVm.refreshStatus()
                                }
                            )
                        }
                    }

                    // ---------- PENDING APPROVAL ----------
                    composable(Routes.PendingApproval) {
                        PendingApprovalScreen(
                            onRefreshStatus = {
                                sessionVm.refreshStatus() { ok, status ->
                                    if (!ok) {
                                        Toast.makeText(context, "Không thể làm mới trạng thái.", Toast.LENGTH_SHORT).show()
                                    } else if (status == "pending-mentor") {
                                        Toast.makeText(context, "Hồ sơ vẫn đang chờ duyệt.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onBackToLogin = {
                                authVm.signOut {
                                    nav.navigate(Routes.Auth) {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }

                    // ---------- MAIN APP ----------
                    composable(Routes.Home) {
                        HomeScreen(
                            onNavigateToMentors = { goToSearch(nav) },
                            onSearch = { _ -> goToSearch(nav) },
                            onBookSlot = { mentor, occurrenceId, date, startTime, endTime, priceVnd, note ->
                                nav.currentBackStackEntry?.savedStateHandle?.set("booking_notes", note)
                                nav.currentBackStackEntry?.savedStateHandle?.set("booking_mentor_name", mentor.name)
                                nav.navigate("bookingSummary/${mentor.id}/$date/$startTime/$endTime/$priceVnd/$occurrenceId")
                            },
                            onOverlayOpened = { overlayVisible = true },
                            onOverlayClosed = { overlayVisible = false }
                        )
                    }

                    // ---------- MENTOR SCREENS ----------
                    composable(Routes.MentorDashboard) {
                        val profileVm: com.mentorme.app.ui.profile.ProfileViewModel = hiltViewModel()
                        MentorDashboardScreen(
                            vm = profileVm,
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
                            onUpdateProfile = {
                                nav.navigate(Routes.mentorProfile("profile")) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
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

                    composable(Routes.MentorProfile) {
                        val localAuthVm = hiltViewModel<com.mentorme.app.ui.auth.AuthViewModel>()
                        MentorProfileScreen(
                            notificationsViewModel = notificationsVm,
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
                            onOpenNotifications = { nav.navigate(Routes.Notifications) },
                            onLogout = {
                                localAuthVm.signOut {
                                    nav.navigate(Routes.Auth) {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }

                    composable(
                        route = Routes.MentorProfileWithTarget,
                        arguments = listOf()
                    ) { backStackEntry ->
                        val localAuthVm = hiltViewModel<com.mentorme.app.ui.auth.AuthViewModel>()
                        val target = backStackEntry.arguments?.getString("target") ?: "profile"

                        MentorProfileScreen(
                            notificationsViewModel = notificationsVm,
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
                            onOpenNotifications = { nav.navigate(Routes.Notifications) },
                            onLogout = {
                                localAuthVm.signOut {
                                    nav.navigate(Routes.Auth) {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }

                    composable(Routes.search) {
                        SearchMentorScreen(
                            onOpenProfile = { /* handled by sheet inside SearchMentorScreen */ },
                            onBook = { id ->
                                val targetId = if (id.startsWith("m") && id.drop(1).all { it.isDigit() }) id.drop(1) else id
                                nav.navigate("booking/$targetId")
                            },
                            onBookSlot = { mentor, occurrenceId, date, startTime, endTime, priceVnd, note ->
                                nav.currentBackStackEntry?.savedStateHandle?.set("booking_notes", note)
                                nav.currentBackStackEntry?.savedStateHandle?.set("booking_mentor_name", mentor.name)
                                nav.navigate("bookingSummary/${mentor.id}/$date/$startTime/$endTime/$priceVnd/$occurrenceId")
                            },
                            onOverlayOpened = { overlayVisible = true },
                            onOverlayClosed = { overlayVisible = false }
                        )
                    }

                    composable(
                        route = Routes.CalendarWithTab,
                        arguments = listOf()
                    ) { backStackEntry ->
                        val tabArg = backStackEntry.arguments?.getString("tab")
                        MenteeCalendarScreen(
                            startTab = CalendarTab.fromRouteArg(tabArg),
                            onOpenDetail = { booking ->
                                nav.navigate("booking_detail/${booking.id}")
                            }
                        )
                    }

                    composable("booking_detail/{bookingId}") { backStackEntry ->
                        val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
                        BookingDetailScreen(
                            bookingId = bookingId,
                            onBack = { nav.popBackStack() }
                        )
                    }

                    composable(Routes.Messages) {
                        MessagesScreen(onOpenConversation = { convId ->
                            nav.navigate("${Routes.Chat}/$convId")
                        })
                    }

                    composable(Routes.Notifications) {
                        NotificationsScreen(
                            onBack = { nav.popBackStack() },
                            onOpenDetail = { id -> nav.navigate(Routes.notificationDetail(id)) },
                            viewModel = notificationsVm
                        )
                    }

                    composable(Routes.NotificationDetail) { backStackEntry ->
                        val notificationId = backStackEntry.arguments?.getString("notificationId")
                            ?: return@composable
                        NotificationDetailScreen(
                            notificationId = notificationId,
                            onBack = { nav.popBackStack() }
                        )
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
                        val localAuthVm = hiltViewModel<com.mentorme.app.ui.auth.AuthViewModel>()
                        val profileVm = hiltViewModel<com.mentorme.app.ui.profile.ProfileViewModel>()
                        ProfileScreen(
                            vm = profileVm,
                            user = UserHeader(fullName = "Nguyễn Văn A", email = "a@example.com", role = UserRole.MENTEE),
                            notificationsViewModel = notificationsVm,
                            onOpenNotifications = { nav.navigate(Routes.Notifications) },
                            onOpenTopUp = { nav.navigate(Routes.TopUp) },
                            onOpenWithdraw = { nav.navigate(Routes.Withdraw) },
                            onOpenChangeMethod = { nav.navigate(Routes.PaymentMethods) },
                            onAddMethod = { nav.navigate(Routes.AddPaymentMethod) },
                            methods = payMethods,
                            onLogout = {
                                localAuthVm.signOut {
                                    nav.navigate(Routes.Auth) {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }

                    // ---------- BOOKING ----------
                    composable("booking/{mentorId}") { backStackEntry ->
                        val mentorId = backStackEntry.arguments?.getString("mentorId") ?: return@composable
                        val mentor = MockData.mockMentors.find { it.id == mentorId } ?: com.mentorme.app.data.model.Mentor(
                            id = mentorId,
                            email = "",
                            fullName = "Mentor",
                            avatar = null,
                            role = com.mentorme.app.data.model.UserRole.MENTOR,
                            createdAt = "",
                            bio = "",
                            skills = emptyList(),
                            hourlyRate = 0.0,
                            rating = 0.0,
                            totalReviews = 0,
                            availability = emptyList(),
                            verified = false,
                            experience = "",
                            education = "",
                            languages = emptyList()
                        )

                        val vm = hiltViewModel<com.mentorme.app.ui.booking.BookingFlowViewModel>()
                        LaunchedEffect(mentorId) { vm.load(mentorId) }
                        val uiState by vm.state.collectAsState()
                        val slots = (uiState as? com.mentorme.app.ui.booking.BookingFlowViewModel.UiState.Success<
                                List<com.mentorme.app.ui.booking.BookingFlowViewModel.TimeSlotUi>
                                >)?.data ?: emptyList()
                        val loading = uiState is com.mentorme.app.ui.booking.BookingFlowViewModel.UiState.Loading
                        val error = (uiState as? com.mentorme.app.ui.booking.BookingFlowViewModel.UiState.Error)?.message

                        BookingChooseTimeScreen(
                            mentor = mentor,
                            slots = slots,
                            loading = loading,
                            errorMessage = error,
                            onNext = { d: BookingDraft ->
                                nav.currentBackStackEntry?.savedStateHandle?.set("booking_notes", d.notes)
                                nav.navigate("bookingSummary/${mentor.id}/${d.date}/${d.startTime}/${d.endTime}/${d.priceVnd}/${d.occurrenceId}")
                            },
                            onClose = { nav.popBackStack() }
                        )
                    }

                    composable("bookingSummary/{mentorId}/{date}/{start}/{end}/{price}/{occurrenceId}") { backStackEntry ->
                        val mentorId = backStackEntry.arguments?.getString("mentorId") ?: return@composable
                        val date = backStackEntry.arguments?.getString("date") ?: ""
                        val startTime = backStackEntry.arguments?.getString("start") ?: ""
                        val endTime = backStackEntry.arguments?.getString("end") ?: ""
                        val priceVnd = backStackEntry.arguments?.getString("price")?.toLongOrNull() ?: 0L
                        val occurrenceIdRaw = backStackEntry.arguments?.getString("occurrenceId")
                        val occurrenceId = if (occurrenceIdRaw.isNullOrBlank()) {
                            "${date}_${startTime}"
                        } else {
                            occurrenceIdRaw
                        }
                        val notes = nav.previousBackStackEntry?.savedStateHandle?.get<String>("booking_notes") ?: ""
                        val mentorName = nav.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<String>("booking_mentor_name")
                            ?.trim()

                        val vm = hiltViewModel<com.mentorme.app.ui.booking.BookingFlowViewModel>()
                        val mentorFromMock = MockData.mockMentors.find { it.id == mentorId }
                        val mentor = if (mentorFromMock != null) {
                            if (!mentorName.isNullOrBlank() && mentorName != mentorFromMock.fullName) {
                                mentorFromMock.copy(fullName = mentorName)
                            } else {
                                mentorFromMock
                            }
                        } else {
                            com.mentorme.app.data.model.Mentor(
                                id = mentorId,
                                email = "",
                                fullName = mentorName ?: "Mentor",
                                avatar = null,
                                role = com.mentorme.app.data.model.UserRole.MENTOR,
                                createdAt = "",
                                bio = "",
                                skills = emptyList(),
                                hourlyRate = 0.0,
                                rating = 0.0,
                                totalReviews = 0,
                                availability = emptyList(),
                                verified = false,
                                experience = "",
                                education = "",
                                languages = emptyList()
                            )
                        }

                        BookingSummaryScreen(
                            mentor = mentor,
                            draft = BookingDraft(
                                mentorId = mentorId,
                                occurrenceId = occurrenceId,
                                date = date,
                                startTime = startTime,
                                endTime = endTime,
                                priceVnd = priceVnd,
                                notes = notes
                            ),
                            currentUserId = "current-user-id",
                            onConfirmed = {
                                when (val res = vm.createBooking(
                                    mentorId = mentorId,
                                    occurrenceId = occurrenceId,
                                    topic = "Mentor Session",
                                    notes = it.notes
                                )) {
                                    is com.mentorme.app.core.utils.AppResult.Success -> {
                                        nav.navigate(Routes.calendar("pending")) {
                                            popUpTo(nav.graph.findStartDestination().id) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                    is com.mentorme.app.core.utils.AppResult.Error -> {
                                        val msg = res.throwable
                                        val code = msg.substringAfter("HTTP ", "").substringBefore(":").toIntOrNull()
                                        when (code) {
                                            401 -> {
                                                authVm.signOut {
                                                    nav.navigate(Routes.Auth) {
                                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                            409, 422 -> {
                                                Toast.makeText(context, "Khung giờ đã được đặt. Vui lòng chọn thời gian khác.", Toast.LENGTH_LONG).show()
                                            }
                                            else -> {
                                                val errorMsg = if (msg.isBlank()) "Có lỗi xảy ra, vui lòng thử lại." else msg
                                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                    com.mentorme.app.core.utils.AppResult.Loading -> Unit
                                }
                            },
                            onBack = { nav.popBackStack() }
                        )
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

// small helper kept from previous file
private fun goToSearch(nav: NavHostController) {
    if (nav.currentDestination?.route != Routes.search) {
        nav.navigate(Routes.search) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}
