package com.huishun.cronjobs;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huishun.cronjobs.database.JobsBase;
import com.huishun.cronjobs.models.JobModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeLayout;
    private List<JobModel> jobs;
    private JobsAdapter jobsAdapter;

    ActivityResultLauncher<Intent> detailActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onActivityResult(ActivityResult result) {
                    jobs = JobsBase.get(getApplicationContext()).getJobs();
                    jobsAdapter.setJobs(jobs);
                    jobsAdapter.notifyDataSetChanged();

                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if(data != null) {
                            if (jobsAdapter.getItemCount() > 0) {
                                findViewById(R.id.add_new_job_prompt).setVisibility(View.GONE);
                                swipeLayout.setEnabled(true);
                            } else {
                                findViewById(R.id.add_new_job_prompt).setVisibility(View.VISIBLE);
                                swipeLayout.setEnabled(false);
                            }
                        }
                    }
                }
            });

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//        if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
//            checkPermissions();
//        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(R.string.app_name);
        }

        RecyclerView jobsRecyclerview = findViewById(R.id.jobs_recyclerview);
        jobsRecyclerview.setLayoutManager(new LinearLayoutManager(this));

        swipeLayout = findViewById(R.id.swipe_layout);
        swipeLayout.setOnRefreshListener(this);

        jobs = JobsBase.get(getApplicationContext()).getJobs();

        jobsAdapter = new JobsAdapter(jobs);
        jobsRecyclerview.setAdapter(jobsAdapter);

        if (jobsAdapter.getItemCount() > 0) {
            findViewById(R.id.add_new_job_prompt).setVisibility(View.GONE);
            swipeLayout.setEnabled(true);
        } else {
            swipeLayout.setEnabled(false);
        }

        createNotificationChannel();
    }

//    private void checkPermissions() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle(Html.fromHtml("Disable <b>Cron</b>'s battery optimisation?"));
//        builder.setMessage("Please disable Cron's battery optimization so that your jobs can be executed on time even when your phone is in doze mode.");
//        builder.setCancelable(false);
//        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.M)
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
//            }
//        });
//        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog,int which) {
//                dialog.cancel();
//            }
//        });
//        AlertDialog alert_dialog = builder.create();
//        alert_dialog.show();
//    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.cron_channel_id), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_job:
                Intent intent = new Intent(this, NewJobActivity.class);
                detailActivityResultLauncher.launch(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onRefresh() {
        jobs = JobsBase.get(MainActivity.this).getJobs();
        jobsAdapter.setJobs(jobs);
        jobsAdapter.notifyDataSetChanged();
        swipeLayout.setRefreshing(false);
    }

    class JobsAdapter extends RecyclerView.Adapter<JobViewHolder> {
        private List<JobModel> jobs;

        public JobsAdapter(List<JobModel> jobs) {
            super();
            this.jobs = jobs;
        }

        @NonNull
        @Override
        public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new JobViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
            holder.bind(this.jobs.get(position));
        }

        @Override
        public int getItemCount() {
            return this.jobs.size();
        }

        public void setJobs(List<JobModel> jobs) {
            this.jobs = jobs;
        }
    }

    public class JobViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private UUID id;
        private final TextView jobNameTextview;
        private final TextView jobUrlTextview;
        private final TextView jobIntervalTextview;
        private final TextView jobMethodTextview;
        private final TextView jobTimeUnitTextview;
        private final TextView jobNextRunTextview;

        @Override
        public void onClick(View view) {
            Intent data = new Intent(MainActivity.this, DetailsActivity.class);
            data.putExtra("job_id", this.id);
            detailActivityResultLauncher.launch(data);
        }

        public JobViewHolder(ViewGroup container) {
            super(LayoutInflater.from(MainActivity.this).inflate(R.layout.job_item, container, false));
            itemView.setOnClickListener(this);

            jobNameTextview = itemView.findViewById(R.id.job_name_textview);
            jobUrlTextview = itemView.findViewById(R.id.job_url_textview);
            jobIntervalTextview = itemView.findViewById(R.id.job_interval_textview);
            jobMethodTextview = itemView.findViewById(R.id.job_method_textview);
            jobTimeUnitTextview = itemView.findViewById(R.id.job_time_unit_textview);
            jobNextRunTextview = itemView.findViewById(R.id.job_next_run_textview);
        }

        @SuppressLint("SetTextI18n")
        public void bind(JobModel job) {
            this.id = job.getId();
            jobNameTextview.setText(job.getJobName());
            jobUrlTextview.setText(job.getJobUrl());
            jobIntervalTextview.setText(job.getJobInterval());
            jobMethodTextview.setText(job.getJobMethod());
            jobTimeUnitTextview.setText(" " +
                    job.getJobTimeUnit() +
                    "(s)");
            @SuppressLint("SimpleDateFormat") String job_next_run = String.valueOf(new SimpleDateFormat("HH:mm dd-MM-yyyy")
                    .format(Long.parseLong(job.getJobNextRun())));
            jobNextRunTextview.setText(job_next_run);
        }
    }
}