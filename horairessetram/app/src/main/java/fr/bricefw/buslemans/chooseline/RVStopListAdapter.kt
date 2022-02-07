package fr.bricefw.buslemans.chooseline

import android.app.Activity
import android.content.ContextWrapper
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fr.bricefw.buslemans.ListCard
import fr.bricefw.buslemans.R
import fr.bricefw.buslemans.dialogfragments.ChooseStopLineDialog

internal class RVStopListAdapter(private val textCards: List<ListCard>) : RecyclerView.Adapter<RVStopListAdapter.TextViewHolder>() {

    override fun getItemCount(): Int {
        return textCards.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): TextViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.stop_list_card, viewGroup, false)
        return TextViewHolder(v)
    }

    override fun onBindViewHolder(holder: TextViewHolder, i: Int) {
        holder.stopName.text = textCards[i].titre
        holder.stopCode.text = textCards[i].texte
        holder.code = textCards[i].code
    }

    internal class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var stopName: TextView = itemView.findViewById(R.id.stop_list_text)
        var stopCode: TextView = itemView.findViewById(R.id.stop_list_subtext)
        var code: String = ""

        init {
            val cv = itemView.findViewById<CardView>(R.id.stop_list)
            cv.setOnClickListener(View.OnClickListener { v ->
                val fragment = ChooseStopLineDialog()
                val bundle = Bundle()
                val refs = ArrayList<String>()
                refs.add("$code%non")
                bundle.putString("arret", stopName.text.toString())
                bundle.putStringArrayList("refs", refs)
                fragment.arguments = bundle

                var activity: Activity? = null
                var context = v.context
                while (context is ContextWrapper) {
                    if (context is Activity) {
                        activity = context
                    }
                    context = context.baseContext
                }
                if (activity == null) {
                    return@OnClickListener
                }
                fragment.show(activity.fragmentManager, "chooseLine")
            })
        }
    }
}
