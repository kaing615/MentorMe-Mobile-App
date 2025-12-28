package com.mentorme.app.ui.navigation

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
internal sealed class AppPhase {
    object Loading : AppPhase()
    object Auth : AppPhase()
    object Onboarding : AppPhase()
    object Pending : AppPhase()
    object Home : AppPhase()
}

internal fun resolvePhase(isLoggedIn: Boolean, status: String?): AppPhase {
    if (!isLoggedIn) return AppPhase.Auth
    if (status == null) return AppPhase.Loading

    return when (status.trim().lowercase().replace('_', '-')) {
        "onboarding" -> AppPhase.Onboarding
        "pending-mentor" -> AppPhase.Pending
        "active" -> AppPhase.Home
        else -> AppPhase.Home
    }
}
