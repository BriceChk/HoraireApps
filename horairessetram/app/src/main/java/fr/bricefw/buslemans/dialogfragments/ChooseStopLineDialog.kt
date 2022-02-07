package fr.bricefw.buslemans.dialogfragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.os.AsyncTask
import android.os.Bundle
import fr.bricefw.buslemans.Timeo
import org.xml.sax.SAXException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class ChooseStopLineDialog : DialogFragment() {
    override fun onCreateDialog(saved: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val arret = arguments.getString("arret")
        val refsEtLignes = arguments.getStringArrayList("refs")
        val lignes = ArrayList<String>()
        val refs = ArrayList<String>()
        val nomsLignes = ArrayList<String>()

        for (s in refsEtLignes) {
            refs.add(s.split("%")[0])
            lignes.add(s.split("%")[1])
        }

        if (refs.size == 1) {
            this.dismiss()
            TimeoDataTask(refs[0], fragmentManager).execute()
        }

        // Récupération des noms des lignes à proposer
        for (ligne in lignes) {
            val reader = BufferedReader(InputStreamReader(activity.assets.open("lignes.txt")))
            reader.forEachLine {
                if (ligne == it.split(" : ")[1]) {
                    nomsLignes.add(it.split(" : ")[0])
                }
            }
            reader.close()
        }

        builder.setTitle(arret)
        val options = nomsLignes.toTypedArray()
        builder.setItems(options) { _, which ->
            TimeoDataTask(refs[which], fragmentManager).execute()
        }

        return builder.create()
    }

    @SuppressLint("StaticFieldLeak")
    inner class TimeoDataTask(private val ref: String, private val manager: FragmentManager) : AsyncTask<String, Void, String>() {
        private val dialog = ProgressDialog()
        private var arret = "Erreur"

        override fun onPreExecute() {
            dialog.show(manager, "progress")
        }

        override fun doInBackground(vararg strings: String): String {
            try {
                val passage = Timeo.getPassages(ref)
                arret = passage[0].nomArret
                return passage[0].tempStopFormat()
            } catch (e: IOException) {
            } catch (e: SAXException) {
            } catch (e: NullPointerException) {
            } catch (e: IndexOutOfBoundsException) {
            }
            return "Problème de communication, Timeo n'est peut être pas disponible. Essayez d'actualiser."
        }

        override fun onPostExecute(message: String) {
            dialog.dismiss()
            val fragment = TempStopFragment()
            val bundle = Bundle()
            bundle.putString("ref", ref)
            bundle.putString("arret", arret)
            bundle.putString("message", message)
            fragment.arguments = bundle
            fragment.show(manager, "tempStop")
        }
    }
}
