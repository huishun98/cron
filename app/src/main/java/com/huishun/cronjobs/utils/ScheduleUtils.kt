package com.huishun.cronjobs.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinition
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import com.huishun.cronjobs.models.JobModel
import com.huishun.cronjobs.worker.AlarmReceiver
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date

class ScheduleUtils {
    companion object {
        private var wakeLock: PowerManager.WakeLock? = null
        const val dateFormat = "yyyy-MM-dd_HH-mm-ss"

        @RequiresApi(Build.VERSION_CODES.O)
        fun calcNextRun(job: JobModel): String? {
            val nextRun = calcNextRunCron(job.jobCron) ?: return null
            return nextRun.time.toString()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        @JvmStatic
        fun calcNextRunCron(cronExpression: String): Date? {
            // Define the Unix cron format
            val cronDefinition: CronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
            val parser = CronParser(cronDefinition)
            val now = ZonedDateTime.now()

            try {
                // Parse the cron expression
                val cron = parser.parse(cronExpression)
                val executionTime = ExecutionTime.forCron(cron)
                val next = executionTime.nextExecution(now)
                return Date.from(next.get().toInstant())
            } catch (e: Exception) {
                println("Error parsing cron expression: ${e.message}")
            }
            return null
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        fun scheduleNextJob(context: Context, job: JobModel) {
            if (wakeLock != null) {
                releaseWakeLock()
            }
            acquireWakeLock(context) // should keep cpu awake
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // schedule new alarm
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra("job_id", job.id)

            val pendingIntent =
                PendingIntent.getBroadcast(context, job.jobAlarmId!!, intent, PendingIntent.FLAG_IMMUTABLE)

            val targetTime = Date(job.jobNextRun!!.toLong())
            val calendar = Calendar.getInstance()
            calendar.time = targetTime

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        @SuppressLint("WakelockTimeout")
        private fun acquireWakeLock(context: Context): PowerManager.WakeLock? {
            val powerManager = ContextCompat.getSystemService(context, PowerManager::class.java)
            wakeLock = powerManager?.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "cron:wakelock")
            wakeLock?.acquire()
            return wakeLock
        }

        fun releaseWakeLock() {
            wakeLock?.release()
        }
    }
}
