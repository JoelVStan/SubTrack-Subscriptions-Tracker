package com.example.model

data class DashboardStats(
    val totalMonthlySpend: Double = 0.0,
    val totalYearlySpend: Double = 0.0,
    val activeCount: Int = 0,
    val totalCount: Int = 0,
    val nextRenewal: Subscription? = null,
    val urgentRenewals: List<Subscription> = emptyList(),
    val categorySpendMap: Map<String, Double> = emptyMap(),
    val monthlySpendAverage: Double = 0.0
) {
    companion object {
        fun compute(subscriptions: List<Subscription>, currentMillis: Long = System.currentTimeMillis()): DashboardStats {
            if (subscriptions.isEmpty()) {
                return DashboardStats()
            }

            val activeSubs = subscriptions.filter { it.isAutoRenew }
            val monthlyTotal = subscriptions.sumOf { it.monthlyCost }
            val yearlyTotal = monthlyTotal * 12.0

            // Sorted by next billing date
            val futureOrCurrent = subscriptions
                .filter { it.daysUntilDue(currentMillis) >= 0 }
                .sortedBy { it.nextBillingDate }

            val next = futureOrCurrent.firstOrNull() ?: subscriptions.minByOrNull { it.nextBillingDate }

            val urgent = subscriptions
                .filter { it.daysUntilDue(currentMillis) <= 7 }
                .sortedBy { it.nextBillingDate }

            val categoryMap = subscriptions
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.monthlyCost } }

            return DashboardStats(
                totalMonthlySpend = monthlyTotal,
                totalYearlySpend = yearlyTotal,
                activeCount = activeSubs.size,
                totalCount = subscriptions.size,
                nextRenewal = next,
                urgentRenewals = urgent,
                categorySpendMap = categoryMap,
                monthlySpendAverage = if (subscriptions.isNotEmpty()) monthlyTotal / subscriptions.size else 0.0
            )
        }
    }
}
