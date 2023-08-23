package com.huishun.cronjobs.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.getSystemService
import com.huishun.cronjobs.R
import java.util.Date

class NotificationUtils {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun createNotificationChannel(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name: CharSequence = getString(context, R.string.channel_name)
            val description = getString(context,R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(getString(context,R.string.cron_channel_id), name, importance)
            channel.description = description
            val notificationManager = getSystemService(context, NotificationManager::class.java)!!
            notificationManager.createNotificationChannel(channel)
        }

        fun sendNotification(context: Context, intent: Intent, title: String?, message: String) {
            val pendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(
                context,
                getString(context, R.string.cron_channel_id)
            )
                .setSmallIcon(R.drawable.ic_schedule_send_white)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            val notificationManagerCompat = NotificationManagerCompat.from(context)

            if (ActivityCompat.checkSelfPermission(
                    context,
                    "android.permission.POST_NOTIFICATIONS"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            val newNotificationId = (Date().time / 1000L % Int.MAX_VALUE).toInt()
            notificationManagerCompat.notify(newNotificationId, builder.build())
        }
    }
}
