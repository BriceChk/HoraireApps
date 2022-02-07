package fr.bricefw.buslemans.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.TextView
import fr.bricefw.buslemans.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            var version = pInfo.versionName
            val vt = findViewById<TextView>(R.id.textView_version)
            version = "Version : $version"
            vt.text = version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 16908332) {
            this.finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
