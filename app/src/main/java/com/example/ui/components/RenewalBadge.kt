package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Subscription
import com.example.model.UrgencyLevel
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed

@Composable
fun RenewalBadge(
    subscription: Subscription,
    modifier: Modifier = Modifier
) {
    val days = subscription.daysUntilDue()
    val urgency = subscription.urgency()

    val (badgeBg, badgeTextColor, text) = when (urgency) {
        UrgencyLevel.OVERDUE -> Triple(
            AlertRed.copy(alpha = 0.16f),
            AlertRed,
            "${-days}d overdue"
        )
        UrgencyLevel.TODAY -> Triple(
            AlertRed.copy(alpha = 0.16f),
            AlertRed,
            "Today"
        )
        UrgencyLevel.CRITICAL -> Triple(
            AlertRed.copy(alpha = 0.16f),
            AlertRed,
            if (days == 1L) "Tomorrow" else "In $days days"
        )
        UrgencyLevel.UPCOMING -> Triple(
            AlertOrange.copy(alpha = 0.16f),
            AlertOrange,
            "In $days days"
        )
        UrgencyLevel.SAFE -> Triple(
            AlertGreen.copy(alpha = 0.16f),
            AlertGreen,
            "In $days days"
        )
    }

    Box(
        modifier = modifier
            .background(badgeBg, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = badgeTextColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
