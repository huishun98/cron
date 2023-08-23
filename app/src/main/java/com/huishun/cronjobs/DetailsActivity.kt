package com.huishun.cronjobs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputLayout
import com.huishun.cronjobs.database.Database
import com.huishun.cronjobs.models.JobModel
import com.huishun.cronjobs.utils.NotificationUtils
import com.huishun.cronjobs.utils.ScheduleUtils
import com.huishun.cronjobs.utils.UIUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


@SuppressLint("UseSwitchCompatOrMaterialCode")
class DetailsActivity : AppCompatActivity() {
    private lateinit var jobNameEdittext: EditText
    private lateinit var jobUrlEdittext: EditText
    private lateinit var jobCronEdittext: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var downloadButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var jobParamsWrapper: TextInputLayout
    private lateinit var jobParamsTextinput: EditText
    private lateinit var jobNotifyErrorSwitch: Switch
    private lateinit var jobNotifySuccessSwitch: Switch
    private lateinit var jobPausedSwitch: Switch

    private var job: JobModel = JobModel()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        UIUtils.showToolbar(findViewById(R.id.toolbar), this, "", true)

        val id = intent.getSerializableExtra("job_id") as UUID?
        if (id != null) {
            job = Database.getJob(id)
        }

        jobNameEdittext = findViewById(R.id.job_name_edittext)
        jobUrlEdittext = findViewById(R.id.job_url_edittext)
        jobCronEdittext = findViewById(R.id.job_cron_edittext)
        jobParamsTextinput = findViewById(R.id.job_params_edittext)
        jobParamsWrapper = findViewById(R.id.job_params_wrapper)
        jobNameEdittext.setText(job.jobName)
        jobUrlEdittext.setText(job.jobUrl)
        jobCronEdittext.setText(job.jobCron)
        jobParamsTextinput.setText(job.jobParams)
        radioGroup = findViewById(R.id.radio_group)
        radioGroup.setOnCheckedChangeListener { _, i -> when(i) {
            R.id.radio1 -> jobParamsWrapper.visibility = View.GONE
            R.id.radio2 -> jobParamsWrapper.visibility = View.VISIBLE
            R.id.radio3 -> jobParamsWrapper.visibility = View.VISIBLE
            R.id.radio4 -> jobParamsWrapper.visibility = View.GONE
        } }

        // switches
        jobNotifyErrorSwitch = findViewById(R.id.job_notify_error_switch)
        jobNotifyErrorSwitch.isChecked = job.jobNotifyError == 1
        jobNotifySuccessSwitch = findViewById(R.id.job_notify_success_switch)
        jobNotifySuccessSwitch.isChecked = job.jobNotifySuccess == 1
        jobPausedSwitch = findViewById(R.id.job_paused_switch)
        jobPausedSwitch.isChecked = job.jobPausedAt != null

        // buttons
        saveButton = findViewById(R.id.add_button)
        deleteButton = findViewById(R.id.delete_button)
        downloadButton = findViewById(R.id.download_button)
        saveButton.setOnClickListener { if(validateFields()) save(id) }
        if (id == null) {
            deleteButton.visibility = View.GONE
            downloadButton.visibility = View.GONE
        }
        deleteButton.setOnClickListener { delete(id!!) }
        downloadButton.setOnClickListener { download() }
    }

    private fun delete(id: UUID) {
        val builder = AlertDialog.Builder(this@DetailsActivity)
        builder.setTitle("Delete")
        builder.setMessage("Are you sure you want to delete this job?")
        builder.setCancelable(false)
        builder.setPositiveButton("Yes") { _, _ ->
            val intent = Intent()
            setResult(RESULT_OK, intent)
            Database.deleteJob(id)
            UIUtils.showToast(this, "Job deleted")
            finish()
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateFields(): Boolean {
        if (TextUtils.isEmpty(jobNameEdittext.text) ||
            TextUtils.isEmpty(jobUrlEdittext.text) ||
            TextUtils.isEmpty(jobCronEdittext.text)
        ) {
            UIUtils.showToast(this, "Please make sure all fields are filled in correctly")
            return false
        }

        try { URL(jobUrlEdittext.text.toString()) } catch (e1: Exception) {
            UIUtils.showToast(this, "Please make sure that your job url is valid")
            return false
        }

        if (ScheduleUtils.calcNextRunCron(jobCronEdittext.text.toString()) == null) {
            UIUtils.showToast(this, "Cron expression is invalid")
            return false
        }

        if (jobParamsTextinput.text.toString().isNotEmpty()) {
            try { JSONObject(jobParamsTextinput.text.toString()) } catch (ex: JSONException) {
                UIUtils.showToast(this, "Please make sure that your json payload is valid")
                return false
            }
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun save(id: UUID?) {
        job.jobName = jobNameEdittext.text.toString()
        job.jobUrl = jobUrlEdittext.text.toString()
        job.jobCron = jobCronEdittext.text.toString()
        job.jobMethod = when (radioGroup.checkedRadioButtonId) {
            R.id.radio1 -> "GET"
            R.id.radio2 -> "POST"
            R.id.radio3 -> "PUT"
            R.id.radio4 -> "DELETE"
            else -> "GET"
        }
        job.jobParams = jobParamsTextinput.text.toString()
        job.jobNotifyError = if (jobNotifyErrorSwitch.isChecked) 1 else 0
        job.jobNotifySuccess = if (jobNotifySuccessSwitch.isChecked) 1 else 0
        job.jobNextRun = ScheduleUtils.calcNextRun(job)

        if (!jobPausedSwitch.isChecked) { job.jobPausedAt = null }
        else if (job.jobPausedAt == null) {
            job.jobPausedAt = SimpleDateFormat(ScheduleUtils.dateFormat, Locale.ENGLISH).format(Date())
        }

        if (id == null) {
            job.jobAlarmId = Calendar.getInstance().timeInMillis.toInt()
            Database.addJob(job)
        } else Database.updateJob(job)

        if (job.jobPausedAt == null) ScheduleUtils.scheduleNextJob(this, job)
        UIUtils.showToast(this, "Saved")

        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun download() {
        val now = Date()
        val dateId = SimpleDateFormat(ScheduleUtils.dateFormat, Locale.ENGLISH).format(now)
        val fileName = job.jobName!!.replace(" ".toRegex(), "-") + "_" + dateId + ".txt"
        try {
            val outputStreamWriter = OutputStreamWriter(openFileOutput(fileName, MODE_PRIVATE))
            outputStreamWriter.write(job.jobResult)
            outputStreamWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
            UIUtils.showToast(this, "Unable to download file, please contact the app creator.")
            return
        }
        val intent = Intent(Intent.ACTION_VIEW)
        val file = File(this.filesDir.toString() + "/" + fileName)
        val uri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider",
            file
        )
        intent.setDataAndType(uri, "text/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val title = "${job.jobName} — File downloaded"
        val msg = "Click to view file"
        NotificationUtils.sendNotification(this, intent, title, msg)
        UIUtils.showToast(this, "File downloaded")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return UIUtils.handleMenuOptions(this, item)
    }
}
