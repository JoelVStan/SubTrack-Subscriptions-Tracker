package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class UrgencyLevel(val label: String, val hexColor: String) {
    OVERDUE("Overdue", "#EF4444"),
    TODAY("Renews Today", "#EF4444"),
    CRITICAL("1-3 Days", "#EF4444"),
    UPCOMING("Within a Week", "#F59E0B"),
    SAFE("Upcoming", "#10B981")
}

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String = "",
    val name: String,
    val category: String,
    val price: Double,
    val currency: String = "INR",
    val billingCycle: String = BillingCycle.MONTHLY.name,
    val nextBillingDate: Long,
    val paymentMethod: String = "Credit Card",
    val isAutoRenew: Boolean = true,
    val notes: String = "",
    val colorHex: String = "#6366F1",
    val iconName: String = "default",
    val reminderDaysBefore: String = "7,3,1,0",
    val createdAt: Long = System.currentTimeMillis()
) {
    val cycle: BillingCycle
        get() = BillingCycle.fromString(billingCycle)

    val monthlyCost: Double
        get() = price * cycle.monthlyMultiplier

    val yearlyCost: Double
        get() = monthlyCost * 12.0

    fun daysUntilDue(currentMillis: Long = System.currentTimeMillis()): Long {
        val startOfToday = Calendar.getInstance().apply {
            timeInMillis = currentMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfDueDate = Calendar.getInstance().apply {
            timeInMillis = nextBillingDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val diff = startOfDueDate - startOfToday
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun urgency(currentMillis: Long = System.currentTimeMillis()): UrgencyLevel {
        val days = daysUntilDue(currentMillis)
        return when {
            days < 0 -> UrgencyLevel.OVERDUE
            days == 0L -> UrgencyLevel.TODAY
            days <= 3L -> UrgencyLevel.CRITICAL
            days <= 7L -> UrgencyLevel.UPCOMING
            else -> UrgencyLevel.SAFE
        }
    }

    fun formattedPrice(): String {
        return CurrencyUtils.formatINR(price)
    }

    fun formattedMonthlyCost(): String {
        return CurrencyUtils.formatINR(monthlyCost)
    }

    fun formattedNextBillingDate(): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(nextBillingDate))
    }

    fun reminderDaysList(): List<Int> {
        return reminderDaysBefore.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }
}
