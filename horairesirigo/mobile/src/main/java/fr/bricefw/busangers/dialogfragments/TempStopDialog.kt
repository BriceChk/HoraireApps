package fr.bricefw.busangers.dialogfragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import fr.bricefw.busangers.activities.MainActivity
import fr.bricefw.busangers.R

class TempStopDialog : DialogFragment() {
    override fun onCreateDialog(saved: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val code = arguments!!.getString("code")!!
        val arret = arguments!!.getString("arret")
        val contenu = arguments!!.getString("contenu")

        val connMgr = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo

        if (networkInfo == null || !networkInfo.isConnected) {
            builder.setTitle("Pas de connexion")
            builder.setMessage("Vérifiez la connexion au WiFi ou au réseau mobile.")
            builder.setNegativeButton("Fermer", null)
            return builder.create()
        }

        builder.setTitle(arret)
        builder.setMessage(contenu)
        builder.setNegativeButton("Fermer", null)
        builder.setPositiveButton("Ajouter cet arrêt") { _, _ ->
            val fragment = AddStopDialog()
            val bundle = Bundle()
            bundle.putString("code", code)
            fragment.arguments = bundle
            fragment.show(requireFragmentManager(), "addStop")
        }

        return builder.create()
    }
}
