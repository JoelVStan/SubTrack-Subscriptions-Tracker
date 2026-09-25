package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.Subscription
import com.example.model.UrgencyLevel

object NotificationHelper {

    const val CHANNEL_ID = "subscription_renewals_channel"
    private const val CHANNEL_NAME = "Subscription Renewals"
    private const val CHANNEL_DESC = "Notifications for upcoming subscription renewals and payments"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendRenewalNotification(
        context: Context,
        subscriptionId: Long,
        title: String,
        message: String,
        isUrgent: Boolean = false
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            subscriptionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isUrgent) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(subscriptionId.toInt(), builder.build())
            }
        } catch (_: SecurityException) {
            // Permission may have been revoked
        }
    }

    fun notifyForSubscription(context: Context, sub: Subscription) {
        val days = sub.daysUntilDue()
        val urgency = sub.urgency()
        val dueText = when {
            days < 0 -> "was due ${-days} days ago!"
            days == 0L -> "is due TODAY!"
            days == 1L -> "is due tomorrow!"
            else -> "renews in $days days (${sub.formattedNextBillingDate()})"
        }

        val title = "🔔 ${sub.name} Renewal Alert"
        val message = "${sub.name} $dueText — ${sub.formattedPrice()} (${sub.paymentMethod})"

        sendRenewalNotification(
            context = context,
            subscriptionId = sub.id,
            title = title,
            message = message,
            isUrgent = urgency == UrgencyLevel.CRITICAL || urgency == UrgencyLevel.TODAY || urgency == UrgencyLevel.OVERDUE
        )
    }

    fun sendTestNotification(context: Context) {
        sendRenewalNotification(
            context = context,
            subscriptionId = 999999L,
            title = "🔔 SubTrack Alert Test",
            message = "Netflix renews in 2 days (₹649 via HDFC Credit Card). Notifications are working perfectly!",
            isUrgent = true
        )
    }
}
