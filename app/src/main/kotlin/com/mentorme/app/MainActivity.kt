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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mentorme.app.core.notifications.NotificationHelper
import com.mentorme.app.ui.navigation.AppNav
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

        // ✅ Extract route from intent - xử lý cả khi app bị kill
        pendingRoute = extractRouteFromIntent(intent)
        android.util.Log.d("MainActivity", "onCreate - pendingRoute: $pendingRoute, action: ${intent?.action}")

        // Observe lifecycle to ensure proper cleanup
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // This block will execute when activity is started
                // and cancel when stopped, preventing memory leaks
            }
        }

        setContent {
            AppNav(
                pendingRoute = pendingRoute,
                onRouteConsumed = {
                    android.util.Log.d("MainActivity", "Route consumed, clearing pendingRoute")
                    pendingRoute = null
                }
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Important: update the intent

        // ✅ Update pendingRoute which will trigger recomposition in AppNav
        val newRoute = extractRouteFromIntent(intent)
        android.util.Log.d("MainActivity", "onNewIntent - pendingRoute: $newRoute, action: ${intent.action}")

        // ✅ Only update if we have a new route
        if (!newRoute.isNullOrBlank()) {
            pendingRoute = newRoute
        }
    }

    /**
     * ✅ Helper function để extract route từ Intent một cách nhất quán
     */
    private fun extractRouteFromIntent(intent: android.content.Intent?): String? {
        if (intent == null) return null

        val route = intent.getStringExtra(NotificationHelper.EXTRA_NAV_ROUTE)
        android.util.Log.d("MainActivity", "extractRouteFromIntent - route: $route, extras: ${intent.extras?.keySet()}")

        return route
    }

    override fun onPause() {
        super.onPause()
        // Clear any pending operations when activity is paused
    }

    override fun onDestroy() {
        // Clean up before destruction to prevent DeadObjectException
        pendingRoute = null
        super.onDestroy()
    }
}
