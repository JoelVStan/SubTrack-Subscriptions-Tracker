package com.example.model

import androidx.compose.ui.graphics.Color

enum class SubscriptionCategory(
    val displayName: String,
    val hexColor: String,
    val defaultColor: Color
) {
    VIDEO("Video", "#EF4444", Color(0xFFEF4444)),
    MUSIC("Music", "#10B981", Color(0xFF10B981)),
    ENTERTAINMENT("Entertainment", "#8B5CF6", Color(0xFF8B5CF6)),
    AI_TOOLS("AI Tools", "#06B6D4", Color(0xFF06B6D4)),
    CLOUD_STORAGE("Cloud Storage", "#3B82F6", Color(0xFF3B82F6)),
    PRODUCTIVITY("Productivity", "#6366F1", Color(0xFF6366F1)),
    GAMING("Gaming", "#EC4899", Color(0xFFEC4899)),
    FITNESS("Fitness", "#F97316", Color(0xFFF97316)),
    EDUCATION("Education", "#EAB308", Color(0xFFEAB308)),
    SHOPPING("Shopping", "#F59E0B", Color(0xFFF59E0B)),
    FINANCE("Finance", "#14B8A6", Color(0xFF14B8A6)),
    OTHER("Other", "#64748B", Color(0xFF64748B));

    companion object {
        fun fromName(name: String): SubscriptionCategory {
            return entries.firstOrNull { 
                it.displayName.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true) 
            } ?: OTHER
        }

        fun allCategories(): List<SubscriptionCategory> = entries.toList()
    }
}
