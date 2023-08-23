package com.huishun.cronjobs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.huishun.cronjobs.utils.UIUtils

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        UIUtils.showToolbar(findViewById(R.id.toolbar), this, "", true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return UIUtils.handleMenuOptions(this, item)
    }
}
