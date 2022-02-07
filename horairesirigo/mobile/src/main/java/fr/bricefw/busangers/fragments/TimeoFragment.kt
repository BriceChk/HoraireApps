package fr.bricefw.busangers.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.bricefw.busangers.PassagesArret
import fr.bricefw.busangers.R
import fr.bricefw.busangers.SimpleItemTouchHelperCallback
import fr.bricefw.busangers.Timeo
import fr.bricefw.busangers.activities.MainActivity
import fr.bricefw.busangers.adapters.TimeoRecyclerViewAdapter
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
    private var adapter: TimeoRecyclerViewAdapter? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private var userStops: ArrayList<String> = ArrayList()
    private var task: TimeoDataTask? = null
    private val handler: Handler = Handler()
    private val runnable: Runnable = Runnable {
        updateData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_timeo, container, false)
        val llm = LinearLayoutManager(activity?.applicationContext)
        val rv = rootView.findViewById<RecyclerView>(R.id.timeo_rv)
        rv.setHasFixedSize(true)
        rv.layoutManager = llm

        adapter = TimeoRecyclerViewAdapter(activity!!)
        val callback = SimpleItemTouchHelperCallback(adapter!!)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rv)
        rv.adapter = adapter

        refreshLayout = rootView.findViewById(R.id.timeo_swiperefresh)
        refreshLayout!!.setColorSchemeResources(R.color.colorPrimary)
        refreshLayout!!.setOnRefreshListener { updateData() }

        handler.postDelayed(runnable, 60000)
        updateData()
        return rootView
    }

    override fun onPause() {
        handler.removeCallbacks(runnable)
        if (task != null && !task!!.isCancelled) {
            task!!.cancel(false)
        }
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
        userStops = MainActivity.getUserStops(activity!!.applicationContext)

        val connMgr = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            if (userStops.size > 0) {
                task = TimeoDataTask()
                task!!.execute()
            } else {
                adapter!!.items = arrayListOf(PassagesArret(activity!!, erreur = "no_card"))
                refreshLayout!!.isRefreshing = false
            }
        } else {
            adapter!!.items = arrayListOf(PassagesArret(activity!!, erreur = "no_internet"))
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
                    doc = dBuilder.parse(URL("http://irigo.bricechk.fr/irigo-info.xml").openStream())
                    doc.documentElement.normalize()
                    if (doc.getElementsByTagName("erreur").item(0).textContent == "1") {
                        val pInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
                        val versionName = pInfo.versionName
                        val version = doc.getElementsByTagName("versions").item(0).textContent
                        if (version == "all" || version.contains(versionName)) {
                            val message = doc.getElementsByTagName("message-erreur").item(0).textContent
                            passages.add(PassagesArret(activity!!, messageDev =  message, erreur = "Message du développeur"))
                        }
                    }
                } catch (e: DOMException) {
                    e.printStackTrace()
                }
                val passageArrets = userStops.map { PassagesArret(activity!!, it) }
                passages.addAll(Timeo.getPassages(passageArrets))
                return "success"
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SAXException) {
                e.printStackTrace()
            } catch (e: KotlinNullPointerException) {
                e.printStackTrace()
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return "fail"
        }

        override fun onPostExecute(result: String) {
            if (adapter != null && activity != null) {
                if (result == "success") {
                    adapter!!.items = passages
                    refreshLayout!!.isRefreshing = false
                } else {
                    adapter!!.items = arrayListOf(PassagesArret(activity!!, erreur = "failed_fetch"))
                    refreshLayout!!.isRefreshing = false
                }
            }
        }
    }
}
