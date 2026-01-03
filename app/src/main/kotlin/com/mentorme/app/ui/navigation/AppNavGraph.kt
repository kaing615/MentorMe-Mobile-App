package com.mentorme.app.ui.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.auth.AuthScreen
import com.mentorme.app.ui.auth.AuthViewModel
import com.mentorme.app.ui.auth.RegisterPayload
import com.mentorme.app.ui.booking.BookingChooseTimeScreen
import com.mentorme.app.ui.booking.BookingDetailScreen
import com.mentorme.app.ui.booking.BookingDraft
import com.mentorme.app.ui.booking.BookingSummaryScreen
import com.mentorme.app.ui.calendar.CalendarTab
import com.mentorme.app.ui.calendar.MenteeCalendarScreen
import com.mentorme.app.ui.calendar.MentorCalendarScreen
import com.mentorme.app.ui.chat.ChatScreen
import com.mentorme.app.ui.chat.MentorMessagesScreen
import com.mentorme.app.ui.chat.MessagesScreen
import com.mentorme.app.ui.dashboard.MentorDashboardScreen
import com.mentorme.app.ui.home.HomeScreen
import com.mentorme.app.ui.notifications.NotificationDetailScreen
import com.mentorme.app.ui.notifications.NotificationsScreen
import com.mentorme.app.ui.notifications.NotificationsViewModel
import com.mentorme.app.ui.onboarding.MenteeOnboardingScreen
import com.mentorme.app.ui.onboarding.MentorOnboardingScreen
import com.mentorme.app.ui.onboarding.PendingApprovalScreen
import com.mentorme.app.ui.profile.MentorProfileScreen
import com.mentorme.app.ui.profile.ProfileScreen
import com.mentorme.app.ui.profile.UserHeader
import com.mentorme.app.ui.profile.UserRole
import com.mentorme.app.ui.search.SearchMentorScreen
import com.mentorme.app.ui.session.SessionState
import com.mentorme.app.ui.session.SessionViewModel
import com.mentorme.app.ui.wallet.AddPaymentMethodScreen
import com.mentorme.app.ui.wallet.EditPaymentMethodScreen
import com.mentorme.app.ui.wallet.PaymentMethod
import com.mentorme.app.ui.wallet.PaymentMethodScreen
import com.mentorme.app.ui.wallet.TopUpScreen
import com.mentorme.app.ui.wallet.WalletViewModel
import com.mentorme.app.ui.wallet.WithdrawScreen
import com.mentorme.app.ui.videocall.VideoCallScreen
import androidx.compose.runtime.rememberCoroutineScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.mentorme.app.domain.usecase.chat.GetConversationWithPeerUseCase
import com.mentorme.app.core.utils.AppResult
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChatNavDeps {
    fun getConversationWithPeerUseCase(): GetConversationWithPeerUseCase
}

@Composable
internal fun AppNavGraph(
    nav: NavHostController,
    sessionVm: SessionViewModel,
    sessionState: SessionState,
    authVm: AuthViewModel,
    notificationsVm: NotificationsViewModel,
    pendingRoleHint: String?,
    onPendingRoleHintChange: (String?) -> Unit,
    @Suppress("UNUSED_PARAMETER") overlayVisible: Boolean,
    onOverlayVisibleChange: (Boolean) -> Unit,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = false,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ Get chat use case for navigation
    val chatDeps = remember(context) {
        EntryPointAccessors.fromApplication(context, ChatNavDeps::class.java)
    }
    val getConversationUseCase = remember { chatDeps.getConversationWithPeerUseCase() }

    val hazeModifier = if (blurEnabled && hazeState != null) {
        Modifier.hazeSource(state = hazeState)
    } else {
        Modifier
    }

    Box(
        modifier = Modifier.fillMaxSize().then(hazeModifier)
    ) {
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
                        val ok =
                            p.fullName.isNotBlank() && p.email.isNotBlank() && p.password.length >= 6
                        if (ok) {
                            sessionVm.refreshStatus()
                        }
                        ok
                    },
                    onResetPassword = { /* TODO */ },
                    onNavigateToMenteeHome = {
                        // user explicitly chose mentee flow during auth -> hint role locally
                        onPendingRoleHintChange("mentee")
                        sessionVm.refreshStatus()
                    },
                    onNavigateToMentorHome = {
                        onPendingRoleHintChange("mentor")
                        sessionVm.refreshStatus()
                    },
                    onNavigateToOnboarding = { tokenFromAuth: String?, role: String? ->
                        // Auth flow decided onboarding is next. Set a local hint so we can navigate immediately
                        onPendingRoleHintChange(role ?: pendingRoleHint)
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
                val roleArg = backStackEntry.arguments?.getString("role") ?: sessionState.role
                ?: pendingRoleHint ?: "mentee"

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
                                Toast.makeText(
                                    context,
                                    "Không thể làm mới trạng thái.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (status == "pending-mentor") {
                                Toast.makeText(
                                    context,
                                    "Hồ sơ vẫn đang chờ duyệt.",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                    onNavigateToCategory = { categoryName ->
                        // ✅ Navigate to search with expertise filter
                        nav.navigate(Routes.searchWithExpertise(categoryName)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSchedule = {
                        // ✅ Navigate to mentee calendar/schedule
                        nav.navigate(Routes.calendar("upcoming")) {
                            launchSingleTop = true
                        }
                    },
                    onJoinSession = { bookingId ->
                        nav.navigate(Routes.videoCall(bookingId)) {
                            launchSingleTop = true
                        }
                    },
                    onMessage = { mentorId ->
                        // ✅ Open chat with mentor
                        scope.launch {
                            when (val result = getConversationUseCase(mentorId)) {
                                is AppResult.Success<*> -> {
                                    val conversationId = result.data
                                    if (conversationId != null) {
                                        // Conversation exists, navigate to chat
                                        nav.navigate("${Routes.Chat}/$conversationId") {
                                            launchSingleTop = true
                                        }
                                    } else {
                                        // No conversation exists - need to book first
                                        Toast.makeText(
                                            context,
                                            "Bạn cần đặt lịch với mentor này trước khi có thể nhắn tin",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                is AppResult.Error -> {
                                    Toast.makeText(
                                        context,
                                        "Không thể mở chat: ${result.throwable}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                AppResult.Loading -> {
                                    // Should not happen
                                }
                            }
                        }
                    },
                    onBookSlot = { mentor, occurrenceId, date, startTime, endTime, priceVnd, note ->
                        nav.currentBackStackEntry?.savedStateHandle?.set("booking_notes", note)
                        nav.currentBackStackEntry?.savedStateHandle?.set("booking_mentor_name", mentor.name)
                        nav.navigate("bookingSummary/${mentor.id}/$date/$startTime/$endTime/$priceVnd/$occurrenceId")
                    },
                    onOverlayOpened = { onOverlayVisibleChange(true) },
                    onOverlayClosed = { onOverlayVisibleChange(false) }
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
                    onJoinSession = { bookingId ->
                        nav.navigate(Routes.videoCall(bookingId)) {
                            launchSingleTop = true
                        }
                    },
                    onViewBookingDetail = { bookingId ->
                        nav.navigate(Routes.bookingDetail(bookingId)) {
                            launchSingleTop = true
                        }
                    },
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
                    onViewSession = { bookingId ->
                        nav.navigate("booking_detail/$bookingId")
                    },
                    onJoinSession = { bookingId ->
                        nav.navigate(Routes.videoCall(bookingId)) {
                            launchSingleTop = true
                        }
                    },
                    onCreateSession = { Log.d("AppNav", "Create session - TODO") },
                    onUpdateAvailability = { Log.d("AppNav", "Update availability - TODO") },
                    onCancelSession = { sessionId ->
                        Log.d(
                            "AppNav",
                            "Cancel session $sessionId - TODO"
                        )
                    },
                    onOverlayChange = onOverlayVisibleChange // ✅ NEW: Wire overlay callback to hide bottom nav
                )
            }

            composable(Routes.MentorMessages) {
                MentorMessagesScreen(
                    onOpenConversation = { convId -> nav.navigate("${Routes.Chat}/$convId") },
                    onFilterStudents = { Log.d("AppNav", "Filter students - TODO") },
                    onSearchConversations = { query ->
                        Log.d(
                            "AppNav",
                            "Search conversations: $query - TODO"
                        )
                    }
                )
            }

            composable(Routes.MentorProfile) {
                val localAuthVm = hiltViewModel<AuthViewModel>()
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
                    onManageServices = {
                        Log.d(
                            "AppNav",
                            "MentorProfile: onManageServices - TODO"
                        )
                    },
                    onViewStatistics = {
                        Log.d(
                            "AppNav",
                            "MentorProfile: onViewStatistics - TODO"
                        )
                    },
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
                val localAuthVm = hiltViewModel<AuthViewModel>()
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
                    onManageServices = {
                        Log.d(
                            "AppNav",
                            "MentorProfile: onManageServices - TODO"
                        )
                    },
                    onViewStatistics = {
                        Log.d(
                            "AppNav",
                            "MentorProfile: onViewStatistics - TODO"
                        )
                    },
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
                route = Routes.searchWithExpertise,
                arguments = listOf(navArgument("expertise") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val expertise = backStackEntry.arguments?.getString("expertise")
                SearchMentorScreen(
                    initialExpertise = expertise, // ✅ Pass expertise to filter
                    onOpenProfile = { /* handled by sheet inside SearchMentorScreen */ },
                    onBook = { id ->
                        val targetId = if (id.startsWith("m") && id.drop(1).all { it.isDigit() }) id.drop(1) else id
                        nav.navigate("booking/$targetId")
                    },
                    onMessage = { mentorId ->
                        //  STEP C: Open chat with mentor
                        scope.launch {
                            when (val result = getConversationUseCase(mentorId)) {
                                is AppResult.Success<*> -> {
                                    val conversationId = result.data
                                    if (conversationId != null) {
                                        // Conversation exists, navigate to chat
                                        nav.navigate("${Routes.Chat}/$conversationId") {
                                            launchSingleTop = true
                                        }
                                    } else {
                                        // No conversation exists - need to book first
                                        Toast.makeText(
                                            context,
                                            "Bạn cần đặt lịch với mentor này trước khi có thể nhắn tin",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                is AppResult.Error -> {
                                    Toast.makeText(
                                        context,
                                        "Không thể mở chat: ${result.throwable}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                AppResult.Loading -> {
                                    // Should not happen
                                }
                            }
                        }
                    },
                    onBookSlot = { mentor, occurrenceId, date, startTime, endTime, priceVnd, note ->
                        nav.currentBackStackEntry?.savedStateHandle?.set("booking_notes", note)
                        nav.currentBackStackEntry?.savedStateHandle?.set("booking_mentor_name", mentor.name)
                        nav.navigate("bookingSummary/${mentor.id}/$date/$startTime/$endTime/$priceVnd/$occurrenceId")
                    },
                    onOverlayOpened = { onOverlayVisibleChange(true) },
                    onOverlayClosed = { onOverlayVisibleChange(false) }
                )
            }

            composable(
                route = Routes.CalendarWithTab,
                arguments = listOf()
            ) { backStackEntry ->
                val tabArg = backStackEntry.arguments?.getString("tab")
                MenteeCalendarScreen(
                    startTab = CalendarTab.fromRouteArg(tabArg),
                    onJoinSession = { booking ->
                        nav.navigate(Routes.videoCall(booking.id)) {
                            launchSingleTop = true
                        }
                    },
                    onOpenDetail = { booking ->
                        nav.navigate("booking_detail/${booking.id}")
                    }
                )
            }

            composable("booking_detail/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
                BookingDetailScreen(
                    bookingId = bookingId,
                    onBack = { nav.popBackStack() },
                    onJoinSession = { bookingId ->
                        nav.navigate(Routes.videoCall(bookingId)) {
                            launchSingleTop = true
                        }
                    }
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
                    onOpenDetail = { route -> nav.navigate(route) },
                    onJoinSession = { bookingId ->
                        nav.navigate(Routes.videoCall(bookingId)) {
                            launchSingleTop = true
                        }
                    },
                    viewModel = notificationsVm
                )
            }

            composable(Routes.NotificationDetail) { backStackEntry ->
                val notificationId = backStackEntry.arguments?.getString("notificationId")
                    ?: return@composable
                NotificationDetailScreen(
                    notificationId = notificationId,
                    onBack = { nav.popBackStack() },
                    onJoinSession = { bookingId ->
                        nav.navigate(Routes.videoCall(bookingId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("${Routes.Chat}/{conversationId}") { backStackEntry ->
                val convId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
                ChatScreen(
                    conversationId = convId,
                    onBack = { nav.popBackStack() },
                    onJoinSession = { bookingId ->
                        nav.navigate(Routes.videoCall(bookingId)) {
                            launchSingleTop = true
                        }
                    },
                    onOpenBookingDetail = { bookingId ->
                        nav.navigate("booking_detail/$bookingId")
                    }
                )
            }

            composable("${Routes.VideoCall}/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
                VideoCallScreen(
                    bookingId = bookingId,
                    onBack = { nav.popBackStack() }
                )
            }

            composable(Routes.Profile) {
                val localAuthVm = hiltViewModel<AuthViewModel>()
                val profileVm = hiltViewModel<com.mentorme.app.ui.profile.ProfileViewModel>()
                val profileEntry = remember { nav.getBackStackEntry(Routes.Profile) }
                val walletVm: WalletViewModel = hiltViewModel(profileEntry)

                // ✅ Wrap with hazeSource to enable blur effect from bottom bar
                val contentModifier = if (blurEnabled && hazeState != null) {
                    Modifier.fillMaxSize().hazeSource(state = hazeState)
                } else {
                    Modifier.fillMaxSize()
                }

                Box(modifier = contentModifier) {
                    ProfileScreen(
                        vm = profileVm,
                        user = UserHeader(fullName = "Nguyễn Văn A", email = "a@example.com", role = UserRole.MENTEE),
                        notificationsViewModel = notificationsVm,
                        walletViewModel = walletVm,
                        onOpenNotifications = { nav.navigate(Routes.Notifications) },
                        onOpenTopUp = { nav.navigate(Routes.TopUp) },
                        onOpenWithdraw = { nav.navigate(Routes.Withdraw) },
                        onOpenChangeMethod = { nav.navigate(Routes.PaymentMethods) },
                        onAddMethod = { nav.navigate(Routes.AddPaymentMethod) },
                        methods = emptyList(),
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
            }

            // ---------- BOOKING ----------
            composable("booking/{mentorId}") { backStackEntry ->
                val mentorId = backStackEntry.arguments?.getString("mentorId") ?: return@composable
                val mentor = MockData.mockMentors.find { it.id == mentorId }
                    ?: com.mentorme.app.data.model.Mentor(
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
                val slots =
                    (uiState as? com.mentorme.app.ui.booking.BookingFlowViewModel.UiState.Success<
                            List<com.mentorme.app.ui.booking.BookingFlowViewModel.TimeSlotUi>
                            >)?.data ?: emptyList()
                val loading =
                    uiState is com.mentorme.app.ui.booking.BookingFlowViewModel.UiState.Loading
                val error =
                    (uiState as? com.mentorme.app.ui.booking.BookingFlowViewModel.UiState.Error)?.message

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
                val notes =
                    nav.previousBackStackEntry?.savedStateHandle?.get<String>("booking_notes") ?: ""
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
                            is AppResult.Success -> {
                                nav.navigate(Routes.calendar("pending")) {
                                    popUpTo(nav.graph.findStartDestination().id) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }

                            is AppResult.Error -> {
                                val msg = res.throwable
                                val code = msg.substringAfter("HTTP ", "").substringBefore(":")
                                    .toIntOrNull()
                                when (code) {
                                    401 -> {
                                        authVm.signOut {
                                            nav.navigate(Routes.Auth) {
                                                popUpTo(nav.graph.findStartDestination().id) {
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    }

                                    409, 422 -> {
                                        Toast.makeText(
                                            context,
                                            "Khung giờ đã được đặt. Vui lòng chọn thời gian khác.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    else -> {
                                        val errorMsg =
                                            if (msg.isBlank()) "Có lỗi xảy ra, vui lòng thử lại." else msg
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }

                            AppResult.Loading -> Unit
                        }
                    },
                    onBack = { nav.popBackStack() }
                )
            }

            // ---------- WALLET ----------
            composable(Routes.TopUp) { backStackEntry ->
                val parentEntry = remember { nav.getBackStackEntry(Routes.Profile) }
                val walletVm: WalletViewModel = hiltViewModel(parentEntry)

                TopUpScreen(
                    onBack = { nav.popBackStack() },
                    walletViewModel = walletVm
                )
            }

            composable(Routes.Withdraw) {
                val parentEntry = remember { nav.getBackStackEntry(Routes.Profile) }
                val walletVm: WalletViewModel = hiltViewModel(parentEntry)

                WithdrawScreen(
                    navController = nav,
                    walletViewModel = walletVm,
                    onBack = { nav.popBackStack() }
                )
            }


            composable(Routes.PaymentMethods) { backStackEntry ->
                val parentEntry = remember {
                    nav.getBackStackEntry(Routes.Profile)
                }
                val walletViewModel: WalletViewModel = hiltViewModel(parentEntry)

                val methods by walletViewModel.paymentMethods.collectAsState()

                PaymentMethodScreen(
                    walletViewModel = walletViewModel,
                    methods = methods,
                    onBack = { nav.popBackStack() },
                    onChosen = { nav.popBackStack() },
                    onAddNew = { nav.navigate(Routes.AddPaymentMethod) },
                    onEditSelected = { method ->
                        nav.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("method", method)

                        nav.navigate(Routes.EditPaymentMethod)
                    }
                )
            }

            composable(Routes.AddPaymentMethod) {
                val parent = remember { nav.getBackStackEntry(Routes.Profile) }
                val walletVm: WalletViewModel = hiltViewModel(parent)

                AddPaymentMethodScreen(
                    onBack = { nav.popBackStack() },
                    onSaved = { req ->
                        walletVm.addPaymentMethod(req)
                        nav.popBackStack()
                    }
                )
            }

            composable(Routes.EditPaymentMethod) {
                val parent = remember {
                    nav.getBackStackEntry(Routes.Profile)
                }
                val walletVm: WalletViewModel = hiltViewModel(parent)

                val method =
                    nav.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<PaymentMethod>("method")
                        ?: return@composable

                EditPaymentMethodScreen(
                    method = method,
                    onBack = { nav.popBackStack() },
                    onSaved = { updated ->
                        walletVm.updatePaymentMethod(method.id, updated)
                        nav.popBackStack()
                    }
                )
            }
        }
    }
}