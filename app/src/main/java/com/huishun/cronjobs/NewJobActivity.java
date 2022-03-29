package com.huishun.cronjobs;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.huishun.cronjobs.database.JobsBase;
import com.huishun.cronjobs.models.JobModel;
import com.huishun.cronjobs.worker.AlarmReceiver;
import com.huishun.cronjobs.worker.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class NewJobActivity extends AppCompatActivity {

    private EditText jobNameEdittext;
    private EditText jobUrlEdittext;
    private EditText jobIntervalEdittext;

    private Spinner jobMethodSpinner;
    private static final String[] jobMethodOptions = {"GET", "POST", "PUT", "DELETE"};

    private Spinner jobTimeUnitSpinner;
    private static final String[] jobTimeUnitOptions = {"minute", "hour", "day"};

    private EditText jobParamsTextinput;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch jobNotifyErrorSwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch jobNotifySuccessSwitch;

    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_job);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        jobNameEdittext = findViewById(R.id.job_name_edittext);
        jobUrlEdittext = findViewById(R.id.job_url_edittext);
        jobIntervalEdittext = findViewById(R.id.job_interval_edittext);
        jobParamsTextinput = findViewById(R.id.job_params_edittext);
        jobNotifyErrorSwitch = findViewById(R.id.job_notify_error_switch);
        jobNotifySuccessSwitch = findViewById(R.id.job_notify_success_switch);

        jobMethodSpinner = findViewById(R.id.job_method_spinner);
        ArrayAdapter<String> job_method_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, jobMethodOptions);
        job_method_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobMethodSpinner.setAdapter(job_method_adapter);
        jobMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 3) {
                    jobParamsTextinput.setEnabled(false);
                    jobParamsTextinput.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                } else {
                    jobParamsTextinput.setEnabled(true);
                    jobParamsTextinput.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        jobTimeUnitSpinner = findViewById(R.id.job_time_unit_spinner);
        ArrayAdapter<String>job_time_unit_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, jobTimeUnitOptions);
        job_time_unit_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobTimeUnitSpinner.setAdapter(job_time_unit_adapter);

        Button add_button = findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                // input checks
                if (TextUtils.isEmpty(jobNameEdittext.getText()) ||
                        TextUtils.isEmpty(jobUrlEdittext.getText()) ||
                        TextUtils.isEmpty(jobIntervalEdittext.getText()) ||
                        Integer.parseInt(jobIntervalEdittext.getText().toString()) == 0
                ) {
                    Toast.makeText(
                            NewJobActivity.this,
                            "Please make sure all fields are filled in correctly",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (Integer.parseInt(jobIntervalEdittext.getText().toString()) % 15 != 0
                        && jobTimeUnitSpinner.getSelectedItemPosition() == 0
                ) {
                    Toast.makeText(
                            NewJobActivity.this,
                            "Please use 15-minute intervals",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (jobParamsTextinput.getText().toString().length() > 0) {
                    try {
                        new JSONObject(jobParamsTextinput.getText().toString());
                    } catch (JSONException ex) {
                        Toast.makeText(
                                NewJobActivity.this,
                                "Please make sure that your json payload is valid",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                try {
                    new URL(jobUrlEdittext.getText().toString());
                } catch (Exception e1) {
                    Toast.makeText(
                            NewJobActivity.this,
                            "Please make sure that your job url is valid",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                JobModel job = new JobModel();
                job.setJobName(jobNameEdittext.getText().toString());
                job.setJobUrl(jobUrlEdittext.getText().toString());
                job.setJobInterval(jobIntervalEdittext.getText().toString());
                job.setJobStatus(1);
                job.setJobMethod(jobMethodOptions[jobMethodSpinner.getSelectedItemPosition()]);
                job.setJobParams(jobParamsTextinput.getText().toString());
                job.setJobTimeUnit(jobTimeUnitOptions[jobTimeUnitSpinner.getSelectedItemPosition()]);
                job.setJobNotifyError(jobNotifyErrorSwitch.isChecked() ? 1 : 0);
                job.setJobNotifySuccess(jobNotifySuccessSwitch.isChecked() ? 1 : 0);
                job.setJobResult("");
                job.setJobNextRun(calcNextRun());
                job.setJobRunCount(0);
                job.setJobLastRun("1580897313933");

                JobsBase.get(getApplicationContext()).addJob(job);

                try {
                    scheduleNextJob(job);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Toast.makeText(NewJobActivity.this, "Job added", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void scheduleNextJob(JobModel job) throws ParseException {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        }

        Calendar calendar = Calendar.getInstance();
        int jobAlarmId = (int) calendar.getTimeInMillis();
        job.setJobAlarmId(jobAlarmId);
        JobsBase.get(this).updateJob(job);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("job_id", job.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, jobAlarmId, intent, PendingIntent.FLAG_IMMUTABLE);
//        AlarmManager.AlarmClockInfo alarmClock = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
//        alarmManager.setAlarmClock(alarmClock,pendingIntent);
        Date targetTime = new Date(Long.parseLong(job.getJobNextRun()));
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTime.getTime(), pendingIntent);
    }

    private String calcNextRun() {
        long currentTime = new Date().getTime();
        long fifteen = 15 * 60 * 1000;
        long nextTime = (currentTime / fifteen + 1) * fifteen;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date(nextTime));
//        switch (job.getJobTimeUnit()) {
//            case "hour":
//                calendar.add(Calendar.HOUR, Integer.parseInt(job.getJobInterval()));
//                break;
//            case "day":
//                calendar.add(Calendar.DATE, Integer.parseInt(job.getJobInterval()));
//                break;
//            default:
//                calendar.add(Calendar.MINUTE, Integer.parseInt(job.getJobInterval()));
//                break;
//        }
        String targetDate = Long.toString(nextTime);
        return targetDate;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}