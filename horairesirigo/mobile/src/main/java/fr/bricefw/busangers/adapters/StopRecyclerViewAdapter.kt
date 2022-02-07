package fr.bricefw.busangers.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import fr.bricefw.busangers.ListCard
import fr.bricefw.busangers.R
import fr.bricefw.busangers.dialogfragments.ChooseLineDialog

internal class StopRecyclerViewAdapter(private val textCards: List<ListCard>, private val ligne: String, private val activity: AppCompatActivity) : RecyclerView.Adapter<StopRecyclerViewAdapter.TextViewHolder>() {

    override fun getItemCount(): Int {
        return textCards.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): TextViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.stop_list_card, viewGroup, false)
        return TextViewHolder(v, activity)
    }

    override fun onBindViewHolder(holder: TextViewHolder, i: Int) {
        holder.stopName.text = textCards[i].titre
        holder.stopCode.text = textCards[i].texte
        holder.code = textCards[i].code
        holder.ligne = ligne
    }

    internal class TextViewHolder(itemView: View, activity: AppCompatActivity) : RecyclerView.ViewHolder(itemView) {
        var stopName: TextView = itemView.findViewById(R.id.stop_list_text)
        var stopCode: TextView = itemView.findViewById(R.id.stop_list_subtext)
        var code: String = ""
        var ligne: String = ""

        init {
            val cv = itemView.findViewById<CardView>(R.id.cv_stop_list)
            cv.setOnClickListener(View.OnClickListener {
                val fragment = ChooseLineDialog()
                val bundle = Bundle()
                val codes = ArrayList<String>()
                codes.add("$ligne|$code|2")
                bundle.putString("arret", stopName.text.toString())
                bundle.putStringArrayList("codes", codes)
                fragment.arguments = bundle

                fragment.show(activity.supportFragmentManager, "chooseLine")
                return@OnClickListener
            })
        }
    }
}
