package com.mentorme.app.ui.videocall

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularAlt1Bar
import androidx.compose.material.icons.filled.SignalCellularAlt2Bar
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mentorme.app.ui.chat.components.GlassIconButton
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.theme.liquidGlassStrong
import org.webrtc.SurfaceViewRenderer

@Composable
fun VideoCallScreen(
    bookingId: String,
    onBack: () -> Unit
) {
    val viewModel = hiltViewModel<VideoCallViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Lock orientation to portrait during call
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        onDispose {
            activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    
    // Handle lifecycle events for background/foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.onAppGoesToBackground()
                Lifecycle.Event.ON_RESUME -> viewModel.onAppComesToForeground()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Show toast messages
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearToast()
        }
    }
    
    // Debug logging
    LaunchedEffect(state.phase, state.peerJoined, state.admitted, state.role) {
        android.util.Log.d("VideoCallScreen", "State updated - phase: ${state.phase}, role: ${state.role}, peerJoined: ${state.peerJoined}, admitted: ${state.admitted}")
    }

    val permissions = remember {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
    var permissionsGranted by rememberSaveable {
        mutableStateOf(
            permissions.all { perm ->
                ContextCompat.checkSelfPermission(context, perm) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var requested by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsGranted = result.values.all { it }
        viewModel.setPermissionsGranted(permissionsGranted)
    }

    LaunchedEffect(bookingId) {
        viewModel.start(bookingId)
    }

    LaunchedEffect(permissionsGranted) {
        viewModel.setPermissionsGranted(permissionsGranted)
    }

    LaunchedEffect(permissionsGranted, requested) {
        if (!permissionsGranted && !requested) {
            requested = true
            launcher.launch(permissions)
        }
    }

    DisposableEffect(bookingId) {
        onDispose {
            viewModel.leaveCall()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Preview mode overlay
        if (state.isPreviewMode && permissionsGranted) {
            PreviewModeOverlay(
                isAudioEnabled = state.isAudioEnabled,
                isVideoEnabled = state.isVideoEnabled,
                isSpeakerEnabled = state.isSpeakerEnabled,
                onToggleAudio = { viewModel.toggleAudio() },
                onToggleVideo = { viewModel.toggleVideo() },
                onToggleSpeaker = { viewModel.toggleSpeaker() },
                onSwitchCamera = { viewModel.switchCamera() },
                onStartCall = { viewModel.exitPreviewMode() },
                onBack = onBack
            )
        }
        
        // Remote video - full screen (rendered first, behind local video)
        AndroidView(
            factory = { ctx ->
                SurfaceViewRenderer(ctx).apply {
                    setZOrderMediaOverlay(false)
                    setZOrderOnTop(false)
                    // Initialize renderer on creation
                    post {
                        viewModel.bindRemoteRenderer(this)
                    }
                }
            },
            update = { view ->
                // Ensure renderer is ready when recomposed
                view.holder?.surface?.let { surface ->
                    if (!surface.isValid) {
                        view.requestLayout()
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Local video - small preview in corner (rendered on top)
        AndroidView(
            factory = { ctx ->
                SurfaceViewRenderer(ctx).apply {
                    setZOrderMediaOverlay(true)
                    setZOrderOnTop(true)
                    // Initialize renderer on creation
                    post {
                        viewModel.bindLocalRenderer(this)
                    }
                }
            },
            update = { view ->
                // Ensure local preview stays on top
                view.setZOrderMediaOverlay(true)
                view.setZOrderOnTop(true)
            },
            modifier = Modifier
                .padding(12.dp)
                .size(120.dp)
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            
            // Network quality indicator
            if (state.phase == CallPhase.InCall) {
                NetworkQualityIndicator(
                    quality = state.networkQuality,
                    rttMs = state.rttMs
                )
            }
        }

        val statusLabel = remember(state.phase, state.role, state.peerJoined, state.reconnectAttempt) {
            when (state.phase) {
                CallPhase.InCall -> "In call"
                CallPhase.Reconnecting -> {
                    if (state.reconnectAttempt > 0) {
                        "Reconnecting (${state.reconnectAttempt}/${state.maxReconnectAttempts})"
                    } else {
                        "Reconnecting"
                    }
                }
                CallPhase.Joining -> "Joining room"
                CallPhase.WaitingForPeer -> "Waiting for participant"
                CallPhase.WaitingForAdmit -> {
                    if (state.role == "mentor" && state.peerJoined) {
                        "Mentee is ready"
                    } else if (state.role == "mentor") {
                        "Waiting for mentee"
                    } else {
                        "Waiting for admission"
                    }
                }
                CallPhase.Connecting -> "Connecting"
                CallPhase.Ended -> "Call ended"
                CallPhase.Error -> "Connection error"
                CallPhase.Idle -> ""
            }
        }
        val timerText = remember(state.callDurationSec) {
            formatDuration(state.callDurationSec)
        }

        if (statusLabel.isNotBlank()) {
            CallStatusPill(
                label = statusLabel,
                timerText = if (state.phase == CallPhase.InCall) timerText else null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            )
        }
        
        // Peer connection status banner
        if (state.phase == CallPhase.InCall && state.peerConnectionStatus != null) {
            when (state.peerConnectionStatus) {
                "reconnecting" -> PeerStatusBanner(
                    message = "Participant is reconnecting...",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = if (statusLabel.isNotBlank()) 52.dp else 12.dp)
                )
                "disconnected" -> PeerStatusBanner(
                    message = "Participant disconnected",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = if (statusLabel.isNotBlank()) 52.dp else 12.dp)
                )
            }
        }

        if (!permissionsGranted) {
            CallOverlay(
                title = "Camera and mic permission required",
                actionLabel = "Request access",
                onAction = { launcher.launch(permissions) }
            )
        } else {
            val statusText = when (state.phase) {
                CallPhase.Joining -> "Joining session"
                CallPhase.WaitingForPeer -> "Waiting for mentee to join"
                CallPhase.WaitingForAdmit -> {
                    if (state.role == "mentor" && state.peerJoined) {
                        "Mentee is ready to start"
                    } else if (state.role == "mentor") {
                        "Waiting for mentee to join"
                    } else {
                        "Mentor will start the session soon"
                    }
                }
                CallPhase.Connecting -> "Starting session..."
                CallPhase.Reconnecting -> "Reconnecting"
                CallPhase.Ended -> "Call ended"
                CallPhase.Error -> state.errorMessage ?: "Call error"
                else -> null
            }

            if (statusText != null && state.phase != CallPhase.InCall) {
                CallOverlay(
                    title = statusText,
                    actionLabel = if (state.phase == CallPhase.Error || state.phase == CallPhase.Reconnecting) "Retry" else null,
                    onAction = {
                        if (state.phase == CallPhase.Error || state.phase == CallPhase.Reconnecting) {
                            viewModel.retry()
                        }
                    }
                )
            }

            if (state.role == "mentor" && 
                state.peerJoined && 
                !state.admitted && 
                state.phase == CallPhase.WaitingForAdmit) {
                android.util.Log.d("VideoCallScreen", "Showing admit popup")
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                        .liquidGlassStrong(radius = 20.dp, alpha = 0.2f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Mentee is ready and waiting",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Click to start the session",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        MMButton(
                            text = "Start Session",
                            onClick = { viewModel.admit() },
                            useGlass = false
                        )
                    }
                }
            }
            
            // Time warning dialog
            state.timeWarningMessage?.let { message ->
                if (state.showTimeWarning) {
                    TimeWarningDialog(
                        message = message,
                        remainingMinutes = state.remainingMinutes,
                        onDismiss = { viewModel.dismissTimeWarning() }
                    )
                }
            }

            CallControls(
                state = state,
                modifier = Modifier.align(Alignment.BottomCenter),
                onToggleAudio = { viewModel.toggleAudio() },
                onToggleVideo = { viewModel.toggleVideo() },
                onToggleSpeaker = { viewModel.toggleSpeaker() },
                onSwitchCamera = { viewModel.switchCamera() },
                onEndCall = { viewModel.endCall(); onBack() }
            )
        }
    }
}

@Composable
private fun CallControls(
    state: VideoCallUiState,
    modifier: Modifier = Modifier,
    onToggleAudio: () -> Unit,
    onToggleVideo: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlButton(
            icon = if (state.isAudioEnabled) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = "Toggle mic",
            onClick = onToggleAudio
        )
        ControlButton(
            icon = if (state.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
            contentDescription = "Toggle camera",
            onClick = onToggleVideo
        )
        ControlButton(
            icon = Icons.Default.Cameraswitch,
            contentDescription = "Switch camera",
            onClick = onSwitchCamera
        )
        ControlButton(
            icon = if (state.isSpeakerEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PhoneInTalk,
            contentDescription = if (state.isSpeakerEnabled) "Speaker" else "Earpiece",
            onClick = onToggleSpeaker
        )
        ControlButton(
            icon = Icons.Default.CallEnd,
            contentDescription = "End call",
            onClick = onEndCall,
            background = Color(0xFFDC2626),
            useGlass = false
        )
    }
}

@Composable
private fun CallOverlay(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .liquidGlassStrong(radius = 20.dp, alpha = 0.22f)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            color = Color.Transparent
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (!actionLabel.isNullOrBlank() && onAction != null) {
                    Spacer(Modifier.height(12.dp))
                    MMButton(
                        text = actionLabel,
                        onClick = onAction,
                        useGlass = false
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    background: Color = Color.Transparent,
    useGlass: Boolean = true
) {
    val modifier = Modifier
        .size(52.dp)
        .clip(CircleShape)
        .then(
            if (useGlass) {
                Modifier.liquidGlassStrong(radius = 26.dp, alpha = 0.25f)
            } else {
                Modifier.background(background)
            }
        )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(52.dp)) {
            Icon(icon, contentDescription = contentDescription, tint = Color.White)
        }
    }
}

@Composable
private fun CallStatusPill(
    label: String,
    timerText: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .liquidGlassStrong(radius = 18.dp, alpha = 0.22f)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color.Transparent
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
            if (!timerText.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(timerText, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun PeerStatusBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .liquidGlassStrong(radius = 18.dp, alpha = 0.3f)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = Color(0xFFFFCC00),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Composable
private fun NetworkQualityIndicator(
    quality: NetworkQuality,
    rttMs: Double?
) {
    val icon = when (quality) {
        NetworkQuality.Excellent, NetworkQuality.Good -> Icons.Default.SignalCellularAlt
        NetworkQuality.Fair -> Icons.Default.SignalCellularAlt2Bar
        NetworkQuality.Poor, NetworkQuality.VeryPoor -> Icons.Default.SignalCellularAlt1Bar
        NetworkQuality.Unknown -> null
    }
    
    val color = when (quality) {
        NetworkQuality.Excellent -> Color.Green
        NetworkQuality.Good -> Color(0xFF90EE90)
        NetworkQuality.Fair -> Color.Yellow
        NetworkQuality.Poor -> Color(0xFFFF8C00)
        NetworkQuality.VeryPoor -> Color.Red
        NetworkQuality.Unknown -> Color.Gray
    }
    
    icon?.let {
        Surface(
            modifier = Modifier.liquidGlassStrong(radius = 8.dp, alpha = 0.3f),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = it,
                    contentDescription = "Network quality",
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                rttMs?.let { rtt ->
                    Text(
                        text = "${rtt.toInt()}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewModeOverlay(
    isAudioEnabled: Boolean,
    isVideoEnabled: Boolean,
    isSpeakerEnabled: Boolean,
    onToggleAudio: () -> Unit,
    onToggleVideo: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onSwitchCamera: () -> Unit,
    onStartCall: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // Back button
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
        }
        
        // Preview controls and start button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Surface(
                modifier = Modifier.liquidGlassStrong(radius = 12.dp, alpha = 0.3f),
                color = Color.Transparent
            ) {
                Text(
                    text = "Camera & Microphone Preview",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Preview controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(
                    icon = if (isAudioEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Toggle mic",
                    onClick = onToggleAudio
                )
                ControlButton(
                    icon = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    contentDescription = "Toggle camera",
                    onClick = onToggleVideo
                )
                ControlButton(
                    icon = Icons.Default.Cameraswitch,
                    contentDescription = "Switch camera",
                    onClick = onSwitchCamera
                )
                ControlButton(
                    icon = if (isSpeakerEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PhoneInTalk,
                    contentDescription = if (isSpeakerEnabled) "Speaker" else "Earpiece",
                    onClick = onToggleSpeaker
                )
            }
            
            // Start call button
            MMButton(
                text = "Join Call",
                onClick = onStartCall,
                modifier = Modifier.width(200.dp),
                useGlass = false
            )
        }
    }
}

@Composable
private fun TimeWarningDialog(
    message: String,
    remainingMinutes: Int?,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .liquidGlassStrong(radius = 20.dp, alpha = 0.3f)
                .padding(24.dp),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Time warning",
                    tint = Color.Yellow,
                    modifier = Modifier.size(48.dp)
                )
                
                Text(
                    text = "Time Notice",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                remainingMinutes?.let { minutes ->
                    if (minutes > 0) {
                        Text(
                            text = "$minutes minute${if (minutes > 1) "s" else ""} remaining",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                MMButton(
                    text = "OK",
                    onClick = onDismiss,
                    useGlass = false,
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}
