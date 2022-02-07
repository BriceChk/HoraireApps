package fr.bricefw.buslemans.activities

import android.content.Context
import android.content.Intent
import android.database.MatrixCursor
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.BaseColumns
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.widget.SimpleCursorAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import fr.bricefw.buslemans.R
import fr.bricefw.buslemans.chooseline.ChooseLineActivity
import fr.bricefw.buslemans.dialogfragments.ChooseStopLineDialog
import fr.bricefw.buslemans.fragments.ItineraireFragment
import fr.bricefw.buslemans.fragments.TimeoFragment
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    /**
     * The [ViewPager] that will host the section contents.
     */
    private var mViewPager: ViewPager? = null
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container)
        mViewPager!!.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tabs)

        mViewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(mViewPager))

        // Affichage du bouton "+" pour ajouter un arrêt
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(applicationContext, ChooseLineActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Bouton recherche
        searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView!!.queryHint = " Nom d'arrêt, code Timeo"

        // Lors d'un clic sur une suggestion
        searchView!!.suggestionsAdapter = SimpleCursorAdapter(applicationContext, R.layout.suggestion, null, arrayOf("stopName"), intArrayOf(android.R.id.text1), 0)
        searchView!!.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val ca = searchView!!.suggestionsAdapter
                val cursor = ca.cursor
                cursor.moveToPosition(position)
                val arret = cursor.getString(cursor.getColumnIndex("stopName"))
                val refsEtLignes = ArrayList<String>()
                val readerLines = BufferedReader(InputStreamReader(assets.open("lignes.txt")))
                val texteLignes = readerLines.readText()
                readerLines.close()
                try {
                    val files = assets.list("")
                    for (s in files) {
                        if (!s.contains(".txt")) {
                            continue
                        }
                        if (texteLignes.contains(s.replace(".txt", ""))) {
                            val reader = BufferedReader(InputStreamReader(assets.open(s)))
                            reader.forEachLine {
                                if (arret == it.split(" : ")[1]) {
                                    refsEtLignes.add(it.split(" : ")[0].split("_")[0] + "%" + s.replace(".txt", ""))
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                searchView!!.onActionViewCollapsed()
                if (arret != "") {
                    val fragment = ChooseStopLineDialog()
                    val bundle = Bundle()
                    bundle.putString("arret", arret)
                    bundle.putStringArrayList("refs", refsEtLignes)
                    fragment.arguments = bundle
                    fragment.show(fragmentManager, "chooseLine")
                } else {
                    Snackbar.make(findViewById(R.id.container), "Aucun arrêt trouvé.", Snackbar.LENGTH_LONG).show()
                }
                return true
            }

            override fun onSuggestionSelect(position: Int): Boolean {
                return true
            }
        })

        // Affichage des suggestions pendant la recherche
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                val mc = MatrixCursor(arrayOf(BaseColumns._ID, "stopName"))
                try {
                    val files = assets.list("")
                    val arretsAjoutes = ArrayList<String>()
                    val readerLines = BufferedReader(InputStreamReader(assets.open("lignes.txt")))
                    val texteLignes = readerLines.readText()
                    readerLines.close()
                    for (s in files) {
                        if (!s.contains(".txt")) {
                            continue
                        }
                        if (texteLignes.contains(s.replace(".txt", ""))) {
                            val reader = BufferedReader(InputStreamReader(assets.open(s)))
                            var i = 0
                            reader.forEachLine {
                                if (it.split(" : ")[0].split("_")[1] == query || it.split(" : ")[1].toLowerCase().contains(query.toLowerCase())) {
                                    val arret = it.split(" : ")[1]
                                    if (!arretsAjoutes.contains(arret)) {
                                        mc.addRow(arrayOf(i, arret))
                                        arretsAjoutes.add(arret)
                                    }
                                }
                                i++
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                searchView!!.suggestionsAdapter.changeCursor(mc)
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
/*
        if (id == R.id.menu_sync_wear) {
            val dataClient = Wearable.getDataClient(this)
            val dataMapReq = PutDataMapRequest.create("/get-userstops")
            dataMapReq.dataMap.putStringArrayList("stops", getUserStops(this))
            val putDataReq = dataMapReq.asPutDataRequest()
            putDataReq.setUrgent()
            dataClient.putDataItem(putDataReq)
            return true
        }
*/
        return super.onOptionsItemSelected(item)
    }

    // Fonction appelée par le SimpleItemTouchHelperCallback pour mettre à jour
    fun updateTimeoData() {
        val page = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager!!.currentItem)
        if (mViewPager!!.currentItem == 0 && page != null) {
            (page as TimeoFragment).updateData()
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return TimeoFragment()
            }
            return ItineraireFragment()
        }

        override fun getCount(): Int {
            // Show 2 total pages.
            return 2
        }
    }

    companion object {
        fun setUserStops(value: ArrayList<String>, context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            editor.clear()
            editor.putInt("StopsArray_size", value.size)
            for (i in value.indices) {
                editor.remove("StopsArray_$i")
                editor.putString("StopsArray_$i", value[i])
            }
            editor.apply()
        }

        fun getUserStops(context: Context): ArrayList<String> {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val size = preferences.getInt("StopsArray_size", 0)
            return (0 until size).mapTo(ArrayList()) { preferences.getString("StopsArray_$it", null) }
        }
    }
}
