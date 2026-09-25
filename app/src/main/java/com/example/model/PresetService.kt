package com.example.model

data class PresetService(
    val name: String,
    val category: SubscriptionCategory,
    val defaultPrice: Double,
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val colorHex: String,
    val defaultPaymentMethod: String = "UPI (Google Pay)",
    val notes: String = ""
) {
    companion object {
        val presets = listOf(
            PresetService("Netflix", SubscriptionCategory.VIDEO, 649.00, BillingCycle.MONTHLY, "#E50914", "Credit Card (••4242)", "Premium 4K UHD Plan"),
            PresetService("Spotify", SubscriptionCategory.MUSIC, 119.00, BillingCycle.MONTHLY, "#1DB954", "UPI (Google Pay)", "Individual Premium"),
            PresetService("ChatGPT Plus", SubscriptionCategory.AI_TOOLS, 1999.00, BillingCycle.MONTHLY, "#10A37F", "Credit Card (••8819)", "GPT-4o & Advanced Voice"),
            PresetService("Google One", SubscriptionCategory.CLOUD_STORAGE, 130.00, BillingCycle.MONTHLY, "#4285F4", "Google Pay UPI", "100 GB Storage"),
            PresetService("Amazon Prime", SubscriptionCategory.SHOPPING, 1499.00, BillingCycle.YEARLY, "#FF9900", "Amazon Pay UPI", "Annual Prime membership & Video"),
            PresetService("YouTube Premium", SubscriptionCategory.VIDEO, 129.00, BillingCycle.MONTHLY, "#FF0000", "Google Pay UPI", "Ad-free YouTube & Music"),
            PresetService("GitHub Copilot", SubscriptionCategory.PRODUCTIVITY, 850.00, BillingCycle.MONTHLY, "#6E40C9", "Credit Card (••4242)", "Individual developer plan"),
            PresetService("Disney+ Hotstar", SubscriptionCategory.VIDEO, 899.00, BillingCycle.YEARLY, "#113CCF", "UPI / PhonePe", "Super Annual Plan"),
            PresetService("iCloud+", SubscriptionCategory.CLOUD_STORAGE, 75.00, BillingCycle.MONTHLY, "#0070C9", "Apple ID UPI", "50 GB iCloud Storage"),
            PresetService("Strava", SubscriptionCategory.FITNESS, 699.00, BillingCycle.MONTHLY, "#FC4C02", "UPI (Paytm)", "Summit Subscription"),
            PresetService("Duolingo Super", SubscriptionCategory.EDUCATION, 499.00, BillingCycle.MONTHLY, "#58CC02", "Google Pay UPI", "Ad-free learning"),
            PresetService("PlayStation Plus", SubscriptionCategory.GAMING, 3999.00, BillingCycle.YEARLY, "#003791", "Credit Card (••4242)", "Essential 12-month tier")
        )
    }
}
