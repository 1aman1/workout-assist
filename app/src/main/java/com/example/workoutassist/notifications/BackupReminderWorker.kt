package com.example.workoutassist.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.workoutassist.MainActivity

internal const val BACKUP_REMINDER_CHANNEL_ID = "backup_reminder"
private const val BACKUP_REMINDER_NOTIFICATION_ID = 1001

/** Fires weekly (see [BackupReminderScheduler]) to nudge the user to back up their data. */
internal class BackupReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        showNotification(applicationContext)
        return Result.success()
    }

    private fun showNotification(context: Context) {
        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openIntent = MainActivity.newBackupSettingsIntent(context)
        val contentIntent = PendingIntent.getActivity(
            context,
            BACKUP_REMINDER_NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, BACKUP_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Back up your workout data")
            .setContentText("It's been a week — export a backup to keep your progress safe.")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .addAction(0, "Open Backup Settings", contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(BACKUP_REMINDER_NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            BACKUP_REMINDER_CHANNEL_ID,
            "Backup reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Weekly reminder to back up your workout data."
        }
        manager.createNotificationChannel(channel)
    }
}
