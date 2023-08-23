package com.huishun.cronjobs.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getString
import com.huishun.cronjobs.MainActivity
import com.huishun.cronjobs.R
import com.huishun.cronjobs.database.Database
import com.huishun.cronjobs.models.JobModel
import com.huishun.cronjobs.utils.NotificationUtils
import com.huishun.cronjobs.utils.ScheduleUtils
import java.util.function.Consumer

class OnBootReceiver : BroadcastReceiver() {
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED != intent.action) return
        val jobs = Database.jobs
        NotificationUtils.createNotificationChannel(context)
        val newIntent = Intent(context, MainActivity::class.java)
        newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val title = getString(context, R.string.app_name)
        val msg = "Starting Cron jobs..."
        NotificationUtils.sendNotification(context, newIntent, title, msg)
        jobs.forEach(Consumer { job: JobModel -> ScheduleUtils.scheduleNextJob(context, job) })
    }
}
