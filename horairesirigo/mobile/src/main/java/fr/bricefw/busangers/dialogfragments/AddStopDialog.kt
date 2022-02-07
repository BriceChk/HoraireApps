package fr.bricefw.busangers.dialogfragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import fr.bricefw.busangers.PassagesArret
import fr.bricefw.busangers.R
import fr.bricefw.busangers.activities.MainActivity

class AddStopDialog : DialogFragment() {
    override fun onCreateDialog(saved: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val codeOriginal = arguments!!.getString("code")!!
        val code = codeOriginal.substring(0, codeOriginal.length - 2)
        val passageArret = PassagesArret(activity as Activity, codeOriginal)

        builder.setTitle("Ajouter l'arrêt " + passageArret.nomArret)
        builder.setNegativeButton("Annuler", null)

        val stops = MainActivity.getUserStops(requireContext())

        val listOptions = ArrayList<String>()
        listOptions.add("Ligne " + passageArret.ligne + " > " + passageArret.destinationAller)
        listOptions.add("Ligne " + passageArret.ligne + " > " + passageArret.destinationRetour)
        listOptions.add("Les deux destinations")

        val options = listOptions.toTypedArray()
        builder.setItems(options) { _, which ->
            var message = "Arrêt déjà dans votre liste !"
            if (!stops.contains("$code|$which")) {
                stops.add("$code|$which")
                MainActivity.setUserStops(stops, requireContext())
                message = "Arrêt ajouté"
            }
            if (activity is MainActivity) {
                Snackbar.make(requireActivity().findViewById(R.id.container), message, Snackbar.LENGTH_SHORT).show()
                (activity as MainActivity).updateTimeoData()
            } else {
                Snackbar.make(requireActivity().findViewById(R.id.rv_choose_linestop), message, Snackbar.LENGTH_SHORT).show()
            }
        }

        return builder.create()
    }
}