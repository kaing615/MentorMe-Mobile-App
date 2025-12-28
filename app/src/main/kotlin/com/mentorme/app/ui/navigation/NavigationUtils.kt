package com.mentorme.app.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

internal fun goToSearch(nav: NavHostController) {
    if (nav.currentDestination?.route != Routes.search) {
        nav.navigate(Routes.search) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}
