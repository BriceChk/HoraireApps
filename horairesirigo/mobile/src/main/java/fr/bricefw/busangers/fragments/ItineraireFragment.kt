package fr.bricefw.busangers.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

import fr.bricefw.busangers.R

class ItineraireFragment : Fragment() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_itineraire, container, false)

        val swipeRefresh = rootView.findViewById<SwipeRefreshLayout>(R.id.itineraire_swiperefresh)
        swipeRefresh.isEnabled = false
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)

        val myWebView = rootView.findViewById<WebView>(R.id.webview)
        myWebView!!.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                swipeRefresh.isRefreshing = false

                // JS pour virer les cartes
                myWebView.loadUrl("javascript:(function() {" +
                        "var maps = document.getElementsByClassName('ctp-map');" +
                        "for (var i = 0; i < maps.length; i++) {" +
                        "   maps.item(i).parentElement.style.display = 'none';" +
                        "}" +
                        "})()")
                super.onPageFinished(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                swipeRefresh.isRefreshing = true
                super.onPageStarted(view, url, favicon)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                view!!.loadUrl("file:///android_asset/pb_com.html")
                super.onReceivedError(view, request, error)
            }
        }

        val webSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.setAppCacheEnabled(true)
        webSettings.databaseEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.setGeolocationEnabled(true)

        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                // Demander permission de loc android
                if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 42)
                }
                callback!!.invoke(origin, true, false)
            }
        }

        myWebView.loadUrl("https://server-url/fr/load/dDz2Gi0j/")
        return rootView
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
            fab?.hide()
            activity?.findViewById<AppBarLayout>(R.id.appbar)?.setExpanded(false)
        }
    }
}

