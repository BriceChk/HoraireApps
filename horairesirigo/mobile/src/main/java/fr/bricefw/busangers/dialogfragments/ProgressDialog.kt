package fr.bricefw.busangers.dialogfragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

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
