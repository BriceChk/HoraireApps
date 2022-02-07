package fr.bricefw.buslemans.dialogfragments

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle

class ProgressDialog : DialogFragment() {
    override fun onCreateDialog(saved: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Chargement ...")
        val alert = builder.create()
        isCancelable = false
        alert.setCanceledOnTouchOutside(false)
        return alert
    }
}
