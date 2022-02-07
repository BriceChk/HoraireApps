package fr.bricefw.buslemans.dialogfragments

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import fr.bricefw.buslemans.activities.MainActivity
import fr.bricefw.buslemans.R

class TempStopFragment : DialogFragment() {
    override fun onCreateDialog(saved: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val ref = arguments.getString("ref")
        val arret = arguments.getString("arret")
        val message = arguments.getString("message")

        val connMgr = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo

        if (networkInfo == null || !networkInfo.isConnected) {
            builder.setTitle("Pas de connexion")
            builder.setMessage("Vérifiez la connexion au WiFi ou au réseau mobile.")
            builder.setNegativeButton("Fermer", null)
            return builder.create()
        }

        builder.setTitle(arret)
        builder.setMessage(message)
        builder.setNegativeButton("Fermer", null)

        val stops = MainActivity.getUserStops(activity.applicationContext)
        if (!stops.contains(ref)) {
            builder.setPositiveButton("Ajouter cet arrêt") { _, _ ->
                stops.add(ref)
                MainActivity.setUserStops(stops, activity.applicationContext)
                if (activity is MainActivity) {
                    Snackbar.make(activity.findViewById(R.id.container), "Arrêt ajouté", Snackbar.LENGTH_SHORT).show()
                    (activity as MainActivity).updateTimeoData()
                } else {
                    Snackbar.make(activity.findViewById(R.id.rv_choose_linestop), "Arrêt ajouté", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        return builder.create()
    }
}
