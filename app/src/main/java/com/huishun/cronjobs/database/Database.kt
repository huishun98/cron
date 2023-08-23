package com.huishun.cronjobs.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.huishun.cronjobs.models.JobModel
import java.util.UUID

class Database {

    companion object {
        private lateinit var databaseHelper: DatabaseHelper
        private lateinit var database: SQLiteDatabase

        fun init(context: Context) {
            databaseHelper = DatabaseHelper(context)
            database = databaseHelper.writableDatabase
        }

        val jobs: List<JobModel>
            @SuppressLint("Range")
            get() {
                val cursor = database.query(
                    DatabaseHelper.JOBS_TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
                val jobs: MutableList<JobModel> = ArrayList()
                return cursor.use { c ->
                    c.moveToFirst()
                    while (!c.isAfterLast) {
                        val job = JobModel(UUID.fromString(c.getString(c.getColumnIndex(DatabaseHelper.UUID))))
                        jobs.add(parseJob(job, c))
                        c.moveToNext()
                    }
                    jobs
                }
            }

        @SuppressLint("Range")
        private fun parseJob(job: JobModel, cursor: Cursor): JobModel {
            job.jobName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_NAME))
            job.jobUrl = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_URL))
            job.jobInterval = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_INTERVAL))
            job.jobStatus = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.JOB_STATUS))
            job.jobMethod = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_METHOD))
            job.jobCron = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_CRON))
            job.jobParams = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_PARAMS))
            job.jobTimeUnit = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_TIME_UNIT))
            job.jobNotifyError = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.JOB_NOTIFY_ERROR))
            job.jobNotifySuccess = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.JOB_NOTIFY_SUCCESS))
            job.jobResult = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_RESULT))
            job.jobNextRun = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_NEXT_RUN))
            job.jobRunCount = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.JOB_RUN_COUNT))
            job.jobAlarmId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.JOB_ALARM_ID))
            job.jobLastRun = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_LAST_RUN))
            job.jobPausedAt = cursor.getString(cursor.getColumnIndex(DatabaseHelper.JOB_PAUSED_AT))
            return job
        }

        fun deleteJob(id: UUID) {
            database.delete(
                DatabaseHelper.JOBS_TABLE_NAME,
                DatabaseHelper.UUID + " = ?",
                arrayOf(id.toString())
            )
        }

        fun updateJob(job: JobModel) {
            database.update(
                DatabaseHelper.JOBS_TABLE_NAME,
                getContentValues(job),
                DatabaseHelper.UUID + " = ?",
                arrayOf(job.id.toString())
            )
        }

        @SuppressLint("Range")
        fun getJob(id: UUID): JobModel {
            val cursor = database.query(
                DatabaseHelper.JOBS_TABLE_NAME,
                null,
                DatabaseHelper.UUID + " = ?", arrayOf(id.toString()),
                null,
                null,
                null
            )
            return cursor.use { c ->
                c.moveToFirst()
                parseJob(JobModel(id), c)
            }
        }

        fun addJob(job: JobModel) {
            database.insert(DatabaseHelper.JOBS_TABLE_NAME, null, getContentValues(job))
        }

        private fun getContentValues(job: JobModel): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(DatabaseHelper.UUID, job.id.toString())
            contentValues.put(DatabaseHelper.JOB_NAME, job.jobName)
            contentValues.put(DatabaseHelper.JOB_URL, job.jobUrl)
            contentValues.put(DatabaseHelper.JOB_INTERVAL, job.jobInterval)
            contentValues.put(DatabaseHelper.JOB_STATUS, job.jobStatus)
            contentValues.put(DatabaseHelper.JOB_METHOD, job.jobMethod)
            contentValues.put(DatabaseHelper.JOB_CRON, job.jobCron)
            contentValues.put(DatabaseHelper.JOB_PARAMS, job.jobParams)
            contentValues.put(DatabaseHelper.JOB_TIME_UNIT, job.jobTimeUnit)
            contentValues.put(DatabaseHelper.JOB_NOTIFY_ERROR, job.jobNotifyError)
            contentValues.put(DatabaseHelper.JOB_NOTIFY_SUCCESS, job.jobNotifySuccess)
            contentValues.put(DatabaseHelper.JOB_RESULT, job.jobResult)
            contentValues.put(DatabaseHelper.JOB_NEXT_RUN, job.jobNextRun)
            contentValues.put(DatabaseHelper.JOB_RUN_COUNT, job.jobRunCount)
            contentValues.put(DatabaseHelper.JOB_ALARM_ID, job.jobAlarmId)
            contentValues.put(DatabaseHelper.JOB_LAST_RUN, job.jobLastRun)
            contentValues.put(DatabaseHelper.JOB_PAUSED_AT, job.jobPausedAt)
            return contentValues
        }
    }
}
