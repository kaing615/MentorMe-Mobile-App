package com.mentorme.app.ui.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mentorme.app.core.appstate.AppForegroundTracker
import com.mentorme.app.core.notifications.NotificationDeduper
import com.mentorme.app.core.notifications.NotificationPreferencesStore
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.ui.calendar.MenteeBookingsViewModel
import com.mentorme.app.ui.calendar.MentorBookingsViewModel
import com.mentorme.app.ui.layout.GlassBottomBar
import com.mentorme.app.ui.notifications.NotificationsViewModel
import com.mentorme.app.ui.session.SessionViewModel
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.wallet.initialPaymentMethods

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNav(
    nav: NavHostController = rememberNavController(),
    initialRoute: String? = null,
    onRouteConsumed: () -> Unit = {}
) {
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
                            actionLabel = "Open",
                            duration = SnackbarDuration.Long
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
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { GlassSnackbarHost(snackbarHostState) },
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
                AppNavGraph(
                    nav = nav,
                    sessionVm = sessionVm,
                    sessionState = sessionState,
                    authVm = authVm,
                    notificationsVm = notificationsVm,
                    payMethods = payMethods,
                    pendingRoleHint = pendingRoleHint,
                    onPendingRoleHintChange = { pendingRoleHint = it },
                    overlayVisible = overlayVisible,
                    onOverlayVisibleChange = { overlayVisible = it }
                )
            }
        }
    }
}
