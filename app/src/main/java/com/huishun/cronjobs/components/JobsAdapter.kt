package com.huishun.cronjobs.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.huishun.cronjobs.R
import com.huishun.cronjobs.models.JobModel
import java.text.SimpleDateFormat
import java.util.UUID

class JobsAdapter(private var jobs: List<JobModel>, private val onClick: (UUID) -> Unit) : RecyclerView.Adapter<JobsAdapter.JobViewHolder>() {
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.job_item, null)
        return JobViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = this.jobs[position]
        holder.jobNameTextview.text = job.jobName
        holder.jobUrlTextview.text = job.jobUrl
        holder.jobMethodTextview.text = job.jobMethod
        holder.jobNextRunTextview.text = SimpleDateFormat("HH:mm dd-MM-yy")
            .format(job.jobNextRun!!.toLong()).toString()
        holder.jobMethodTextview.setBackgroundResource(R.drawable.bg_blue)
        holder.jobMethodTextview.setTextColor(ContextCompat.getColor(holder.context, R.color.blue_200))
        holder.jobCronTextview.text = job.jobCron
        holder.jobWrapper.setOnClickListener { onClick.invoke(job.id) }

        if (job.jobPausedAt == null) return
        holder.jobNextRunTextview.text = "-"
        holder.jobMethodTextview.setBackgroundResource(R.drawable.bg_radio_button)
        holder.jobMethodTextview.setTextColor(ContextCompat.getColor(holder.context, R.color.gray_200))
    }

    override fun getItemCount(): Int {
        return this.jobs.size
    }

    fun setJobs(jobs: List<JobModel>) {
        this.jobs = jobs
    }

    class JobViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val jobNameTextview : TextView = view.findViewById(R.id.job_name_textview)
        val jobUrlTextview : TextView = view.findViewById(R.id.job_url_textview)
        val jobMethodTextview : TextView = view.findViewById(R.id.job_method_textview)
        val jobCronTextview: TextView = view.findViewById(R.id.job_cron_textview)
        val jobNextRunTextview : TextView= view.findViewById(R.id.job_next_run_textview)
        val jobWrapper : RelativeLayout = view.findViewById(R.id.job_item)
        val context: Context = view.context
    }
}