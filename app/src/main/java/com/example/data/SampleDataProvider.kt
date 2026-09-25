package com.example.data

import com.example.model.BillingCycle
import com.example.model.Subscription
import com.example.model.SubscriptionCategory
import java.util.Calendar

object SampleDataProvider {

    fun getSampleSubscriptions(): List<Subscription> {
        val now = System.currentTimeMillis()

        fun dateInDays(days: Int): Long {
            return Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, days)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        return listOf(
            Subscription(
                name = "Netflix",
                category = SubscriptionCategory.VIDEO.displayName,
                price = 649.00,
                currency = "INR",
                billingCycle = BillingCycle.MONTHLY.name,
                nextBillingDate = dateInDays(2), // Red alert (urgent: 2 days)
                paymentMethod = "HDFC Credit Card (••4242)",
                isAutoRenew = true,
                notes = "4K UHD Premium plan, shared with family",
                colorHex = "#E50914"
            ),
            Subscription(
                name = "Spotify",
                category = SubscriptionCategory.MUSIC.displayName,
                price = 119.00,
                currency = "INR",
                billingCycle = BillingCycle.MONTHLY.name,
                nextBillingDate = dateInDays(5), // Orange alert (5 days)
                paymentMethod = "UPI (Google Pay)",
                isAutoRenew = true,
                notes = "Individual Premium plan with lossless audio & offline playlists",
                colorHex = "#1DB954"
            ),
            Subscription(
                name = "ChatGPT Plus",
                category = SubscriptionCategory.AI_TOOLS.displayName,
                price = 1999.00,
                currency = "INR",
                billingCycle = BillingCycle.MONTHLY.name,
                nextBillingDate = dateInDays(11), // Green (11 days)
                paymentMethod = "ICICI Credit Card",
                isAutoRenew = true,
                notes = "GPT-4o, Canvas, and Advanced Voice Mode",
                colorHex = "#10A37F"
            ),
            Subscription(
                name = "Google One",
                category = SubscriptionCategory.CLOUD_STORAGE.displayName,
                price = 130.00,
                currency = "INR",
                billingCycle = BillingCycle.MONTHLY.name,
                nextBillingDate = dateInDays(18), // Green (18 days)
                paymentMethod = "Google Pay UPI AutoPay",
                isAutoRenew = true,
                notes = "100 GB Google Drive storage & photo backup",
                colorHex = "#4285F4"
            ),
            Subscription(
                name = "Amazon Prime",
                category = SubscriptionCategory.SHOPPING.displayName,
                price = 1499.00,
                currency = "INR",
                billingCycle = BillingCycle.YEARLY.name,
                nextBillingDate = dateInDays(42), // Green (42 days)
                paymentMethod = "Amazon Pay UPI",
                isAutoRenew = true,
                notes = "Annual Prime membership: free one-day delivery & Prime Video",
                colorHex = "#FF9900"
            ),
            Subscription(
                name = "YouTube Premium",
                category = SubscriptionCategory.VIDEO.displayName,
                price = 129.00,
                currency = "INR",
                billingCycle = BillingCycle.MONTHLY.name,
                nextBillingDate = dateInDays(25),
                paymentMethod = "UPI AutoPay",
                isAutoRenew = true,
                notes = "Ad-free YouTube & YouTube Music included",
                colorHex = "#FF0000"
            ),
            Subscription(
                name = "GitHub Copilot",
                category = SubscriptionCategory.PRODUCTIVITY.displayName,
                price = 850.00,
                currency = "INR",
                billingCycle = BillingCycle.MONTHLY.name,
                nextBillingDate = dateInDays(8),
                paymentMethod = "SBI Debit Card",
                isAutoRenew = true,
                notes = "AI pair programmer in IDE",
                colorHex = "#6E40C9"
            )
        )
    }
}
