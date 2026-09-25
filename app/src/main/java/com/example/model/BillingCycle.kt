package com.example.model

import java.util.Calendar

enum class BillingCycle(
    val title: String,
    val shortLabel: String,
    val monthlyMultiplier: Double
) {
    WEEKLY("Weekly", "/wk", 52.0 / 12.0),
    MONTHLY("Monthly", "/mo", 1.0),
    QUARTERLY("Quarterly", "/qtr", 1.0 / 3.0),
    YEARLY("Yearly", "/yr", 1.0 / 12.0);

    fun calculateNextDate(currentEpochMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = currentEpochMillis
        }
        when (this) {
            WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 7)
            MONTHLY -> cal.add(Calendar.MONTH, 1)
            QUARTERLY -> cal.add(Calendar.MONTH, 3)
            YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }

    companion object {
        fun fromString(value: String): BillingCycle {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || it.title.equals(value, ignoreCase = true) 
            } ?: MONTHLY
        }
    }
}
