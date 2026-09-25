package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AddEditSubscriptionSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthMode
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionsListScreen

enum class SubTrackTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    SUBSCRIPTIONS("Subs", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    ANALYTICS("Analytics", Icons.Filled.PieChart, Icons.Outlined.PieChart),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTrackApp(
    viewModel: SubTrackViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAuthChecking by viewModel.isAuthChecking.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val hasRegisteredUsers by viewModel.hasRegisteredUsers.collectAsState()

    // 1. Session checking splash
    if (isAuthChecking) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "SubTrack",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "SubTrack",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        return
    }

    // 2. Authentication Screen if user is not logged in
    val loggedInUser = currentUser
    if (loggedInUser == null) {
        AuthScreen(
            initialMode = if (hasRegisteredUsers) AuthMode.SIGN_IN else AuthMode.SIGN_UP,
            errorMessage = authError,
            isLoading = isAuthLoading,
            onSignIn = { email, password -> viewModel.signIn(email, password) },
            onSignUp = { name, email, password -> viewModel.signUp(name, email, password) },
            onClearError = { viewModel.clearAuthError() }
        )
        return
    }

    // 3. Main Application Flow once logged in
    var selectedTab by remember { mutableStateOf(SubTrackTab.DASHBOARD) }

    val allSubscriptions by viewModel.allSubscriptions.collectAsState()
    val filteredSubscriptions by viewModel.filteredSubscriptions.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedCycle by viewModel.selectedCycleFilter.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    val calendarMonth by viewModel.calendarMonthCalendar.collectAsState()
    val selectedCalendarDate by viewModel.selectedCalendarDateMillis.collectAsState()

    val isAddEditOpen by viewModel.isAddEditSheetOpen.collectAsState()
    val subscriptionToEdit by viewModel.subscriptionToEdit.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Back button behavior: if on another tab, return to Dashboard first
    BackHandler(enabled = selectedTab != SubTrackTab.DASHBOARD) {
        selectedTab = SubTrackTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                SubTrackTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                SubTrackTab.DASHBOARD -> {
                    DashboardScreen(
                        stats = stats,
                        allSubscriptions = allSubscriptions,
                        userName = loggedInUser.name,
                        onAddSubscription = { viewModel.openAddSubscription() },
                        onSubscriptionClick = { viewModel.openEditSubscription(it) },
                        onEditSubscription = { viewModel.openEditSubscription(it) },
                        onDeleteSubscription = { viewModel.deleteSubscription(it) },
                        onToggleAutoRenew = { sub, isAuto -> viewModel.toggleAutoRenew(sub, isAuto) },
                        onNavigateToList = { selectedTab = SubTrackTab.SUBSCRIPTIONS }
                    )
                }

                SubTrackTab.SUBSCRIPTIONS -> {
                    SubscriptionsListScreen(
                        subscriptions = filteredSubscriptions,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.searchQuery.value = it },
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.selectedCategoryFilter.value = it },
                        selectedCycle = selectedCycle,
                        onCycleSelect = { viewModel.selectedCycleFilter.value = it },
                        sortOrder = sortOrder,
                        onSortOrderChange = { viewModel.sortOrder.value = it },
                        onAddSubscription = { viewModel.openAddSubscription() },
                        onSubscriptionClick = { viewModel.openEditSubscription(it) },
                        onEditSubscription = { viewModel.openEditSubscription(it) },
                        onDeleteSubscription = { viewModel.deleteSubscription(it) },
                        onToggleAutoRenew = { sub, isAuto -> viewModel.toggleAutoRenew(sub, isAuto) },
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }

                SubTrackTab.CALENDAR -> {
                    CalendarScreen(
                        allSubscriptions = allSubscriptions,
                        calendarMonth = calendarMonth,
                        selectedDateMillis = selectedCalendarDate,
                        onMonthChange = { viewModel.changeCalendarMonth(it) },
                        onDateSelect = { viewModel.selectCalendarDate(it) },
                        onSubscriptionClick = { viewModel.openEditSubscription(it) },
                        onEditSubscription = { viewModel.openEditSubscription(it) },
                        onDeleteSubscription = { viewModel.deleteSubscription(it) },
                        onToggleAutoRenew = { sub, isAuto -> viewModel.toggleAutoRenew(sub, isAuto) }
                    )
                }

                SubTrackTab.ANALYTICS -> {
                    AnalyticsScreen(
                        stats = stats,
                        allSubscriptions = allSubscriptions
                    )
                }

                SubTrackTab.SETTINGS -> {
                    SettingsScreen(
                        currentUser = loggedInUser,
                        onLogout = { viewModel.logout() },
                        onResetSampleData = { viewModel.resetToSampleData() },
                        onSendTestAlert = { viewModel.sendTestAlert() }
                    )
                }
            }
        }

        // Add/Edit Bottom Sheet
        if (isAddEditOpen) {
            AddEditSubscriptionSheet(
                subscription = subscriptionToEdit,
                sheetState = sheetState,
                onDismiss = { viewModel.dismissAddEdit() },
                onSave = { viewModel.saveSubscription(it) },
                onDelete = { viewModel.deleteSubscription(it) }
            )
        }
    }
}
