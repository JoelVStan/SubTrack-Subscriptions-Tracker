package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuthRepository
import com.example.data.SessionManager
import com.example.data.SubscriptionRepository
import com.example.model.BillingCycle
import com.example.model.DashboardStats
import com.example.model.Subscription
import com.example.model.SubscriptionCategory
import com.example.model.User
import com.example.notification.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class SortOrder(val label: String) {
    RENEWAL_DATE("Next Renewal"),
    PRICE_HIGH_LOW("Price: High to Low"),
    PRICE_LOW_HIGH("Price: Low to High"),
    NAME_AZ("Name: A to Z")
}

@OptIn(ExperimentalCoroutinesApi::class)
class SubTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository(db.userDao(), sessionManager)
    private val repository = SubscriptionRepository(db.subscriptionDao())

    // Auth State
    val currentUser = MutableStateFlow<User?>(null)
    val isAuthChecking = MutableStateFlow(true)
    val authError = MutableStateFlow<String?>(null)
    val isAuthLoading = MutableStateFlow(false)
    val hasRegisteredUsers = MutableStateFlow(false)

    init {
        NotificationHelper.createNotificationChannel(application)
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            try {
                val hasUsers = authRepository.hasAnyRegisteredUser()
                hasRegisteredUsers.value = hasUsers

                if (authRepository.isUserLoggedIn()) {
                    val user = authRepository.getCurrentUser()
                    if (user != null) {
                        currentUser.value = user
                        repository.initDefaultDataForUserIfEmpty(user.email)
                    } else {
                        authRepository.logout()
                        currentUser.value = null
                    }
                } else {
                    currentUser.value = null
                }
            } catch (e: Exception) {
                authError.value = e.message
            } finally {
                isAuthChecking.value = false
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authError.value = null
            try {
                val result = authRepository.login(email, password)
                result.fold(
                    onSuccess = { user ->
                        currentUser.value = user
                        repository.initDefaultDataForUserIfEmpty(user.email)
                        hasRegisteredUsers.value = true
                    },
                    onFailure = { error ->
                        authError.value = error.message ?: "Authentication failed"
                    }
                )
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authError.value = null
            try {
                val result = authRepository.signUp(name, email, password)
                result.fold(
                    onSuccess = { user ->
                        currentUser.value = user
                        // Populate realistic starting data for new user
                        repository.resetWithSampleDataForUser(user.email)
                        hasRegisteredUsers.value = true
                    },
                    onFailure = { error ->
                        authError.value = error.message ?: "Sign up failed"
                    }
                )
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            currentUser.value = null
            dismissAddEdit()
            clearFilters()
            hasRegisteredUsers.value = authRepository.hasAnyRegisteredUser()
        }
    }

    fun clearAuthError() {
        authError.value = null
    }

    // Reactive subscriptions scoped to the currently logged in user
    val allSubscriptions: StateFlow<List<Subscription>> = currentUser
        .flatMapLatest { user ->
            if (user == null || user.email.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.getSubscriptionsForUser(user.email)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = allSubscriptions
        .combine(MutableStateFlow(System.currentTimeMillis())) { subs, _ ->
            DashboardStats.compute(subs)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Filters and Search for List Screen
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCycleFilter = MutableStateFlow<BillingCycle?>(null)
    val sortOrder = MutableStateFlow(SortOrder.RENEWAL_DATE)

    // Calendar state
    val calendarMonthCalendar = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    })
    val selectedCalendarDateMillis = MutableStateFlow(System.currentTimeMillis())

    // UI sheet state
    val isAddEditSheetOpen = MutableStateFlow(false)
    val subscriptionToEdit = MutableStateFlow<Subscription?>(null)

    val filteredSubscriptions: StateFlow<List<Subscription>> = combine(
        allSubscriptions,
        searchQuery,
        selectedCategoryFilter,
        selectedCycleFilter,
        sortOrder
    ) { subs, query, cat, cycle, sort ->
        var list = subs

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.paymentMethod.lowercase().contains(q)
            }
        }

        if (cat != null) {
            list = list.filter { it.category.equals(cat, ignoreCase = true) }
        }

        if (cycle != null) {
            list = list.filter { it.cycle == cycle }
        }

        when (sort) {
            SortOrder.RENEWAL_DATE -> list.sortedBy { it.nextBillingDate }
            SortOrder.PRICE_HIGH_LOW -> list.sortedByDescending { it.monthlyCost }
            SortOrder.PRICE_LOW_HIGH -> list.sortedBy { it.monthlyCost }
            SortOrder.NAME_AZ -> list.sortedBy { it.name.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun openAddSubscription() {
        subscriptionToEdit.value = null
        isAddEditSheetOpen.value = true
    }

    fun openEditSubscription(subscription: Subscription) {
        subscriptionToEdit.value = subscription
        isAddEditSheetOpen.value = true
    }

    fun dismissAddEdit() {
        isAddEditSheetOpen.value = false
        subscriptionToEdit.value = null
    }

    fun saveSubscription(subscription: Subscription) {
        viewModelScope.launch {
            val email = currentUser.value?.email ?: ""
            val userScopedSub = subscription.copy(userEmail = email)
            if (userScopedSub.id == 0L) {
                repository.insert(userScopedSub)
            } else {
                repository.update(userScopedSub)
            }
            dismissAddEdit()
        }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            repository.delete(subscription)
            if (subscriptionToEdit.value?.id == subscription.id) {
                dismissAddEdit()
            }
        }
    }

    fun toggleAutoRenew(subscription: Subscription, isAutoRenew: Boolean) {
        viewModelScope.launch {
            repository.update(subscription.copy(isAutoRenew = isAutoRenew))
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            val email = currentUser.value?.email ?: ""
            if (email.isNotBlank()) {
                repository.resetWithSampleDataForUser(email)
            }
        }
    }

    fun clearFilters() {
        searchQuery.value = ""
        selectedCategoryFilter.value = null
        selectedCycleFilter.value = null
        sortOrder.value = SortOrder.RENEWAL_DATE
    }

    fun changeCalendarMonth(delta: Int) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = calendarMonthCalendar.value.timeInMillis
            add(Calendar.MONTH, delta)
        }
        calendarMonthCalendar.value = cal
    }

    fun selectCalendarDate(epochMillis: Long) {
        selectedCalendarDateMillis.value = epochMillis
    }

    fun sendTestAlert() {
        NotificationHelper.sendTestNotification(getApplication())
    }
}
