package com.huishun.cronjobs.worker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.huishun.cronjobs.MainActivity;
import com.huishun.cronjobs.R;
import com.huishun.cronjobs.database.JobsBase;
import com.huishun.cronjobs.models.JobModel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AlarmReceiver extends BroadcastReceiver {

    AlarmManager alarmManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        String jobId = intent.getSerializableExtra("job_id").toString();
        JobModel job;

        try {
            job = JobsBase.get(context).getJob(UUID.fromString(jobId));
        } catch (CursorIndexOutOfBoundsException e) {
            // job is not in db but in bg process.
            // if job doesn't exist in db, will neither call api nor reschedule.
            return;
        }

        Date currentDate = new Date();
        Date jobLastRun;

        jobLastRun = new Date(Long.parseLong(job.getJobLastRun()));
        if (TimeUnit.MILLISECONDS.toMinutes(currentDate.getTime() - jobLastRun.getTime()) < 10) { // call came in too early, probably a repeat call
            return;
        }

        String jobCurrentRun = Long.toString(currentDate.getTime());
        job.setJobLastRun(jobCurrentRun);
        JobsBase.get(context).updateJob(job);

        sendAndRequestResponse(context, job);

        job.setJobNextRun(calcNextRun(job));
        job.setJobRunCount(job.getJobRunCount() + 1);
        JobsBase.get(context).updateJob(job);

        scheduleNextJob(context, job);

    }

    public int createNotificationId(){
        return (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String calcNextRun(JobModel job) {

        Date currentTime = new Date();
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentTime);

        Calendar nextCalendar = Calendar.getInstance();
        Date targetTime = new Date(Long.parseLong(job.getJobNextRun()));
        nextCalendar.setTime(targetTime);

        // while target time is not more than 10 minutes later than current time, increase the target time
        // this is in case one of previous job is missed
        while (TimeUnit.MILLISECONDS.toMinutes(targetTime.getTime() - currentTime.getTime()) < 10) {
            switch (job.getJobTimeUnit()) {
                case "hour":
                    nextCalendar.add(Calendar.HOUR, Integer.parseInt(job.getJobInterval()));
                    targetTime = nextCalendar.getTime();
                    break;
                case "day":
                    nextCalendar.add(Calendar.DATE, Integer.parseInt(job.getJobInterval()));
                    targetTime = nextCalendar.getTime();
                    break;
                default:
                    nextCalendar.add(Calendar.MINUTE, Integer.parseInt(job.getJobInterval()));
                    targetTime = nextCalendar.getTime();
                    break;
            }
        }

        return Long.toString(nextCalendar.getTime().getTime());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void scheduleNextJob(Context context, JobModel job) {

        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        // schedule new alarm
        int jobAlarmId = job.getJobAlarmId();
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("job_id", job.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, jobAlarmId, intent, PendingIntent.FLAG_IMMUTABLE);

        Date targetTime = new Date(Long.parseLong(job.getJobNextRun()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetTime);
//        AlarmManager.AlarmClockInfo alarmClock = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
//        alarmManager.setAlarmClock(alarmClock, pendingIntent);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public void sendAndRequestResponse(Context context, JobModel job) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String jsonString = job.getJobParams();

        int method;
        switch (job.getJobMethod()) {
            case "POST":
                method = Request.Method.POST;
                break;
            case "PUT":
                method = Request.Method.PUT;
                break;
            case "DELETE":
                method = Request.Method.DELETE;
                break;
            default:
                method = Request.Method.GET;
        }

        StringRequest stringRequest = new StringRequest(method, job.getJobUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(String response) {
                job.setJobResult(response);
                JobsBase.get(context).updateJob(job);

                // show success notification
                if (job.getJobNotifySuccess() == 1) {
                    sendNotification(context, job, "Request is successful");
                }
            }
        }, new Response.ErrorListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onErrorResponse(VolleyError error) {

                // set error result
                try {
                    job.setJobResult(new String(error.networkResponse.data, StandardCharsets.UTF_8));
                } catch (NullPointerException e) {
                    Toast.makeText(
                            context,
                            job.getJobName() + " — Please check your internet connection and make sure that the job url is correct",
                            Toast.LENGTH_LONG).show();
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    job.setJobResult(sw.toString());
                    JobsBase.get(context).updateJob(job);
                    e.printStackTrace();
                }

                // show error notification
                if (job.getJobNotifyError() == 1) {
                    sendNotification(context, job, "Error — unable to call endpoint");
                }
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return jsonString == null ? null : jsonString.getBytes(StandardCharsets.UTF_8);
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(stringRequest);
    }

    private void sendNotification(Context context, JobModel job, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.cron_channel_id))
                .setSmallIcon(R.drawable.ic_schedule_send_white)
                .setContentTitle(job.getJobName())
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(createNotificationId(), builder.build());
    }
}