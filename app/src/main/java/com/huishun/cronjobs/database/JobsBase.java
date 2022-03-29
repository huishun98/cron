package com.huishun.cronjobs.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.huishun.cronjobs.models.JobModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JobsBase {
    private static JobsBase jobsBase;
    private final JobsDatabaseHelper databaseHelper;
    private final SQLiteDatabase database;

    private JobsBase(Context context) {
        databaseHelper = new JobsDatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
    }

    public static JobsBase get(Context applicationContext) {
        if(jobsBase == null) {
            jobsBase = new JobsBase(applicationContext);
        }
        return jobsBase;
    }

    public List<JobModel> getJobs() {
        Cursor cursor = database.query(JobsDatabaseHelper.JOBS_TABLE_NAME, null, null, null, null, null, null);
        List<JobModel> jobs = new ArrayList<>();

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                @SuppressLint("Range") String jobName = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_NAME));
                @SuppressLint("Range") String jobUrl = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_URL));
                @SuppressLint("Range") String jobInterval = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_INTERVAL));
                @SuppressLint("Range") String uuid = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.UUID));
                @SuppressLint("Range") Integer jobStatus = cursor.getInt(cursor.getColumnIndex(JobsDatabaseHelper.JOB_STATUS));
                @SuppressLint("Range") String jobMethod = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_METHOD));
                @SuppressLint("Range") String jobParams = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_PARAMS));
                @SuppressLint("Range") String jobTimeUnit = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_TIME_UNIT));
                @SuppressLint("Range") Integer jobNotifyError = cursor.getInt(cursor.getColumnIndex(JobsDatabaseHelper.JOB_NOTIFY_ERROR));
                @SuppressLint("Range") Integer jobNotifySuccess = cursor.getInt(cursor.getColumnIndex(JobsDatabaseHelper.JOB_NOTIFY_SUCCESS));
                @SuppressLint("Range") String jobResult = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_RESULT));
                @SuppressLint("Range") String jobNextRun = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_NEXT_RUN));
                @SuppressLint("Range") Integer jobRunCount = cursor.getInt(cursor.getColumnIndex(JobsDatabaseHelper.JOB_RUN_COUNT));
                @SuppressLint("Range") Integer jobAlarmId = cursor.getInt(cursor.getColumnIndex(JobsDatabaseHelper.JOB_ALARM_ID));
                @SuppressLint("Range") String jobLastRun = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_LAST_RUN));

                JobModel job = new JobModel(UUID.fromString(uuid));
                job.setJobName(jobName);
                job.setJobUrl(jobUrl);
                job.setJobInterval(jobInterval);
                job.setJobStatus(jobStatus);
                job.setJobMethod(jobMethod);
                job.setJobParams(jobParams);
                job.setJobTimeUnit(jobTimeUnit);
                job.setJobNotifyError(jobNotifyError);
                job.setJobNotifySuccess(jobNotifySuccess);
                job.setJobResult(jobResult);
                job.setJobNextRun(jobNextRun);
                job.setJobRunCount(jobRunCount);
                job.setJobAlarmId(jobAlarmId);
                job.setJobLastRun(jobLastRun);
                jobs.add(job);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return jobs;
    }

    public void deleteJob(UUID id) {
        database.delete(
                JobsDatabaseHelper.JOBS_TABLE_NAME,
                JobsDatabaseHelper.UUID + " = ?",
                new String[]{id.toString()}
        );
    }

    public void updateJob(JobModel job) {
        database.update(
                JobsDatabaseHelper.JOBS_TABLE_NAME,
                getContentValues(job),
                JobsDatabaseHelper.UUID + " = ?",
                new String[]{job.getId().toString()});
    }

    public JobModel getJob(UUID id) {
        Cursor cursor = database.query(
                JobsDatabaseHelper.JOBS_TABLE_NAME,
                null,
                JobsDatabaseHelper.UUID + " = ?",
                new String[]{id.toString()},
                null,
                null,
                null
        );

        try {
            cursor.moveToFirst();

            @SuppressLint("Range") String jobName = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_NAME));
            @SuppressLint("Range") String jobUrl = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_URL));
            @SuppressLint("Range") String jobInterval = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_INTERVAL));
            @SuppressLint("Range") Integer jobStatus = cursor.getInt(cursor.getColumnIndex(JobsDatabaseHelper.JOB_STATUS));
            @SuppressLint("Range") String jobMethod = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_METHOD));
            @SuppressLint("Range") String jobParams = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_PARAMS));
            @SuppressLint("Range") String jobTimeUnit = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_TIME_UNIT));
            @SuppressLint("Range") Integer jobNotifyError = cursor.getInt(cursor.getColumnIndex(JobsDatabaseHelper.JOB_NOTIFY_ERROR));
            @SuppressLint("Range") Integer jobNotifySuccess = cursor.getInt(cursor.getColumnIndex(JobsDatabaseHelper.JOB_NOTIFY_SUCCESS));
            @SuppressLint("Range") String jobResult = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_RESULT));
            @SuppressLint("Range") String jobNextRun = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_NEXT_RUN));
            @SuppressLint("Range") Integer jobRunCount = cursor.getInt(cursor.getColumnIndex(JobsDatabaseHelper.JOB_RUN_COUNT));
            @SuppressLint("Range") Integer jobAlarmId = cursor.getInt(cursor.getColumnIndex(JobsDatabaseHelper.JOB_ALARM_ID));
            @SuppressLint("Range") String jobLastRun = cursor.getString(cursor.getColumnIndex(JobsDatabaseHelper.JOB_LAST_RUN));

            JobModel job = new JobModel(id);
            job.setJobName(jobName);
            job.setJobUrl(jobUrl);
            job.setJobInterval(jobInterval);
            job.setJobStatus(jobStatus);
            job.setJobMethod(jobMethod);
            job.setJobParams(jobParams);
            job.setJobTimeUnit(jobTimeUnit);
            job.setJobNotifyError(jobNotifyError);
            job.setJobNotifySuccess(jobNotifySuccess);
            job.setJobResult(jobResult);
            job.setJobNextRun(jobNextRun);
            job.setJobRunCount(jobRunCount);
            job.setJobAlarmId(jobAlarmId);
            job.setJobLastRun(jobLastRun);
            return job;
        } finally {
            cursor.close();
        }
    }

    public void addJob(JobModel job) {
        database.insert(databaseHelper.JOBS_TABLE_NAME, null, getContentValues(job));
    }

    private ContentValues getContentValues(JobModel job) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(JobsDatabaseHelper.UUID, job.getId().toString());
        contentValues.put(JobsDatabaseHelper.JOB_NAME, job.getJobName());
        contentValues.put(JobsDatabaseHelper.JOB_URL, job.getJobUrl());
        contentValues.put(JobsDatabaseHelper.JOB_INTERVAL, job.getJobInterval());
        contentValues.put(JobsDatabaseHelper.JOB_STATUS, job.getJobStatus());
        contentValues.put(JobsDatabaseHelper.JOB_METHOD, job.getJobMethod());
        contentValues.put(JobsDatabaseHelper.JOB_PARAMS, job.getJobParams());
        contentValues.put(JobsDatabaseHelper.JOB_TIME_UNIT, job.getJobTimeUnit());
        contentValues.put(JobsDatabaseHelper.JOB_NOTIFY_ERROR, job.getJobNotifyError());
        contentValues.put(JobsDatabaseHelper.JOB_NOTIFY_SUCCESS, job.getJobNotifySuccess());
        contentValues.put(JobsDatabaseHelper.JOB_RESULT, job.getJobResult());
        contentValues.put(JobsDatabaseHelper.JOB_NEXT_RUN, job.getJobNextRun());
        contentValues.put(JobsDatabaseHelper.JOB_RUN_COUNT, job.getJobRunCount());
        contentValues.put(JobsDatabaseHelper.JOB_ALARM_ID, job.getJobAlarmId());
        contentValues.put(JobsDatabaseHelper.JOB_LAST_RUN, job.getJobLastRun());
        return contentValues;
    }

}
