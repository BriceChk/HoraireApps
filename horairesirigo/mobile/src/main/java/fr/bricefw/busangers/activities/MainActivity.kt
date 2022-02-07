package fr.bricefw.busangers.activities

import android.content.Context
import android.content.Intent
import android.database.MatrixCursor
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.BaseColumns
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.material.tabs.TabLayout
import fr.bricefw.busangers.R
import fr.bricefw.busangers.dialogfragments.ChooseLineDialog
import fr.bricefw.busangers.fragments.ItineraireFragment
import fr.bricefw.busangers.fragments.TimeoFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    /**
     * The [androidx.viewpager.widget.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.fragment.app.FragmentStatePagerAdapter.
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        fab.setOnClickListener {
            val intent = Intent(applicationContext, ChooseLineActivity::class.java)
            startActivity(intent)
        }

        // Mise à jour version de config
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = prefs.edit()
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val version = pInfo.versionName
        editor.putString("prefs_version", version)
        editor.apply()
    }

    fun updateTimeoData() {
        val page = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.container + ":" + container.currentItem)
        if (container.currentItem == 0 && page != null) {
            (page as TimeoFragment).updateData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Bouton recherche
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = " Nom d'arrêt, code Timeo"

        searchView.suggestionsAdapter = SimpleCursorAdapter(applicationContext, R.layout.suggestion, null, arrayOf("nomArret"), intArrayOf(R.id.suggestion_text), 0)
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val ca = searchView.suggestionsAdapter
                val cursor = ca.cursor
                cursor.moveToPosition(position)
                val arret = cursor.getString(cursor.getColumnIndex("nomArret"))
                val codes = ArrayList<String>()

                val readerLigne = BufferedReader(InputStreamReader(assets.open("lignes.txt")))
                readerLigne.forEachLine { lineString: String ->
                    val line = lineString.split("|")[0]
                    val reader = BufferedReader(InputStreamReader(assets.open("$line.txt")))
                    reader.forEachLine arrets@{ arretString: String ->
                        val timeo1 = arretString.split("|")[0]
                        val nomArret = arretString.split("|")[2]
                        val code = "$line|$timeo1|2"
                        if (nomArret == arret) {
                            codes.add(code)
                            return@arrets
                        }
                    }
                }

                searchView.onActionViewCollapsed()
                val fragment = ChooseLineDialog()
                val bundle = Bundle()
                bundle.putString("arret", arret)
                bundle.putStringArrayList("codes", codes)
                fragment.arguments = bundle
                fragment.show(supportFragmentManager, "chooseLine")
                return true
            }

            override fun onSuggestionSelect(position: Int): Boolean {
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                val mc = MatrixCursor(arrayOf(BaseColumns._ID, "nomArret"))
                try {
                    val arretsAjoutes = ArrayList<String>()
                    var i = 0
                    val readerLigne = BufferedReader(InputStreamReader(assets.open("lignes.txt")))
                    readerLigne.forEachLine {lineString: String ->
                        val line = lineString.split("|")[0]
                        val reader = BufferedReader(InputStreamReader(assets.open("$line.txt")))
                        reader.forEachLine {arretString: String ->
                            val timeo1 = arretString.split("|")[0]
                            val timeo2 = arretString.split("|")[1]
                            val nomArret = arretString.split("|")[2]

                            if ((timeo1 == query || timeo2 == query || nomArret.toLowerCase().contains(query.toLowerCase())) && !arretsAjoutes.contains(nomArret)) {
                                mc.addRow(arrayOf(i, nomArret))
                                arretsAjoutes.add(nomArret)
                                i++
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                searchView.suggestionsAdapter.changeCursor(mc)
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_about) {
            val intent = Intent(applicationContext, AboutActivity::class.java)
            startActivity(intent)
            return true
        }

        if (id == R.id.menu_refresh) {
            updateTimeoData()
            return true
        }

        if (id == R.id.menu_sync_wear) {
            val dataClient = Wearable.getDataClient(this)
            val dataMapReq = PutDataMapRequest.create("/get-userstops")
            dataMapReq.dataMap.putStringArrayList("stops", getUserStops(this))
            val putDataReq = dataMapReq.asPutDataRequest()
            putDataReq.setUrgent()
            dataClient.putDataItem(putDataReq)
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            if (position == 0) {
                return TimeoFragment()
            }
            return ItineraireFragment()
        }

        override fun getCount(): Int {
            return 2
        }
    }

    companion object {
        fun setUserStops(value: ArrayList<String>, context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            val size = prefs.getInt("StopsArray_size", 0)
            for (i in 0..size) {
                editor.remove("StopsArray_$i")
            }
            editor.putInt("StopsArray_size", value.size)
            for (i in value.indices) {
                editor.putString("StopsArray_$i", value[i])
            }
            editor.apply()
        }

        fun getUserStops(context: Context): ArrayList<String> {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val size = preferences.getInt("StopsArray_size", 0)
            return (0 until size).mapTo(ArrayList()) { preferences.getString("StopsArray_$it", null) }
        }

        fun getPrefVersion(context: Context): String {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString("prefs_version", "")!!
        }
    }
}
