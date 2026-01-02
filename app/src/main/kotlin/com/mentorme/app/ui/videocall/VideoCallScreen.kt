package com.mentorme.app.ui.videocall

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.projection.MediaProjectionManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.automirrored.filled.StopScreenShare
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularAlt1Bar
import androidx.compose.material.icons.filled.SignalCellularAlt2Bar
import androidx.compose.material.icons.filled.StopScreenShare
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.style.TextAlign
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.launch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val activity = context as? Activity

    // PiP state
    var isInPipMode by remember { mutableStateOf(false) }
    // Track if user intentionally ended the call (to prevent PiP on exit)
    var userEndedCall by remember { mutableStateOf(false) }

    // Lock orientation to portrait during call
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        onDispose {
            activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Enter PiP when app goes to background during active call
    DisposableEffect(lifecycleOwner, state.phase) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.onAppGoesToBackground()
                    // Enter PiP mode when going to background during call
                    // Don't enter PiP if user intentionally ended the call or if reconnecting
                    val shouldEnterPip = state.phase == CallPhase.InCall && 
                        !userEndedCall && 
                        state.peerConnectionStatus != "reconnecting" &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    if (shouldEnterPip) {
                        activity?.let { act ->
                            if (act.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                                try {
                                    val params = PictureInPictureParams.Builder()
                                        .setAspectRatio(Rational(9, 16)) // Portrait aspect ratio
                                        .build()
                                    act.enterPictureInPictureMode(params)
                                    isInPipMode = true
                                } catch (e: Exception) {
                                    // PiP not supported or failed
                                }
                            }
                        }
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.onAppComesToForeground()
                    isInPipMode = false
                }
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

    // Screen capture permission launcher
    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.startScreenSharing(result.data!!)
        }
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
            viewModel.releaseRenderers()
        }
    }

    // Auto-hide controls after 5 seconds during call
    var controlsVisible by remember { mutableStateOf(true) }
    var lastTapTime by remember { mutableStateOf(0L) }

    // Local preview animated position using Animatable for smooth animations
    val coroutineScope = rememberCoroutineScope()
    val localPreviewOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    // Store screen dimensions for drag constraints
    var screenWidthPx by remember { mutableStateOf(0f) }
    var screenHeightPx by remember { mutableStateOf(0f) }
    // Preview dimensions (3:4 portrait aspect ratio)
    var previewWidthPx by remember { mutableStateOf(0f) }
    var previewHeightPx by remember { mutableStateOf(0f) }
    var marginPx by remember { mutableStateOf(0f) }

    // Initialize position at top-right corner and store screen dimensions
    LaunchedEffect(Unit) {
        val resources = context.resources
        val displayMetrics = resources.displayMetrics
        screenWidthPx = displayMetrics.widthPixels.toFloat()
        screenHeightPx = displayMetrics.heightPixels.toFloat()
        // Preview size: 110dp width x 150dp height (larger, ~3:4 portrait ratio)
        previewWidthPx = 110.dp.value * displayMetrics.density
        previewHeightPx = 150.dp.value * displayMetrics.density
        marginPx = 12.dp.value * displayMetrics.density
        // Position at top-right with margin
        val initialX = screenWidthPx - previewWidthPx - marginPx
        val initialY = marginPx
        localPreviewOffset.snapTo(Offset(initialX, initialY))
    }

    // Auto-hide controls when in call
    LaunchedEffect(state.phase, controlsVisible) {
        if (state.phase == CallPhase.InCall && controlsVisible) {
            kotlinx.coroutines.delay(5000) // Hide after 5 seconds
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Toggle controls visibility on tap during call
                if (state.phase == CallPhase.InCall) {
                    controlsVisible = !controlsVisible
                    lastTapTime = System.currentTimeMillis()
                }
            }
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
                    // Initialize renderer on creation - use postDelayed to ensure surface is ready
                    postDelayed({
                        android.util.Log.d("VideoCallScreen", "Binding remote renderer")
                        viewModel.bindRemoteRenderer(this)
                    }, 100)
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
        
        // ✅ Gradient Scrim at bottom - để nút control nổi bật
        if (!isInPipMode && (controlsVisible || state.phase != CallPhase.InCall)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
        }

        // ✅ Gradient Scrim at top - để top bar rõ ràng
        if (!isInPipMode && (controlsVisible || state.phase != CallPhase.InCall)) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Peer camera off overlay - show when peer has video disabled
        AnimatedVisibility(
            visible = state.phase == CallPhase.InCall && !state.peerVideoEnabled && !isInPipMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A2E)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2D2D44)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Peer avatar",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Text(
                        text = state.peerName ?: "Participant",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Camera is off",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
        // Local video - small draggable preview with improvements (rendered on top)
        Box(
            modifier = Modifier
                .then(
                    if (isInPipMode) Modifier.size(0.dp) 
                    else Modifier.size(width = 110.dp, height = 150.dp) // Larger 3:4 portrait ratio
                )
                .offset { IntOffset(localPreviewOffset.value.x.toInt(), localPreviewOffset.value.y.toInt()) }
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = if (!state.isVideoEnabled) 2.dp else 1.dp,
                    color = if (!state.isVideoEnabled) Color(0xFFFF6B6B) else Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                )
                .background(Color(0xFF1A1A2E))
                .pointerInput(screenWidthPx, screenHeightPx, previewWidthPx, previewHeightPx, marginPx) {
                    detectDragGestures(
                        onDragEnd = {
                            // Snap to nearest corner with smooth spring animation
                            val maxX = screenWidthPx - previewWidthPx - marginPx
                            val maxY = screenHeightPx - previewHeightPx - marginPx
                            val centerX = screenWidthPx / 2
                            val centerY = screenHeightPx / 2
                            
                            val currentOffset = localPreviewOffset.value
                            // Determine which corner is nearest (use width for X, height for Y)
                            val snapX = if (currentOffset.x + previewWidthPx / 2 < centerX) marginPx else maxX
                            val snapY = if (currentOffset.y + previewHeightPx / 2 < centerY) marginPx else maxY
                            
                            coroutineScope.launch {
                                localPreviewOffset.animateTo(
                                    targetValue = Offset(snapX, snapY),
                                    animationSpec = spring(
                                        dampingRatio = 0.7f,
                                        stiffness = 400f
                                    )
                                )
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentOffset = localPreviewOffset.value
                            val newX = currentOffset.x + dragAmount.x
                            val newY = currentOffset.y + dragAmount.y

                            // Constrain to screen bounds using stored screen dimensions
                            val maxX = screenWidthPx - previewWidthPx
                            val maxY = screenHeightPx - previewHeightPx
                            val constrainedX = newX.coerceIn(0f, maxX)
                            val constrainedY = newY.coerceIn(0f, maxY)
                            
                            coroutineScope.launch {
                                localPreviewOffset.snapTo(Offset(constrainedX, constrainedY))
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val cornerRadiusDp = 20.dp
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        setZOrderMediaOverlay(true)
                        // Note: Don't use setZOrderOnTop(true) as it prevents clipping
                        // Apply rounded corners to SurfaceViewRenderer
                        clipToOutline = true
                        outlineProvider = object : android.view.ViewOutlineProvider() {
                            override fun getOutline(view: android.view.View, outline: android.graphics.Outline) {
                                val cornerRadiusPx = (cornerRadiusDp.value * resources.displayMetrics.density)
                                outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx)
                            }
                        }
                        // Initialize renderer on creation - use postDelayed to ensure surface is ready
                        postDelayed({
                            android.util.Log.d("VideoCallScreen", "Binding local renderer")
                            viewModel.bindLocalRenderer(this)
                        }, 100)
                    }
                },
                update = { view ->
                    // Ensure local preview corners stay rounded
                    view.setZOrderMediaOverlay(true)
                    view.clipToOutline = true
                    // Re-apply outline provider in case view size changed
                    if (view.outlineProvider == null || view.outlineProvider == android.view.ViewOutlineProvider.BACKGROUND) {
                        view.outlineProvider = object : android.view.ViewOutlineProvider() {
                            override fun getOutline(v: android.view.View, outline: android.graphics.Outline) {
                                val cornerRadiusPx = (cornerRadiusDp.value * view.resources.displayMetrics.density)
                                outline.setRoundRect(0, 0, v.width, v.height, cornerRadiusPx)
                            }
                        }
                    }
                    view.invalidateOutline()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadiusDp))
            )
            
            // Camera off indicator overlay
            if (!state.isVideoEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1A1A2E).copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideocamOff,
                        contentDescription = "Camera off",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(40.dp) // Larger icon for bigger preview
                    )
                }
            }
        }

        // Status label and timer for top bar
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

        // ✅ Top bar with safe area - gom status + timer + network vào 1 hàng
        AnimatedVisibility(
            visible = !isInPipMode && (state.phase != CallPhase.InCall || controlsVisible),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back button (chỉ hiển thị khi không InCall)
                if (state.phase != CallPhase.InCall) {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack
                    )
                } else {
                    Spacer(Modifier.width(44.dp)) // Placeholder để giữ symmetry
                }

                // Center: Status + Timer + Network (gom vào 1 pill)
                if (statusLabel.isNotBlank()) {
                    Surface(
                        modifier = Modifier.liquidGlassStrong(radius = 20.dp, alpha = 0.3f),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Network indicator
                            if (state.phase == CallPhase.InCall) {
                                val networkIcon = when (state.networkQuality) {
                                    NetworkQuality.Excellent, NetworkQuality.Good -> Icons.Default.SignalCellularAlt
                                    NetworkQuality.Fair -> Icons.Default.SignalCellularAlt2Bar
                                    NetworkQuality.Poor, NetworkQuality.VeryPoor -> Icons.Default.SignalCellularAlt1Bar
                                    NetworkQuality.Unknown -> null
                                }
                                val networkColor = when (state.networkQuality) {
                                    NetworkQuality.Excellent -> Color.Green
                                    NetworkQuality.Good -> Color(0xFF90EE90)
                                    NetworkQuality.Fair -> Color.Yellow
                                    NetworkQuality.Poor -> Color(0xFFFF8C00)
                                    NetworkQuality.VeryPoor -> Color.Red
                                    NetworkQuality.Unknown -> Color.Gray
                                }
                                networkIcon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = "Network",
                                        tint = networkColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Status text
                            Text(
                                statusLabel,
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )

                            // Timer
                            if (state.phase == CallPhase.InCall && timerText.isNotBlank()) {
                                Text(
                                    "•",
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    timerText,
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.width(1.dp)) // Placeholder
                }

                // Right: Secondary controls (Chat, Screen Share, Effects, Camera Switch)
                if (state.phase == CallPhase.InCall) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Chat with badge
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .liquidGlassStrong(radius = 22.dp, alpha = 0.26f),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleChatPanel() },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "Chat",
                                        tint = if (state.isChatPanelOpen) Color(0xFF059669) else Color.White
                                    )
                                }
                            }
                            if (state.unreadChatCount > 0 && !state.isChatPanelOpen) {
                                Badge(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                ) {
                                    Text(
                                        if (state.unreadChatCount > 9) "9+" else state.unreadChatCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        // More menu (3 dots) - chứa các tính năng phụ
                        var showMoreMenu by remember { mutableStateOf(false) }
                        Box {
                            GlassIconButton(
                                icon = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                onClick = { showMoreMenu = !showMoreMenu }
                            )

                            // Dropdown menu for secondary features
                            androidx.compose.material3.DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                modifier = Modifier.background(Color(0xFF1A1A2E).copy(alpha = 0.95f))
                            ) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Switch Camera", color = Color.White) },
                                    onClick = {
                                        viewModel.switchCamera()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Cameraswitch, null, tint = Color.White)
                                    }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (state.isScreenSharing) "Stop Sharing" else "Share Screen",
                                            color = Color.White
                                        )
                                    },
                                    onClick = {
                                        if (state.isScreenSharing) {
                                            viewModel.stopScreenSharing()
                                        } else {
                                            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                                            screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                                        }
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (state.isScreenSharing) Icons.AutoMirrored.Filled.StopScreenShare else Icons.AutoMirrored.Filled.ScreenShare,
                                            null,
                                            tint = if (state.isScreenSharing) Color(0xFF2563EB) else Color.White
                                        )
                                    }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (state.isBackgroundBlurEnabled) "Blur: On" else "Blur: Off",
                                            color = Color.White
                                        )
                                    },
                                    onClick = {
                                        viewModel.toggleBackgroundBlur()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (state.isBackgroundBlurEnabled) Icons.Default.BlurOn else Icons.Default.BlurOff,
                                            null,
                                            tint = if (state.isBackgroundBlurEnabled) Color(0xFF7C3AED) else Color.White
                                        )
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.width(44.dp)) // Placeholder
                }
            }
        }

        // Peer connection status banner - hide in PiP mode
        if (!isInPipMode && state.phase == CallPhase.InCall && state.peerConnectionStatus != null) {
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
                CallPhase.Reconnecting -> null // Handled separately with ReconnectingOverlay
                CallPhase.Ended -> "Call ended"
                CallPhase.Error -> state.errorMessage ?: "Call error"
                else -> null
            }
            
            // Show loading for joining/waiting/connecting phases
            val showLoading = state.phase in listOf(
                CallPhase.Joining, 
                CallPhase.WaitingForPeer, 
                CallPhase.Connecting
            ) || (state.phase == CallPhase.WaitingForAdmit && state.role != "mentor")

            if (statusText != null && state.phase != CallPhase.InCall) {
                CallOverlay(
                    title = statusText,
                    actionLabel = if (state.phase == CallPhase.Error) "Retry" else null,
                    onAction = {
                        if (state.phase == CallPhase.Error) {
                            viewModel.retry()
                        }
                    },
                    showLoading = showLoading
                )
            }
            
            // Reconnecting overlay with better animation
            if (state.phase == CallPhase.Reconnecting) {
                ReconnectingOverlay(
                    reconnectAttempt = state.reconnectAttempt,
                    maxReconnectAttempts = state.maxReconnectAttempts
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

            // Time warning dialog - hide in PiP mode
            if (!isInPipMode) {
                state.timeWarningMessage?.let { message ->
                    if (state.showTimeWarning) {
                        TimeWarningDialog(
                            message = message,
                            remainingMinutes = state.remainingMinutes,
                            onDismiss = { viewModel.dismissTimeWarning() }
                        )
                    }
                }
            }

            // Auto-hide controls during InCall - tap screen to toggle visibility (hide in PiP)
            AnimatedVisibility(
                visible = !isInPipMode && (controlsVisible || state.phase != CallPhase.InCall),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                CallControls(
                    state = state,
                    modifier = Modifier,
                    onToggleAudio = { viewModel.toggleAudio() },
                    onToggleVideo = { viewModel.toggleVideo() },
                    onToggleSpeaker = { viewModel.toggleSpeaker() },
                    onSwitchCamera = { viewModel.switchCamera() },
                    onToggleScreenShare = {
                        if (state.isScreenSharing) {
                            viewModel.stopScreenSharing()
                        } else {
                            // Request screen capture permission
                            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                            screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                        }
                    },
                    onToggleBackgroundBlur = { viewModel.toggleBackgroundBlur() },
                    onToggleChat = { viewModel.toggleChatPanel() },
                    onEndCall = { userEndedCall = true; viewModel.endCall(); onBack() }
                )
            }

            // In-call chat panel (slide from right)
            AnimatedVisibility(
                visible = !isInPipMode && state.isChatPanelOpen && state.phase == CallPhase.InCall,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                InCallChatPanel(
                    messages = state.chatMessages,
                    onSendMessage = { viewModel.sendChatMessage(it) },
                    onClose = { viewModel.toggleChatPanel() },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(end = 8.dp, top = 60.dp, bottom = 100.dp)
                )
            }

            // Hint to show controls when hidden (hide in PiP)
            AnimatedVisibility(
                visible = !isInPipMode && !controlsVisible && state.phase == CallPhase.InCall,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            ) {
                Text(
                    text = "Tap to show controls",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
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
    onToggleScreenShare: () -> Unit,
    onToggleBackgroundBlur: () -> Unit,
    onToggleChat: () -> Unit,
    onEndCall: () -> Unit
) {
    // ✅ Single control bar layout - kiểu Zalo
    Column(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ✅ Main control bar - hình viên thuốc, chứa 4 nút chính
        Surface(
            modifier = Modifier.liquidGlassStrong(radius = 32.dp, alpha = 0.3f),
            color = Color.Black.copy(alpha = 0.4f),
            shape = RoundedCornerShape(32.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic
                ControlButton(
                    icon = if (state.isAudioEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Toggle mic",
                    onClick = onToggleAudio,
                    background = if (!state.isAudioEnabled) Color(0xFFDC2626) else null,
                    size = 52.dp
                )

                // Camera
                ControlButton(
                    icon = if (state.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    contentDescription = "Toggle camera",
                    onClick = onToggleVideo,
                    background = if (!state.isVideoEnabled) Color(0xFFDC2626) else null,
                    size = 52.dp
                )

                // Speaker
                ControlButton(
                    icon = if (state.isSpeakerEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PhoneInTalk,
                    contentDescription = if (state.isSpeakerEnabled) "Speaker" else "Earpiece",
                    onClick = onToggleSpeaker,
                    size = 52.dp
                )
            }
        }

        // ✅ End Call button - TO, ĐỎ, NỔIỆT BẬT
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFDC2626))
                .shadow(8.dp, CircleShape)
                .clickable(onClick = onEndCall),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CallEnd,
                contentDescription = "End call",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun CallOverlay(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    showLoading: Boolean = false
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Loading animation (pulsing dots)
                if (showLoading) {
                    LoadingDots()
                    Spacer(Modifier.height(4.dp))
                }
                
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                
                if (!actionLabel.isNullOrBlank() && onAction != null) {
                    Spacer(Modifier.height(4.dp))
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
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingDots")
    
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = index * 200),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun ReconnectingOverlay(
    reconnectAttempt: Int,
    maxReconnectAttempts: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Spinning circle animation
            val infiniteTransition = rememberInfiniteTransition(label = "reconnecting")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = androidx.compose.animation.core.LinearEasing)
                ),
                label = "rotation"
            )
            
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer { rotationZ = rotation }
                        .clip(CircleShape)
                        .border(
                            width = 4.dp,
                            brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.White,
                                    Color.White
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
            
            Text(
                text = "Reconnecting...",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            
            if (reconnectAttempt > 0) {
                Text(
                    text = "Attempt $reconnectAttempt of $maxReconnectAttempts",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Progress indicator
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = reconnectAttempt.toFloat() / maxReconnectAttempts)
                        .fillMaxSize()
                        .background(Color(0xFF3B82F6))
                )
            }
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    background: Color? = null,
    useGlass: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 52.dp
) {
    val hasBackground = background != null
    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .then(
            when {
                hasBackground -> Modifier.background(background!!)
                useGlass -> Modifier.liquidGlassStrong(radius = size / 2, alpha = 0.25f)
                else -> Modifier
            }
        )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(size)) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(size * 0.46f)
            )
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

// ============== IN-CALL CHAT COMPONENTS ==============

@Composable
private fun ChatControlButton(
    unreadCount: Int,
    isChatOpen: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 52.dp
) {
    Box {
        ControlButton(
            icon = Icons.Default.Chat,
            contentDescription = "Toggle chat",
            onClick = onClick,
            background = if (isChatOpen) Color(0xFF059669) else null,
            size = size
        )

        // Badge for unread messages
        if (unreadCount > 0 && !isChatOpen) {
            Badge(
                containerColor = Color(0xFFDC2626),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Text(
                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun InCallChatPanel(
    messages: List<InCallChatMessage>,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = modifier
            .liquidGlassStrong(radius = 16.dp, alpha = 0.85f),
        color = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close chat",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No messages yet",
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(messages) { message ->
                        ChatMessageBubble(message = message)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text("Type a message...", color = Color.White.copy(alpha = 0.5f))
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) Color(0xFF2563EB) else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: InCallChatMessage) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.isFromMe) Color(0xFF2563EB) else Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 16.dp
            ),
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .widthIn(max = 250.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!message.isFromMe) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )

                Text(
                    text = timeFormat.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
