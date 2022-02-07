package fr.bricefw.busangers

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.wearable.activity.WearableActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.wear.widget.WearableRecyclerView
import com.google.android.gms.wearable.*
import java.net.URL


class MainActivity : WearableActivity(), DataClient.OnDataChangedListener {
    private var adapter: RVStopCardAdapter = RVStopCardAdapter()
    private var userStops: ArrayList<String> = ArrayList()
    private var refreshLayout: SwipeRefreshLayout? = null
    private var task: TimeoDataTask? = null
    private val handler: Handler = Handler()
    private val runnable: Runnable = Runnable {
        updateData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv = findViewById<WearableRecyclerView>(R.id.rv)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(this)
        rv.isEdgeItemsCenteringEnabled = true
        rv.adapter = adapter

        refreshLayout = findViewById(R.id.swiperefresh)
        refreshLayout!!.setColorSchemeResources(R.color.colorPrimary)
        refreshLayout!!.setOnRefreshListener { updateData() }

        handler.postDelayed(runnable, 60000)

        updateData()
    }

    private fun updateData() {
        if (task != null && !task!!.isCancelled) {
            task!!.cancel(false)
        }
        refreshLayout!!.isRefreshing = true
        userStops = getUserStops()

        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            if (userStops.size > 0) {
                task = TimeoDataTask()
                task!!.execute()
            } else {
                adapter.items = arrayListOf(PassagesArret(contenuErreur = "Synchronisez vos arrêts depuis\nl'application sur téléphone.", erreur = "C'est vide ici !"))
                refreshLayout!!.isRefreshing = false
            }
        } else {
            adapter.items = arrayListOf(PassagesArret(contenuErreur = "Vérifiez votre connexion\nWiFi ou téléphone.", erreur = "Pas de connexion"))
            refreshLayout!!.isRefreshing = false
        }
    }

    public override fun onResume() {
        Wearable.getDataClient(this).addListener(this)
        handler.postDelayed(runnable, 60000)
        updateData()
        super.onResume()
    }

    override fun onPause() {
        Wearable.getDataClient(this).removeListener(this)
        handler.removeCallbacks(runnable)
        super.onPause()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/get-userstops") {
                setUserStops(DataMapItem.fromDataItem(event.dataItem).dataMap.getStringArrayList("stops"))
                updateData()
                break
            }
        }
    }

    private fun setUserStops(list: ArrayList<String>) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        editor.clear()
        editor.putInt("StopsArray_size", list.size)
        for (i in list.indices) {
            editor.remove("StopsArray_$i")
            editor.putString("StopsArray_$i", list[i])
        }
        editor.apply()
    }

    private fun getUserStops(): ArrayList<String> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val size = preferences.getInt("StopsArray_size", 0)
        return (0 until size).mapTo(ArrayList()) { preferences.getString("StopsArray_$it", null) }
    }

    @SuppressLint("StaticFieldLeak")
    inner class TimeoDataTask : AsyncTask<String, Void, String>() {
        private var passages : ArrayList<PassagesArret> = ArrayList()

        override fun doInBackground(vararg args: String): String {
            val param = userStops.joinToString(";")
            val url = URL("http://server-url/irigo-api.php?stops=$param")
            url.readText().lines().forEach {
                passages.add(PassagesArret(it))
            }
            return ""
        }

        override fun onPostExecute(result: String) {
            if (passages.isEmpty()) {
                adapter.items = arrayListOf(PassagesArret(contenuErreur = "Une erreur est survenue, essayer d'actualiser.", erreur = "Oops :("))
            } else {
                adapter.items = passages
            }
            refreshLayout!!.isRefreshing = false
        }
    }

}