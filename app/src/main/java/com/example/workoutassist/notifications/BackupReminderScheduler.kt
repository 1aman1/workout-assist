package com.example.workoutassist.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val BACKUP_REMINDER_WORK_NAME = "backup_reminder_weekly"
private const val REMINDER_HOUR_OF_DAY = 10

/** Schedules/cancels the weekly (Sunday) backup-reminder notification via WorkManager. */
internal object BackupReminderScheduler {

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<BackupReminderWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(millisUntilNextSunday(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BACKUP_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(BACKUP_REMINDER_WORK_NAME)
    }

    private fun millisUntilNextSunday(): Long {
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR_OF_DAY)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = Calendar.getInstance()
        while (target.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY || !target.after(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
