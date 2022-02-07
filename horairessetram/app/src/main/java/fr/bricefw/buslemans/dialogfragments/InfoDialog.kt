package fr.bricefw.buslemans.dialogfragments

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle

class InfoDialog : DialogFragment() {
    override fun onCreateDialog(saved: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val message = arguments.getString("message")
        builder.setTitle("Info trafic")
        builder.setMessage(message)
        builder.setNegativeButton("Fermer", null)
        return builder.create()
    }
}
