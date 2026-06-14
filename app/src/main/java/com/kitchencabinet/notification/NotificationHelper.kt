package com.kitchencabinet.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.kitchencabinet.MainActivity
import com.kitchencabinet.data.AppDatabase
import com.kitchencabinet.data.PantryItem
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

object NotificationHelper {

    const val CHANNEL_ID = "expiry_reminder"
    const val NOTIFICATION_ID = 1001
    private const val WORK_NAME = "expiry_check"

    fun createChannel(context: Context) {
        val name = "Recordatorios de vencimiento"
        val descriptionText = "Notificaciones cuando ingredientes están por vencer"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun scheduleDailyCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES) // first check soon after scheduling
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelDailyCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    suspend fun checkAndNotify(context: Context) {
        val db = AppDatabase.getInstance(context)
        val repo = com.kitchencabinet.data.Repository(
            db.recipeDao(), db.pantryDao(), db.shoppingDao(),
            db.settingsDao(), db.pantryCategoryDao(), db.mealPlanDao(), db.notificationsConfigDao()
        )

        val config = repo.getNotificationsConfigOnce()
        if (config == null || !config.expiryEnabled) return

        val expiryDays = config.expiryDaysBefore.coerceIn(1, 14)
        val now = System.currentTimeMillis()
        val threshold = now + expiryDays * 24L * 60L * 60L * 1000L

        val expiringItems = db.pantryDao().getAllOnce().filter { item ->
            item.available && item.expiresAt != null &&
                    item.expiresAt in (now + 1) until threshold
        }

        if (expiringItems.isEmpty()) return

        val strings = com.kitchencabinet.ui.i18n.Strings.es
        val title = if (expiringItems.size == 1) {
            "\uD83D\uDD14 ${expiringItems.first().name}"
        } else {
            "\uD83D\uDD14 $expiryDays ${strings.pantry.days.replace("{n}", "$expiryDays")}"
        }
        val body = expiringItems.joinToString("\n") { item ->
            val daysLeft = ((item.expiresAt!! - now) / (24L * 60L * 60L * 1000L)).toInt()
            "${item.name} — ${strings.pantry.days.replace("{n}", "$daysLeft")}"
        }

        showNotification(context, title, body)
    }

    private fun showNotification(context: Context, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "pantry")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted — silently skip
        }
    }
}
