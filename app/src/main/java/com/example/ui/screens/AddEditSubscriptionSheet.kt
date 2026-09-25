package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BillingCycle
import com.example.model.PresetService
import com.example.model.Subscription
import com.example.model.SubscriptionCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionSheet(
    subscription: Subscription?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (Subscription) -> Unit,
    onDelete: (Subscription) -> Unit
) {
    val context = LocalContext.current
    val isEditing = subscription != null

    var name by remember { mutableStateOf(subscription?.name ?: "") }
    var selectedCategory by remember {
        mutableStateOf(subscription?.category ?: SubscriptionCategory.ENTERTAINMENT.displayName)
    }
    var priceText by remember {
        mutableStateOf(
            if (subscription != null) {
                if (subscription.price % 1.0 == 0.0) String.format(Locale.US, "%.0f", subscription.price)
                else String.format(Locale.US, "%.2f", subscription.price)
            } else ""
        )
    }
    var selectedCycle by remember {
        mutableStateOf(subscription?.cycle ?: BillingCycle.MONTHLY)
    }
    var nextBillingDateMillis by remember {
        mutableStateOf(subscription?.nextBillingDate ?: run {
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 30)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
            }.timeInMillis
        })
    }
    var paymentMethod by remember {
        mutableStateOf(subscription?.paymentMethod ?: "Credit Card (••4242)")
    }
    var isAutoRenew by remember { mutableStateOf(subscription?.isAutoRenew ?: true) }
    var notes by remember { mutableStateOf(subscription?.notes ?: "") }
    var colorHex by remember { mutableStateOf(subscription?.colorHex ?: "#6366F1") }

    // Reminders selection
    val selectedReminders = remember {
        val list = mutableStateListOf<Int>()
        if (subscription != null) {
            list.addAll(subscription.reminderDaysList())
        } else {
            list.addAll(listOf(7, 3, 1, 0))
        }
        list
    }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Subscription" else "New Subscription",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_sheet_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Quick Autofill Presets (Show when adding new)
            if (!isEditing) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetService.presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    name = preset.name
                                    selectedCategory = preset.category.displayName
                                    priceText = if (preset.defaultPrice % 1.0 == 0.0) {
                                        String.format(Locale.US, "%.0f", preset.defaultPrice)
                                    } else {
                                        String.format(Locale.US, "%.2f", preset.defaultPrice)
                                    }
                                    selectedCycle = preset.billingCycle
                                    colorHex = preset.colorHex
                                    paymentMethod = preset.defaultPaymentMethod
                                    notes = preset.notes
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("preset_${preset.name}")
                        ) {
                            Text(
                                text = preset.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Service Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = { Text("Service Name *") },
                placeholder = { Text("e.g. Netflix, Spotify, ChatGPT") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subscription_name_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Price in INR Field
            OutlinedTextField(
                value = priceText,
                onValueChange = {
                    priceText = it
                    errorMessage = null
                },
                label = { Text("Price (INR ₹) *") },
                placeholder = { Text("499") },
                leadingIcon = {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subscription_price_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Billing Cycle Selector
            Text(
                text = "Billing Cycle",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BillingCycle.entries.forEach { cycle ->
                    FilterChip(
                        selected = selectedCycle == cycle,
                        onClick = { selectedCycle = cycle },
                        label = { Text(cycle.title) },
                        modifier = Modifier.testTag("cycle_${cycle.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubscriptionCategory.allCategories().forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.displayName,
                        onClick = { selectedCategory = cat.displayName },
                        label = { Text(cat.displayName) },
                        modifier = Modifier.testTag("category_chip_${cat.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next Billing Date Picker
            Text(
                text = "Next Billing Date",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val currentCal = Calendar.getInstance().apply { timeInMillis = nextBillingDateMillis }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val updatedCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                    set(Calendar.HOUR_OF_DAY, 9)
                                    set(Calendar.MINUTE, 0)
                                }
                                nextBillingDateMillis = updatedCal.timeInMillis
                            },
                            currentCal.get(Calendar.YEAR),
                            currentCal.get(Calendar.MONTH),
                            currentCal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .testTag("next_billing_date_picker"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = sdf.format(Date(nextBillingDateMillis)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Field & Presets
            OutlinedTextField(
                value = paymentMethod,
                onValueChange = { paymentMethod = it },
                label = { Text("Payment Method") },
                placeholder = { Text("e.g. Credit Card, PayPal, Apple Pay") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_method_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick payment method chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("UPI (Google Pay)", "UPI (PhonePe)", "Paytm", "Credit Card", "Debit Card", "NetBanking").forEach { method ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .clickable { paymentMethod = method }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = method,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-Renew Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Renew",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatically roll over payment on renewal date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isAutoRenew,
                        onCheckedChange = { isAutoRenew = it },
                        modifier = Modifier.testTag("auto_renew_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminders Section
            Text(
                text = "Renewal Reminders",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Notify me before upcoming subscription charges:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            listOf(
                7 to "7 days before renewal",
                3 to "3 days before renewal",
                1 to "1 day before renewal",
                0 to "On the renewal due date"
            ).forEach { (day, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedReminders.contains(day)) {
                                selectedReminders.remove(day)
                            } else {
                                selectedReminders.add(day)
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedReminders.contains(day),
                        onCheckedChange = {
                            if (it) selectedReminders.add(day) else selectedReminders.remove(day)
                        },
                        modifier = Modifier.testTag("reminder_checkbox_$day")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Accent Color Picker
            Text(
                text = "Card Accent Color",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val colorPalette = listOf(
                "#6366F1", "#EF4444", "#10B981", "#3B82F6",
                "#F59E0B", "#8B5CF6", "#EC4899", "#14B8A6",
                "#000000", "#D97706"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colorPalette.forEach { hex ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (_: Exception) {
                        Color.Gray
                    }
                    val isChosen = colorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { colorHex = hex }
                            .then(
                                if (isChosen) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChosen) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes Field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Family plan, cancellation link, account details...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subscription_notes_input"),
                shape = RoundedCornerShape(14.dp),
                minLines = 2
            )

            // Error Display
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter a service name"
                        return@Button
                    }
                    val priceVal = priceText.toDoubleOrNull()
                    if (priceVal == null || priceVal <= 0.0) {
                        errorMessage = "Please enter a valid price amount"
                        return@Button
                    }

                    val updatedSubscription = Subscription(
                        id = subscription?.id ?: 0L,
                        name = name.trim(),
                        category = selectedCategory,
                        price = priceVal,
                        currency = "INR",
                        billingCycle = selectedCycle.name,
                        nextBillingDate = nextBillingDateMillis,
                        paymentMethod = paymentMethod.trim(),
                        isAutoRenew = isAutoRenew,
                        notes = notes.trim(),
                        colorHex = colorHex,
                        reminderDaysBefore = selectedReminders.joinToString(","),
                        createdAt = subscription?.createdAt ?: System.currentTimeMillis()
                    )

                    onSave(updatedSubscription)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_subscription_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isEditing) "Save Changes" else "Add Subscription",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Delete Button (if editing)
            if (isEditing && subscription != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("delete_subscription_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Subscription", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirmDialog && subscription != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Subscription") },
            text = { Text("Are you sure you want to delete ${subscription.name}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(subscription)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
