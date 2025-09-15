package com.mentorme.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mentorme.app.ui.auth.LoginScreen
import com.mentorme.app.ui.auth.RegisterScreen
import com.mentorme.app.ui.home.HomeScreen
import com.mentorme.app.ui.mentors.MentorsScreen
import com.mentorme.app.ui.mentors.MentorDetailScreen
import com.mentorme.app.ui.booking.BookingsScreen
import com.mentorme.app.ui.chat.MessagesScreen
import com.mentorme.app.ui.profile.ProfileScreen
import com.mentorme.app.ui.layout.BottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorMeNavigation(
    isAuthenticated: Boolean = false
) {
    val navController = rememberNavController()

    if (!isAuthenticated) {
        // Auth flow
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        // Handle login success - typically would trigger a state change
                        // that causes isAuthenticated to become true
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = {
                        // Handle register success
                    }
                )
            }
        }
    } else {
        // Main app flow
        Scaffold(
            bottomBar = {
                BottomNavigation(
                    navController = navController,
                    items = bottomNavigationItems
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(navController = navController)
                }
                composable(Screen.Mentors.route) {
                    MentorsScreen(navController = navController)
                }
                composable(Screen.Bookings.route) {
                    BookingsScreen(navController = navController)
                }
                composable(Screen.Messages.route) {
                    MessagesScreen(navController = navController)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(navController = navController)
                }
                composable(Screen.MentorDetail.route) { backStackEntry ->
                    val mentorId = backStackEntry.arguments?.getString("mentorId") ?: ""
                    MentorDetailScreen(
                        mentorId = mentorId,
                        navController = navController
                    )
                }
            }
        }
    }
}
