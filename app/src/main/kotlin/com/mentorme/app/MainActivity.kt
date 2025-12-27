// MainActivity.kt
package com.mentorme.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mentorme.app.core.notifications.NotificationHelper
import com.mentorme.app.ui.navigation.AppNav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var pendingRoute by mutableStateOf<String?>(null)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Trong suốt hoàn toàn cho status + navigation bars
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(0x00000000, 0x00000000),
            navigationBarStyle = SystemBarStyle.auto(0x00000000, 0x00000000)
        )
        super.onCreate(savedInstanceState)
        pendingRoute = intent?.getStringExtra(NotificationHelper.EXTRA_NAV_ROUTE)
        setContent {
            AppNav(
                initialRoute = pendingRoute,
                onRouteConsumed = { pendingRoute = null }
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        pendingRoute = intent.getStringExtra(NotificationHelper.EXTRA_NAV_ROUTE)
    }
}
