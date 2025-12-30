// ProfileScreen.kt
@file:Suppress("FunctionName")

package com.mentorme.app.ui.profile

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mentorme.app.core.datastore.DataStoreManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentorme.app.data.session.SessionManager
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.notifications.NotificationsViewModel
import com.mentorme.app.ui.profile.components.ProfileTab
import com.mentorme.app.ui.profile.components.SettingsTab
import com.mentorme.app.ui.profile.components.StatsTab
import com.mentorme.app.ui.profile.components.WalletTab
import com.mentorme.app.ui.profile.components.WalletTabWithVm
import com.mentorme.app.ui.wallet.PaymentMethod
import com.mentorme.app.ui.wallet.WalletViewModel
import com.mentorme.app.ui.wallet.initialPaymentMethods
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/* =======================
   Screen
   ======================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    user: UserHeader,
    notificationsViewModel: NotificationsViewModel,
    walletViewModel: WalletViewModel,
    onOpenSettings: (() -> Unit)? = null,
    onOpenNotifications: (() -> Unit)? = null,
    onOpenTopUp: () -> Unit = {},
    onOpenWithdraw: () -> Unit = {},
    onOpenChangeMethod: () -> Unit = {},
    onAddMethod: () -> Unit = {},
    methods: List<PaymentMethod> = initialPaymentMethods(),
    onLogout: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ✅ FIX 1: Collect state từ ViewModel thay vì tạo local state
    val state by vm.state.collectAsState()

    val profile = state.profile
    val userRole = state.role ?: UserRole.MENTEE
    val isLoading = state.loading
    val isEditing = state.editing
    val edited = state.edited
    val notificationPreferences by notificationsViewModel.preferences.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // ✅ FIX 2: Xóa LaunchedEffect gọi api.getMe() - ViewModel đã tự load trong init{}

    Box(Modifier.fillMaxSize()) {
        com.mentorme.app.ui.theme.LiquidBackground(
            modifier = Modifier
                .matchParentSize()
                .zIndex(-1f)
        )

        // ✅ Toàn bộ nội dung dùng màu trắng mặc định
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Thông tin cá nhân",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Quản lý thông tin và cài đặt tài khoản",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            ) { padding ->

                // ✅ FIX 3: Hiển thị loading state
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                    return@Scaffold
                }

                // ✅ FIX 4: Hiển thị error state
                state.error?.let { errorMsg ->
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lỗi: $errorMsg", color = Color.White)
                            Spacer(Modifier.height(12.dp))
                            MMPrimaryButton(onClick = { vm.refresh() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                    return@Scaffold
                }

                // ✅ FIX 5: Kiểm tra profile null
                if (profile == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không có dữ liệu profile", color = Color.White)
                    }
                    return@Scaffold
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 24.dp)
                ) {
                    // Tabs
                    com.mentorme.app.ui.theme.LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        radius = 22.dp
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            indicator = { positions ->
                                if (positions.isNotEmpty()) {
                                    val pos = positions[selectedTab.coerceIn(0, positions.lastIndex)]
                                    Box(
                                        Modifier
                                            .tabIndicatorOffset(pos)
                                            .fillMaxHeight()
                                            .padding(6.dp)
                                            .border(
                                                BorderStroke(
                                                    2.dp,
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFF60A5FA), Color(0xFFA78BFA), Color(0xFFF472B6))
                                                    )
                                                ),
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                    )
                                }
                            },
                            divider = {}
                        ) {
                            listOf("Profile", "Ví", "Thống kê", "Cài đặt").forEachIndexed { i, label ->
                                Tab(
                                    selected = selectedTab == i,
                                    onClick = { selectedTab = i },
                                    selectedContentColor = Color.White,
                                    unselectedContentColor = Color.White.copy(alpha = .6f),
                                text = {
                                    Text(
                                        label,
                                        fontWeight = if (selectedTab == i) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    when (selectedTab) {
                        0 -> ProfileTab(
                            profile = profile,
                            isEditing = isEditing,
                            edited = edited ?: profile,
                            onEditToggle = { vm.toggleEdit() },
                            onChange = { newEdited -> vm.updateEdited { newEdited } },
                            onSave = {
                                vm.save { success, msg ->
                                    scope.launch {
                                        if (success) {
                                            snackbarHostState.showSnackbar("Cập nhật profile thành công!")
                                        } else {
                                            snackbarHostState.showSnackbar(msg ?: "Cập nhật thất bại")
                                        }
                                    }
                                }
                            },
                            onCancel = { vm.toggleEdit() }
                        )
                        1 -> WalletTabWithVm(
                            walletViewModel = walletViewModel,
                            onTopUp = onOpenTopUp,
                            onWithdraw = onOpenWithdraw,
                            onChangeMethod = onOpenChangeMethod,
                            onAddMethod = onAddMethod,
                        )
                        // ✅ FIX 6: Dùng userRole từ ViewModel state thay vì user.role
                        2 -> StatsTab(userRole = userRole, profile = profile)
                        3 -> SettingsTab(
                            onOpenSettings = onOpenSettings,
                            onOpenNotifications = onOpenNotifications,
                            notificationPreferences = notificationPreferences,
                            onUpdateNotificationPreferences = { notificationsViewModel.updatePreferences(it) },
                            onOpenCSBM = null,
                            onOpenDKSD = null,
                            onOpenLHHT = null,
                            onLogout = onLogout
                        )
                    }

                    // Spacer để nội dung không bị bottom bar đè khi scroll xuống
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

/* =======================
   Previews
   ======================= */

@Composable
fun ProfileScreenRoute(
    vm: ProfileViewModel = hiltViewModel(),
    user: UserHeader = UserHeader(
        fullName = "User Demo",
        email = "demo@example.com",
        role = UserRole.MENTEE
    ),
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel(),
    onOpenSettings: (() -> Unit)? = null,
    onOpenNotifications: (() -> Unit)? = null,
    onOpenTopUp: () -> Unit = {},
    onOpenWithdraw: () -> Unit = {},
    onOpenChangeMethod: () -> Unit = {},
    onAddMethod: () -> Unit = {},
    methods: List<PaymentMethod> = initialPaymentMethods(),
    onLogout: () -> Unit = {}
) {
    ProfileScreen(
        vm = vm,
        user = user,
        notificationsViewModel = notificationsViewModel,
        walletViewModel = walletViewModel,
        onOpenSettings = onOpenSettings,
        onOpenNotifications = onOpenNotifications,
        onOpenTopUp = onOpenTopUp,
        onOpenWithdraw = onOpenWithdraw,
        onOpenChangeMethod = onOpenChangeMethod,
        onAddMethod = onAddMethod,
        methods = methods,
        onLogout = onLogout
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun Preview_Mentee() {
    // Note: Preview without Hilt injection - requires manual mock
    MaterialTheme(colorScheme = lightColorScheme()) {
        // ProfileScreen requires ViewModel - preview is limited without Hilt
        Box(Modifier.fillMaxSize()) {
            Text("Preview requires Hilt setup")
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_Mentor_Dark() {
    // Note: Preview without Hilt injection - requires manual mock
    MaterialTheme(colorScheme = darkColorScheme()) {
        // ProfileScreen requires ViewModel - preview is limited without Hilt
        Box(Modifier.fillMaxSize()) {
            Text("Preview requires Hilt setup")
        }
    }
}


