package com.mentorme.app.ui.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
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
import com.mentorme.app.data.model.NotificationType
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.ui.booking.PendingBookingPromptDialog
import com.mentorme.app.ui.calendar.MenteeBookingsViewModel
import com.mentorme.app.ui.calendar.MentorBookingsViewModel
import com.mentorme.app.ui.layout.GlassBottomBar
import com.mentorme.app.ui.notifications.NotificationsViewModel
import com.mentorme.app.ui.session.SessionViewModel
import com.mentorme.app.ui.theme.LocalHazeEnabled
import com.mentorme.app.ui.theme.LocalHazeState
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.wallet.initialPaymentMethods
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

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
    val pendingConfirmations by mentorBookingsVm.pendingConfirmations.collectAsStateWithLifecycle()
    val isMentorRole = sessionState.role.equals("mentor", ignoreCase = true)
    val activeBookings = if (isMentorRole) mentorBookings else menteeBookings
    val pendingBookingCount = remember(activeBookings) {
        activeBookings.count {
            it.status == BookingStatus.PENDING_MENTOR || it.status == BookingStatus.PAYMENT_PENDING
        }
    }
    val messageUnreadCount = 0
    val blurEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val hazeContentState = remember { HazeState() }
    val hazeBackgroundState = remember { HazeState() }

    var overlayVisible by remember { mutableStateOf(false) }
    var pendingActionBusy by remember { mutableStateOf(false) }
    var pendingRoleHint by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingRoute by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarType by remember { mutableStateOf<NotificationType?>(null) }

    // Compute phase (single source of navigation decisions)
    val phase by remember(sessionState.isLoggedIn, sessionState.status, pendingRoleHint) {
        derivedStateOf {
            // Prefer server role/status but fall back to pending hint set by AuthScreen flows
            resolvePhase(sessionState.isLoggedIn, sessionState.status)
        }
    }

    val isLoggedInState by rememberUpdatedState(sessionState.isLoggedIn)
    val isForegroundState by rememberUpdatedState(isForeground)
    val pendingPrompt = pendingConfirmations.firstOrNull()

    LaunchedEffect(pendingPrompt?.id) {
        pendingActionBusy = false
    }

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
        try {
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
                            snackbarType = event.notification.type
                            val action = try {
                                snackbarHostState.showSnackbar(
                                    message = title,
                                    actionLabel = "Open",
                                    duration = SnackbarDuration.Long
                                )
                            } finally {
                                snackbarType = null
                            }
                            if (action == SnackbarResult.ActionPerformed) {
                                val route = event.notification.deepLink ?: Routes.Notifications
                                if (nav.currentDestination?.route != route) {
                                    try {
                                        nav.navigate(route) { launchSingleTop = true }
                                    } catch (e: Exception) {
                                        Log.e("AppNav", "Navigation error from notification", e)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AppNav", "Error collecting realtime events", e)
        }
    }

    // Clean up when AppNav is disposed (app exit)
    DisposableEffect(Unit) {
        onDispose {
            Log.d("AppNav", "AppNav disposed - cleaning up")
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
    val isVideoCallRoute = currentRoute?.startsWith("${Routes.VideoCall}/") == true

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

    CompositionLocalProvider(
        LocalHazeState provides hazeBackgroundState,
        LocalHazeEnabled provides blurEnabled
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0),
                snackbarHost = { GlassSnackbarHost(snackbarHostState, activeType = snackbarType) },
                bottomBar = {
                    val hideForChat = currentRoute?.startsWith("${Routes.Chat}/") == true
                    val hideForVideoCall = currentRoute?.startsWith("${Routes.VideoCall}/") == true
                    val hideForBooking = currentRoute?.startsWith("booking/") == true || currentRoute?.startsWith("bookingSummary/") == true
                    val hideForWallet = currentRoute?.startsWith("wallet/") == true
                    val hideForOnboarding = currentRoute?.startsWith("onboarding/") == true || currentRoute == Routes.Onboarding
                    val hideForPendingApproval = currentRoute == Routes.PendingApproval

                    if (!overlayVisible && sessionState.isLoggedIn && currentRoute != Routes.Auth &&
                        !hideForChat && !hideForVideoCall && !hideForBooking && !hideForWallet &&
                        !hideForOnboarding && !hideForPendingApproval
                    ) {
                        GlassBottomBar(
                            navController = nav,
                            userRole = sessionState.role ?: "mentee",
                            notificationUnreadCount = unreadNotificationCount,
                            calendarPendingCount = pendingBookingCount,
                            messageUnreadCount = messageUnreadCount,
                        hazeState = hazeContentState,
                        blurEnabled = blurEnabled
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val backgroundHazeModifier = if (blurEnabled) {
                    Modifier.hazeSource(state = hazeBackgroundState)
                } else {
                    Modifier
                }
                LiquidBackground(modifier = Modifier.matchParentSize().then(backgroundHazeModifier))
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        nav = nav,
                        sessionVm = sessionVm,
                        sessionState = sessionState,
                        authVm = authVm,
                        notificationsVm = notificationsVm,
                        pendingRoleHint = pendingRoleHint,
                        onPendingRoleHintChange = { pendingRoleHint = it },
                        overlayVisible = overlayVisible,
                        onOverlayVisibleChange = { overlayVisible = it },
                        hazeState = hazeContentState,
                        blurEnabled = blurEnabled
                    )
                }

                if (sessionState.isLoggedIn && isMentorRole && pendingPrompt != null && !isVideoCallRoute) {
                    val bookingId = pendingPrompt.id
                    PendingBookingPromptDialog(
                        booking = pendingPrompt,
                        actionBusy = pendingActionBusy,
                        onAccept = {
                            if (pendingActionBusy) return@PendingBookingPromptDialog
                            pendingActionBusy = true
                            mentorBookingsVm.accept(bookingId) { ok ->
                                pendingActionBusy = false
                                if (ok) {
                                    mentorBookingsVm.dismissPendingPrompt(bookingId)
                                    mentorBookingsVm.refresh()
                                    Toast.makeText(context, "Da chap nhan booking", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Khong the chap nhan booking", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onDecline = {
                            if (pendingActionBusy) return@PendingBookingPromptDialog
                            pendingActionBusy = true
                            mentorBookingsVm.decline(bookingId, "Mentor declined") { ok ->
                                pendingActionBusy = false
                                if (ok) {
                                    mentorBookingsVm.dismissPendingPrompt(bookingId)
                                    mentorBookingsVm.refresh()
                                    Toast.makeText(context, "Da tu choi booking", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Khong the tu choi booking", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onDismiss = { mentorBookingsVm.dismissPendingPrompt(bookingId) }
                    )
                }
            }
        }
        }
    }
}
