package com.huishun.cronjobs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class JobsDatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DB_NAME = "cronDatabase.db";
    public static final String JOBS_TABLE_NAME = "jobs";
    private static final String ID = "_id";
    public static final String UUID = "_uuid";
    public static final String JOB_NAME = "job_name";
    public static final String JOB_URL = "job_url";
    public static final String JOB_INTERVAL = "job_interval";
    public static final String JOB_STATUS = "job_status";
    public static final String JOB_METHOD = "job_method";
    public static final String JOB_PARAMS = "job_params";
    public static final String JOB_TIME_UNIT = "job_time_unit";
    public static final String JOB_NOTIFY_ERROR = "job_notify_error";
    public static final String JOB_NOTIFY_SUCCESS = "job_notify_success";
    public static final String JOB_RESULT = "job_result";
    public static final String JOB_NEXT_RUN = "job_next_run";
    public static final String JOB_RUN_COUNT = "job_run_count";
    public static final String JOB_ALARM_ID = "job_alarm_id";
    public static final String JOB_LAST_RUN = "job_last_run";

    private static final String SQL_CREATE_TABLE_QUERY = "CREATE TABLE " + JOBS_TABLE_NAME + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + UUID + " TEXT, "
            + JOB_NAME + " TEXT, "
            + JOB_URL + " TEXT, "
            + JOB_INTERVAL + " TEXT, "
            + JOB_STATUS + " INTEGER, "
            + JOB_METHOD + " TEXT, "
            + JOB_PARAMS + " TEXT, "
            + JOB_TIME_UNIT + " TEXT, "
            + JOB_NOTIFY_ERROR + " INTEGER, "
            + JOB_NOTIFY_SUCCESS + " INTEGER, "
            + JOB_RESULT + " TEXT, "
            + JOB_NEXT_RUN + " TEXT, "
            + JOB_RUN_COUNT + " INTEGER, "
            + JOB_ALARM_ID + " INTEGER, "
            + JOB_LAST_RUN + " TEXT)";

    public JobsDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + JOBS_TABLE_NAME + "'");
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + JOBS_TABLE_NAME + "'");
        onCreate(sqLiteDatabase);
    }
}
