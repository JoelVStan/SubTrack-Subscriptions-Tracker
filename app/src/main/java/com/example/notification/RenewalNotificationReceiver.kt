package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RenewalNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val subs = db.subscriptionDao().getAllSubscriptions().first()
                for (sub in subs) {
                    if (sub.isAutoRenew) {
                        val days = sub.daysUntilDue()
                        val reminderDays = sub.reminderDaysList()
                        if (days.toInt() in reminderDays || days <= 1) {
                            NotificationHelper.notifyForSubscription(context, sub)
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore background errors
            } finally {
                pendingResult.finish()
            }
        }
    }
}
