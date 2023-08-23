package com.huishun.cronjobs.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_QUERY)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '$JOBS_TABLE_NAME'")
//        onCreate(sqLiteDatabase)
        sqLiteDatabase.execSQL(addColQuery(JOB_CRON, "TEXT", "*/15 * * * *"))
        sqLiteDatabase.execSQL(addColQuery(JOB_PAUSED_AT, "TEXT", "null"))
    }

    override fun onDowngrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '$JOBS_TABLE_NAME'")
        onCreate(sqLiteDatabase)
    }

    companion object {
        private const val VERSION = 3
        private const val DB_NAME = "cronDatabase.db"
        const val JOBS_TABLE_NAME = "jobs"
        private const val ID = "_id"
        const val UUID = "_uuid"
        const val JOB_NAME = "job_name"
        const val JOB_URL = "job_url"
        const val JOB_INTERVAL = "job_interval"
        const val JOB_STATUS = "job_status"
        const val JOB_METHOD = "job_method"
        const val JOB_CRON = "job_cron"
        const val JOB_PARAMS = "job_params"
        const val JOB_TIME_UNIT = "job_time_unit"
        const val JOB_NOTIFY_ERROR = "job_notify_error"
        const val JOB_NOTIFY_SUCCESS = "job_notify_success"
        const val JOB_RESULT = "job_result"
        const val JOB_NEXT_RUN = "job_next_run"
        const val JOB_RUN_COUNT = "job_run_count"
        const val JOB_ALARM_ID = "job_alarm_id"
        const val JOB_LAST_RUN = "job_last_run"
        const val JOB_PAUSED_AT = "job_paused_at"

        private const val SQL_CREATE_TABLE_QUERY =
            ("CREATE TABLE " + JOBS_TABLE_NAME + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + UUID + " TEXT, "
                    + JOB_NAME + " TEXT, "
                    + JOB_URL + " TEXT, "
                    + JOB_INTERVAL + " TEXT, "
                    + JOB_STATUS + " INTEGER, "
                    + JOB_METHOD + " TEXT, "
                    + JOB_CRON + " TEXT, "
                    + JOB_PARAMS + " TEXT, "
                    + JOB_TIME_UNIT + " TEXT, "
                    + JOB_NOTIFY_ERROR + " INTEGER, "
                    + JOB_NOTIFY_SUCCESS + " INTEGER, "
                    + JOB_RESULT + " TEXT, "
                    + JOB_NEXT_RUN + " TEXT, "
                    + JOB_RUN_COUNT + " INTEGER, "
                    + JOB_ALARM_ID + " INTEGER, "
                    + JOB_LAST_RUN + " TEXT,"
                    + JOB_PAUSED_AT + " TEXT)")

        fun addColQuery(col: String, type: String, default: String): String {
            return "ALTER TABLE $JOBS_TABLE_NAME ADD COLUMN $col $type DEFAULT $default"
        }
    }
}
