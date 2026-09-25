package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrencyUtils
import com.example.model.Subscription
import com.example.ui.components.SubscriptionCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    allSubscriptions: List<Subscription>,
    calendarMonth: Calendar,
    selectedDateMillis: Long,
    onMonthChange: (Int) -> Unit,
    onDateSelect: (Long) -> Unit,
    onSubscriptionClick: (Subscription) -> Unit,
    onEditSubscription: (Subscription) -> Unit,
    onDeleteSubscription: (Subscription) -> Unit,
    onToggleAutoRenew: (Subscription, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitleSdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val selectedDaySdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    // Calculate days grid for the viewed month
    val year = calendarMonth.get(Calendar.YEAR)
    val month = calendarMonth.get(Calendar.MONTH)

    val tempCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, etc.
    val offset = firstDayOfWeek - 1

    // Group subscriptions by day of month if they fall in this month and year
    // Note: for recurring subscriptions, calculate occurrences in this month!
    val daySubscriptionsMap = mutableMapOf<Int, MutableList<Subscription>>()
    allSubscriptions.forEach { sub ->
        val subCal = Calendar.getInstance().apply { timeInMillis = sub.nextBillingDate }
        if (subCal.get(Calendar.YEAR) == year && subCal.get(Calendar.MONTH) == month) {
            val day = subCal.get(Calendar.DAY_OF_MONTH)
            daySubscriptionsMap.getOrPut(day) { mutableListOf() }.add(sub)
        }
    }

    // Selected day info
    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val isSelectedInCurrentView = selectedCal.get(Calendar.YEAR) == year && selectedCal.get(Calendar.MONTH) == month
    val selectedDay = if (isSelectedInCurrentView) selectedCal.get(Calendar.DAY_OF_MONTH) else null
    val selectedDaySubs = if (selectedDay != null) daySubscriptionsMap[selectedDay] ?: emptyList() else emptyList()
    val selectedDayTotal = selectedDaySubs.sumOf { it.price }

    // Month renewals total
    val monthTotalSpend = daySubscriptionsMap.values.flatten().sumOf { it.price }
    val monthTotalRenewals = daySubscriptionsMap.values.flatten().size

    // Today's day of month
    val todayCal = Calendar.getInstance()
    val isTodayInMonth = todayCal.get(Calendar.YEAR) == year && todayCal.get(Calendar.MONTH) == month
    val todayDay = if (isTodayInMonth) todayCal.get(Calendar.DAY_OF_MONTH) else -1

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Renewal Calendar",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "$monthTotalRenewals renewals scheduled in ${monthTitleSdf.format(tempCal.time)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick jump to Today
                IconButton(
                    onClick = {
                        val today = System.currentTimeMillis()
                        onDateSelect(today)
                        val nowCal = Calendar.getInstance()
                        val diffMonth = (nowCal.get(Calendar.YEAR) - year) * 12 + (nowCal.get(Calendar.MONTH) - month)
                        if (diffMonth != 0) {
                            onMonthChange(diffMonth)
                        }
                    },
                    modifier = Modifier.testTag("calendar_today_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Go to Today",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Calendar Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_view_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month navigation bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onMonthChange(-1) },
                            modifier = Modifier.testTag("calendar_prev_month")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                        }

                        Text(
                            text = monthTitleSdf.format(tempCal.time),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { onMonthChange(1) },
                            modifier = Modifier.testTag("calendar_next_month")
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Weekday headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Days Grid
                    val totalCells = ((offset + daysInMonth + 6) / 7) * 7
                    for (row in 0 until (totalCells / 7)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0..6) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - offset + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val isSelected = selectedDay == dayNumber
                                    val isToday = todayDay == dayNumber
                                    val subsForDay = daySubscriptionsMap[dayNumber] ?: emptyList()

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                    isToday -> MaterialTheme.colorScheme.surfaceVariant
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .then(
                                                if (isToday && !isSelected) {
                                                    Modifier.border(
                                                        1.5.dp,
                                                        MaterialTheme.colorScheme.primary,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                } else Modifier
                                            )
                                            .clickable {
                                                val chosenCal = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, year)
                                                    set(Calendar.MONTH, month)
                                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                                    set(Calendar.HOUR_OF_DAY, 9)
                                                    set(Calendar.MINUTE, 0)
                                                }
                                                onDateSelect(chosenCal.timeInMillis)
                                            }
                                            .testTag("calendar_day_$dayNumber"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNumber",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    isToday -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            // Dot indicators for renewals
                                            if (subsForDay.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    subsForDay.take(3).forEach { sub ->
                                                        val dotColor = try {
                                                            Color(android.graphics.Color.parseColor(sub.colorHex))
                                                        } catch (_: Exception) {
                                                            MaterialTheme.colorScheme.primary
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(dotColor)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Empty padding cell
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Month totals footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Projected for Month:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtils.formatINR(monthTotalSpend),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Selected Date Details Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Renewals on Selected Date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedDaySdf.format(selectedCal.time),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (selectedDaySubs.isNotEmpty()) {
                        Text(
                            text = CurrencyUtils.formatINR(selectedDayTotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (selectedDaySubs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No subscriptions are renewing on this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(selectedDaySubs, key = { it.id }) { sub ->
                SubscriptionCard(
                    subscription = sub,
                    onClick = { onSubscriptionClick(sub) },
                    onEdit = { onEditSubscription(sub) },
                    onDelete = { onDeleteSubscription(sub) },
                    onToggleAutoRenew = { onToggleAutoRenew(sub, it) }
                )
            }
        }
    }
}
