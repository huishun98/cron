package com.huishun.cronjobs

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.huishun.cronjobs.components.JobsAdapter
import com.huishun.cronjobs.database.Database
import com.huishun.cronjobs.models.JobModel
import com.huishun.cronjobs.utils.NotificationUtils
import com.huishun.cronjobs.utils.UIUtils
import java.util.UUID

class MainActivity : AppCompatActivity(), OnRefreshListener {
    private lateinit var swipeLayout: SwipeRefreshLayout
    private var jobs: List<JobModel> = emptyList()
    private lateinit var jobsAdapter: JobsAdapter
    private lateinit var newJobsPrompt: View

    @SuppressLint("BatteryLife")
    @RequiresApi(api = Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        UIUtils.showToolbar(findViewById(R.id.toolbar), this, getString(R.string.app_name), false)
        Database.init(this)

        newJobsPrompt = findViewById(R.id.add_new_job_prompt)
        swipeLayout = findViewById(R.id.swipe_layout)
        swipeLayout.setOnRefreshListener(this)

        val jobsRecyclerview = findViewById<RecyclerView>(R.id.jobs_recyclerview)
        jobs = Database.jobs
        jobsAdapter = JobsAdapter(jobs) { launchDetails(it) }
        jobsRecyclerview.adapter = jobsAdapter
        jobsRecyclerview.layoutManager = LinearLayoutManager(this)

        if (jobsAdapter.itemCount > 0) newJobsPrompt.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.add_button).setOnClickListener {
            val intent = Intent(this, DetailsActivity::class.java)
            detailActivityResultLauncher.launch(intent)
        }

        NotificationUtils.createNotificationChannel(this)
        if (!isBatteryOptimizationExempt()) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    var detailActivityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        jobs = Database.jobs
        jobsAdapter.setJobs(jobs)
        jobsAdapter.notifyDataSetChanged()
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        result.data ?: return@registerForActivityResult
        newJobsPrompt.visibility = View.VISIBLE
        if (jobsAdapter.itemCount > 0) newJobsPrompt.visibility = View.GONE
    }

    private fun launchDetails(jobId: UUID) {
        val data = Intent(this, DetailsActivity::class.java)
        data.putExtra("job_id", jobId)
        detailActivityResultLauncher.launch(data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return UIUtils.handleMenuOptions(this, item)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onRefresh() {
        jobsAdapter.setJobs(Database.jobs)
        jobsAdapter.notifyDataSetChanged()
        swipeLayout.isRefreshing = false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isBatteryOptimizationExempt(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }
}
