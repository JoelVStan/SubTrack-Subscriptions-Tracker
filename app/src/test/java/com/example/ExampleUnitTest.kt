package com.example

import com.example.model.BillingCycle
import com.example.model.DashboardStats
import com.example.model.Subscription
import com.example.model.SubscriptionCategory
import com.example.model.UrgencyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun testBillingCycleCalculations() {
        val monthly = Subscription(
            id = 1,
            name = "Netflix",
            category = "Video",
            price = 15.0,
            billingCycle = BillingCycle.MONTHLY.name,
            nextBillingDate = System.currentTimeMillis()
        )
        assertEquals(15.0, monthly.monthlyCost, 0.01)
        assertEquals(180.0, monthly.yearlyCost, 0.01)

        val yearly = Subscription(
            id = 2,
            name = "Amazon Prime",
            category = "Shopping",
            price = 120.0,
            billingCycle = BillingCycle.YEARLY.name,
            nextBillingDate = System.currentTimeMillis()
        )
        assertEquals(10.0, yearly.monthlyCost, 0.01)
        assertEquals(120.0, yearly.yearlyCost, 0.01)

        val weekly = Subscription(
            id = 3,
            name = "Fitness Pass",
            category = "Fitness",
            price = 10.0,
            billingCycle = BillingCycle.WEEKLY.name,
            nextBillingDate = System.currentTimeMillis()
        )
        // 10 * 52 / 12 = 43.333
        assertEquals(43.333, weekly.monthlyCost, 0.01)
    }

    @Test
    fun testUrgencyLevels() {
        val now = System.currentTimeMillis()

        fun dateInDays(days: Int): Long {
            return Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, days)
            }.timeInMillis
        }

        val overdueSub = Subscription(
            id = 1, name = "A", category = "Other", price = 10.0,
            nextBillingDate = dateInDays(-2)
        )
        assertEquals(UrgencyLevel.OVERDUE, overdueSub.urgency(now))

        val criticalSub = Subscription(
            id = 2, name = "B", category = "Other", price = 10.0,
            nextBillingDate = dateInDays(2)
        )
        assertEquals(UrgencyLevel.CRITICAL, criticalSub.urgency(now))

        val upcomingSub = Subscription(
            id = 3, name = "C", category = "Other", price = 10.0,
            nextBillingDate = dateInDays(6)
        )
        assertEquals(UrgencyLevel.UPCOMING, upcomingSub.urgency(now))

        val safeSub = Subscription(
            id = 4, name = "D", category = "Other", price = 10.0,
            nextBillingDate = dateInDays(15)
        )
        assertEquals(UrgencyLevel.SAFE, safeSub.urgency(now))
    }

    @Test
    fun testDashboardStatsComputation() {
        val now = System.currentTimeMillis()
        val subs = listOf(
            Subscription(id = 1, name = "Netflix", category = "Video", price = 20.0, billingCycle = BillingCycle.MONTHLY.name, nextBillingDate = now + 86400000L),
            Subscription(id = 2, name = "Spotify", category = "Music", price = 10.0, billingCycle = BillingCycle.MONTHLY.name, nextBillingDate = now + 172800000L),
            Subscription(id = 3, name = "Gym", category = "Fitness", price = 50.0, billingCycle = BillingCycle.MONTHLY.name, isAutoRenew = false, nextBillingDate = now + 864000000L)
        )

        val stats = DashboardStats.compute(subs, now)
        assertEquals(80.0, stats.totalMonthlySpend, 0.01)
        assertEquals(960.0, stats.totalYearlySpend, 0.01)
        assertEquals(2, stats.activeCount)
        assertEquals(3, stats.totalCount)
        assertEquals("Netflix", stats.nextRenewal?.name)
    }

    @Test
    fun testINRFormatting() {
        val sub = Subscription(
            id = 1,
            name = "Netflix",
            category = "Video",
            price = 649.0,
            billingCycle = BillingCycle.MONTHLY.name,
            nextBillingDate = System.currentTimeMillis()
        )
        val formatted = sub.formattedPrice()
        assertTrue(formatted.contains("₹") || formatted.contains("649"))
        assertEquals("INR", sub.currency)
    }

    @Test
    fun testPasswordHashingAndVerification() {
        val rawPassword = "securePassword123"
        val hash = com.example.util.SecurityUtils.hashPassword(rawPassword)
        assertTrue(hash.isNotEmpty())
        assertTrue(com.example.util.SecurityUtils.verifyPassword(rawPassword, hash))
        org.junit.Assert.assertFalse(com.example.util.SecurityUtils.verifyPassword("wrongPassword", hash))
    }
}
