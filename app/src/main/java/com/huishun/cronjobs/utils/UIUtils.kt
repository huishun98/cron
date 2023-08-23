package com.huishun.cronjobs.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.huishun.cronjobs.R
import com.huishun.cronjobs.SettingsActivity

class UIUtils {
    companion object {
        fun showToolbar(toolbar: Toolbar, activity: AppCompatActivity, title: String, x :Boolean) {
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar?.title = title
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(x)
            activity.supportActionBar?.setDisplayShowHomeEnabled(x)
        }

        fun showToast(context: Context, text: String) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }

        fun handleMenuOptions(activity: Activity, item : MenuItem) : Boolean {
            when (item.itemId) {
                android.R.id.home -> {
                    activity.finish()
                    return true
                }
                R.id.settings_button -> {
                    val intent = Intent(activity, SettingsActivity::class.java)
                    activity.startActivity(intent)
                    return true
                }
            }
            return activity.onOptionsItemSelected(item)
        }
    }
}
