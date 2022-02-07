package fr.bricefw.busangers.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Reset des donnée si ancienne version, pour nouvelle version
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName

            if (MainActivity.getPrefVersion(applicationContext) != version) {
                MainActivity.setUserStops(ArrayList(), applicationContext)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
