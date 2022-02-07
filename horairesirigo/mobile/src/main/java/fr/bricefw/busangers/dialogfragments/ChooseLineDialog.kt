package fr.bricefw.busangers.dialogfragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import fr.bricefw.busangers.Timeo
import java.io.BufferedReader
import java.io.InputStreamReader

class ChooseLineDialog : DialogFragment() {
    override fun onCreateDialog(saved: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val arret = arguments!!.getString("arret")
        val codes = arguments!!.getStringArrayList("codes")!!
        val nomsLignes = ArrayList<String>()

        if (codes.size == 1) {
            this.dismiss()
            TimeoDataTask(codes[0], requireFragmentManager()).execute()
        }

        // Récupération des noms des lignes à proposer
        for (code in codes) {
            val reader = BufferedReader(InputStreamReader(requireActivity().assets.open("lignes.txt")))
            reader.forEachLine {
                if (code.split("|")[0] == it.split("|")[0]) {
                    nomsLignes.add("Ligne " + it.split("|")[0] + " : " + it.split("|")[1])
                }
            }
            reader.close()
        }

        builder.setTitle(arret)
        val options = nomsLignes.toTypedArray()
        builder.setItems(options) { _, which ->
            TimeoDataTask(codes[which], requireFragmentManager()).execute()
        }
        return builder.create()
    }

    @SuppressLint("StaticFieldLeak")
    inner class TimeoDataTask(private val code: String, private val manager: FragmentManager) : AsyncTask<String, Void, String>() {
        private val dialog = ProgressDialog()
        private var arret = "Erreur"

        override fun onPreExecute() {
            dialog.show(manager, "progress")
        }

        override fun doInBackground(vararg strings: String): String {
            // Ne doit retourner qu'un passage
            val passages = Timeo.getPassages(activity!!, code)
            // On check quand même, parfois ça marche pas
            if (passages.isNotEmpty()) {
                arret = passages[0].nomArret
                return passages[0].tempStopFormat()
            }
            return "Problème de communication, Timeo n'est peut être pas disponible. Essayez d'actualiser."
        }

        override fun onPostExecute(contenu: String) {
            dialog.dismiss()
            val fragment = TempStopDialog()
            val bundle = Bundle()
            bundle.putString("code", code)
            bundle.putString("arret", arret)
            bundle.putString("contenu", contenu)
            fragment.arguments = bundle
            fragment.show(manager, "tempStop")
        }
    }
}
