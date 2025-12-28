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

        // Extract route from intent
        pendingRoute = intent?.getStringExtra(NotificationHelper.EXTRA_NAV_ROUTE)

        // Observe lifecycle to ensure proper cleanup
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // This block will execute when activity is started
                // and cancel when stopped, preventing memory leaks
            }
        }

        setContent {
            AppNav(
                initialRoute = pendingRoute,
                onRouteConsumed = { pendingRoute = null }
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Important: update the intent
        pendingRoute = intent.getStringExtra(NotificationHelper.EXTRA_NAV_ROUTE)
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
