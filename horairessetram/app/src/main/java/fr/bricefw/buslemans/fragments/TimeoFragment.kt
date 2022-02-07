package fr.bricefw.buslemans.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.bricefw.buslemans.*
import fr.bricefw.buslemans.activities.MainActivity
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.xml.sax.SAXException
import java.io.IOException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

/**
* Created by brice on 08/08/2017.
*/

class TimeoFragment : Fragment() {
    private var adapter: RVStopCardAdapter? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private var userStops: ArrayList<String> = ArrayList()
    private var task: TimeoDataTask? = null
    private val handler: Handler = Handler()
    private val runnable: Runnable = Runnable {
        updateData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_timeo, container, false)

        val llm = LinearLayoutManager(activity)
        val rv = rootView.findViewById<RecyclerView>(R.id.timeo_rv)
        rv.setHasFixedSize(true)
        rv.layoutManager = llm

        adapter = RVStopCardAdapter(activity!!)
        val callback = SimpleItemTouchHelperCallback(adapter!!)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rv)
        rv.adapter = adapter

        refreshLayout = rootView.findViewById(R.id.timeo_swiperefresh)
        refreshLayout!!.setColorSchemeResources(R.color.colorAccent)
        refreshLayout!!.setOnRefreshListener { updateData() }

        handler.postDelayed(runnable, 60000)
        updateData()
        return rootView
    }

    override fun onPause() {
        handler.removeCallbacks(runnable)
        super.onPause()
    }

    override fun onResume() {
        handler.postDelayed(runnable, 60000)
        updateData()
        super.onResume()
    }

    fun updateData() {
        if (task != null && !task!!.isCancelled) {
            task!!.cancel(false)
        }
        refreshLayout!!.isRefreshing = true
        userStops = MainActivity.getUserStops(activity!!)

        val connMgr = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            if (userStops.size > 0) {
                task = TimeoDataTask()
                task!!.execute()
            } else {
                val cards = ArrayList<PassagesArret>()
                cards.add(PassagesArret("C'est vide ici !", "Ajoutez vos arrêts favoris ou utilisez la recherche rapide.", "", erreur = true))
                adapter!!.items = cards
                refreshLayout!!.isRefreshing = false
            }
        } else {
            val cards = ArrayList<PassagesArret>()
            cards.add(PassagesArret("Pas d'accès à internet", "Vérifiez la connexion au WiFi ou au réseau mobile.", "", erreur = true))
            adapter!!.items = cards
            refreshLayout!!.isRefreshing = false
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (activity != null && isVisibleToUser) {
            val fab = activity!!.findViewById<FloatingActionButton>(R.id.fab)
            fab.show()
            activity!!.findViewById<AppBarLayout>(R.id.appbar).setExpanded(true)
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class TimeoDataTask : AsyncTask<String, Void, String>() {
        private var passages : ArrayList<PassagesArret> = ArrayList()

        override fun doInBackground(vararg args: String): String {
            try {
                val dbFactory = DocumentBuilderFactory.newInstance()
                val dBuilder = dbFactory.newDocumentBuilder()
                val doc: Document
                try {
                    doc = dBuilder.parse(URL("https://horaires-setram.bricechk.fr/setram-info.xml").openStream())
                    doc.documentElement.normalize()
                    if (doc.getElementsByTagName("erreur").item(0).textContent == "1") {
                        val message = doc.getElementsByTagName("message-erreur").item(0).textContent
                        passages.add(PassagesArret("Message du développeur", message, "", erreur = true))
                    }
                } catch (e: DOMException) {
                    e.printStackTrace()
                }

                passages.addAll(Timeo.getPassages(userStops))
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SAXException) {
                e.printStackTrace()
            }

            return ""
        }

        override fun onPostExecute(result: String) {
            if (passages.isEmpty()) {
                passages.add(PassagesArret("Problème de communication", "Timeo n'est peut être pas disponible. Essayez d'actualiser.", "", erreur = true))
            }
            adapter!!.items = passages
            refreshLayout!!.isRefreshing = false
        }
    }
}
