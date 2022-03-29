package com.huishun.cronjobs;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class DetailsActivity extends AppCompatActivity {

    private EditText jobNameEdittext;
    private EditText jobUrlEdittext;
    private EditText jobIntervalEdittext;

    private Button saveButton;
    private Button deleteButton;
    private Button downloadButton;

    private Spinner jobMethodSpinner;
    private static final String[] jobMethodOptions = {"GET", "POST", "PUT", "DELETE"};

    private Spinner jobTimeUnitSpinner;
    private static final String[] jobTimeUnitOptions = {"minute", "hour", "day"};

    private UUID id;
    private EditText jobParamsTextinput;
    private Switch jobNotifyErrorSwitch;
    private Switch jobNotifySuccessSwitch;

    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        id = (UUID) getIntent().getSerializableExtra("job_id");
        JobModel job = JobsBase.get(getApplicationContext()).getJob(id);

        jobNameEdittext = findViewById(R.id.job_name_edittext);
        jobUrlEdittext = findViewById(R.id.job_url_edittext);
        jobIntervalEdittext = findViewById(R.id.job_interval_edittext);

        jobNameEdittext.setText(job.getJobName());
        jobUrlEdittext.setText(job.getJobUrl());
        jobIntervalEdittext.setText(job.getJobInterval());

        jobMethodSpinner = findViewById(R.id.job_method_spinner);
        ArrayAdapter<String>jobMethodAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, jobMethodOptions);
        jobMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobMethodSpinner.setAdapter(jobMethodAdapter);
        jobMethodSpinner.setSelection(jobMethodAdapter.getPosition(job.getJobMethod()));
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
        ArrayAdapter<String>jobTimeUnitAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, jobTimeUnitOptions);
        jobTimeUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobTimeUnitSpinner.setAdapter(jobTimeUnitAdapter);
        jobTimeUnitSpinner.setSelection(jobTimeUnitAdapter.getPosition(job.getJobTimeUnit()));

        jobParamsTextinput = findViewById(R.id.job_params_edittext);
        jobParamsTextinput.setText(job.getJobParams());

        jobNotifyErrorSwitch = findViewById(R.id.job_notify_error_switch);
        jobNotifyErrorSwitch.setChecked(job.getJobNotifyError() == 1);

        jobNotifySuccessSwitch = findViewById(R.id.job_notify_success_switch);
        jobNotifySuccessSwitch.setChecked(job.getJobNotifySuccess() == 1);

        saveButton = findViewById(R.id.add_button);
        deleteButton = findViewById(R.id.delete_button);
        downloadButton = findViewById(R.id.download_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
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
                            DetailsActivity.this,
                            "Please make sure all fields are filled in correctly",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (Integer.parseInt(jobIntervalEdittext.getText().toString()) % 15 != 0
                        && jobTimeUnitSpinner.getSelectedItemPosition() == 0
                ) {
                    Toast.makeText(
                            DetailsActivity.this,
                            "Please use 15-minute intervals",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (jobParamsTextinput.getText().toString().length() > 0) {
                    try {
                        new JSONObject(jobParamsTextinput.getText().toString());
                    } catch (JSONException ex) {
                        Toast.makeText(
                                DetailsActivity.this,
                                "Please make sure that your json payload is valid",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                try {
                    new URL(jobUrlEdittext.getText().toString());
                } catch (Exception e1) {
                    Toast.makeText(
                            DetailsActivity.this,
                            "Please make sure that your job url is valid",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                job.setJobName(jobNameEdittext.getText().toString());
                job.setJobUrl(jobUrlEdittext.getText().toString());
                job.setJobInterval(jobIntervalEdittext.getText().toString());
                job.setJobMethod(jobMethodOptions[jobMethodSpinner.getSelectedItemPosition()]);
                job.setJobParams(jobParamsTextinput.getText().toString());
                job.setJobTimeUnit(jobTimeUnitOptions[jobTimeUnitSpinner.getSelectedItemPosition()]);
                job.setJobNotifyError(jobNotifyErrorSwitch.isChecked() ? 1 : 0);
                job.setJobNotifySuccess(jobNotifySuccessSwitch.isChecked() ? 1 : 0);
                job.setJobNextRun(calcNextRun(job));
                job.setJobRunCount(0);
                job.setJobLastRun("1580897313933");

                JobsBase.get(getApplicationContext()).updateJob(job);
                Toast.makeText(DetailsActivity.this, "Record updated", Toast.LENGTH_SHORT).show();

                try {
                    scheduleNextJob(job);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this job?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);

                        JobsBase.get(getApplicationContext()).deleteJob(id);

                        Toast.makeText(DetailsActivity.this, "Job deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date now = new Date();
                String dateId = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",  Locale.ENGLISH).format(now);
                String fileName = job.getJobName().replaceAll(" ", "-") + "_" + dateId + ".txt";

                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                            DetailsActivity.this.openFileOutput(
                                    fileName,
                                    MODE_PRIVATE
                            )
                    );
                    outputStreamWriter.write(job.getJobResult());
                    outputStreamWriter.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailsActivity.this, "Unable to download file, please contact the app creator.", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                File file = new File(DetailsActivity.this.getFilesDir() + "/" + fileName);
                Uri uri = FileProvider.getUriForFile(DetailsActivity.this, getApplicationContext().getPackageName() + ".provider", file);
                intent.setDataAndType(uri, "text/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                PendingIntent pendingIntent = PendingIntent.getActivity(DetailsActivity.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(DetailsActivity.this, getString(R.string.cron_channel_id))
                        .setSmallIcon(R.drawable.ic_schedule_send_white)
                        .setContentTitle("File download for job — " + job.getJobName())
                        .setContentText("Click to view file")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(DetailsActivity.this);
                notificationManager.notify(createNotificationId(), builder.build());

                Toast.makeText(DetailsActivity.this, "File downloaded", Toast.LENGTH_LONG).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void scheduleNextJob(JobModel job) throws ParseException {

        if (alarmManager == null) {
            alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        }

        // start the alarm now
        Integer jobAlarmId = job.getJobAlarmId();
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("job_id", job.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, jobAlarmId, intent, PendingIntent.FLAG_IMMUTABLE);

//        Calendar calendar = Calendar.getInstance();
//        AlarmManager.AlarmClockInfo alarmClock = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
//        alarmManager.setAlarmClock(alarmClock,pendingIntent);
        Date targetTime = new Date(Long.parseLong(job.getJobNextRun()));
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTime.getTime(), pendingIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // have to calculate next run here to show it in main activity
    private String calcNextRun(JobModel job) {
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

    public int createNotificationId(){
        return (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    }

}