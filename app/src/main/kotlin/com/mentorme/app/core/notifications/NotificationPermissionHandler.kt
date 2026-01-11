package com.mentorme.app.core.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.mentorme.app.core.datastore.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Composable to handle runtime notification permission request for Android 13+
 * Automatically requests permission on first launch if not granted
 */
@Composable
fun RequestNotificationPermission(
    dataStoreManager: DataStoreManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Track if we've already asked for permission this session
    var hasAskedThisSession by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Save that we've asked for permission
        scope.launch {
            dataStoreManager.setNotificationPermissionAsked(true)
        }

        if (isGranted) {
            android.util.Log.d("NotificationPermission", "Notification permission granted")
        } else {
            android.util.Log.w("NotificationPermission", "Notification permission denied")
        }
    }

    // Check and request permission on first composition
    LaunchedEffect(Unit) {
        // Only needed for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = NotificationHelper.hasPostPermission(context)
            val hasAskedBefore = dataStoreManager.hasAskedNotificationPermission().first()

            if (!hasPermission && !hasAskedBefore && !hasAskedThisSession) {
                hasAskedThisSession = true
                android.util.Log.d("NotificationPermission", "Requesting notification permission")
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

