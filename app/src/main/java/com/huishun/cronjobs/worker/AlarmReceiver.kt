package com.huishun.cronjobs.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.CursorIndexOutOfBoundsException
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.huishun.cronjobs.MainActivity
import com.huishun.cronjobs.database.Database
import com.huishun.cronjobs.models.JobModel
import com.huishun.cronjobs.utils.NotificationUtils
import com.huishun.cronjobs.utils.ScheduleUtils
import com.huishun.cronjobs.utils.UIUtils
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.UUID

class AlarmReceiver : BroadcastReceiver() {
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val jobId = intent.getSerializableExtra("job_id").toString()

        Database.init(context)

        // job is not in db but in bg process. ignore.
        val job = try {
            Database.getJob(UUID.fromString(jobId))
        } catch (e: CursorIndexOutOfBoundsException) { return }

        if (job.jobPausedAt != null) return
        val currentDate = Date()
        val jobCurrentRun = currentDate.time.toString()
        job.jobLastRun = jobCurrentRun
        Database.updateJob(job)
        sendRequest(context, job)
        job.jobNextRun = ScheduleUtils.calcNextRun(job)
        job.jobRunCount = job.jobRunCount + 1
        Database.updateJob(job)
        ScheduleUtils.scheduleNextJob(context, job)
    }

    private fun sendRequest(context: Context, job: JobModel) {
        val queue = Volley.newRequestQueue(context)
        val jsonString = job.jobParams
        val method: Int = when (job.jobMethod) {
            "POST" -> Request.Method.POST
            "PUT" -> Request.Method.PUT
            "DELETE" -> Request.Method.DELETE
            else -> Request.Method.GET
        }
        val stringRequest = object : StringRequest(method, job.jobUrl, Response.Listener { resp ->
                job.jobResult = resp
                Database.updateJob(job)
                if (job.jobNotifySuccess != 1) return@Listener
                notify(context, job.jobName, "${job.jobMethod} request successful")
            }, Response.ErrorListener { err ->
//                TODO - may not work as expected
                try {
                    job.jobResult = String(err.networkResponse.data, StandardCharsets.UTF_8)
                } catch (e: NullPointerException) {
                    val sw = StringWriter()
                    e.printStackTrace(PrintWriter(sw))
                    job.jobResult = sw.toString()
                }
                UIUtils.showToast(context, job.jobResult.toString())
                if (job.jobNotifyError != 1) return@ErrorListener
                notify(context, job.jobName, "Error — unable to call endpoint")
                Database.updateJob(job)
            }) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
                override fun getBody(): ByteArray? {
                    return jsonString?.toByteArray(StandardCharsets.UTF_8)
                }
            }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            0,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

    private fun notify(context: Context, title: String?, msg: String) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        NotificationUtils.sendNotification(context, intent, title, msg)
    }
}
