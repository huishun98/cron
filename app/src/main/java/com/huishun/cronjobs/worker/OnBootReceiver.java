package com.huishun.cronjobs.worker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.huishun.cronjobs.database.JobsBase;
import com.huishun.cronjobs.models.JobModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OnBootReceiver extends BroadcastReceiver {
    AlarmManager alarmManager;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            List<JobModel> jobs = JobsBase.get(context).getJobs();
            Toast.makeText(
                    context,
                    "Starting cron jobs",
                    Toast.LENGTH_SHORT).show();
            jobs.forEach((job) -> scheduleNextJob(context, job));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void scheduleNextJob(Context context, JobModel job) {

        // start the alarm now
        Integer jobAlarmId = job.getJobAlarmId();
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("job_id", job.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, jobAlarmId, intent, PendingIntent.FLAG_IMMUTABLE);

        String nextRun = calcNextRun(job);
        job.setJobNextRun(nextRun);
        JobsBase.get(context).updateJob(job);

        Date targetTime = new Date(Long.parseLong(nextRun));
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTime.getTime(), pendingIntent);
    }

    private String calcNextRun(JobModel job) {

        Date currentTime = new Date();
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentTime);

        Calendar nextCalendar = Calendar.getInstance();
        Date targetTime = new Date(Long.parseLong(job.getJobNextRun()));
        nextCalendar.setTime(targetTime);

        // while target time is not more than 10 minutes later than current time, increase the target time
        // this is in case one of previous job is missed
        while (TimeUnit.MILLISECONDS.toMinutes(targetTime.getTime() - currentTime.getTime()) < 0) {
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
}
