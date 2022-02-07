package fr.bricefw.buslemans.chooseline

import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fr.bricefw.buslemans.ListCard

import fr.bricefw.buslemans.R

internal class RVLineListAdapter(private val textCards: List<ListCard>) : RecyclerView.Adapter<RVLineListAdapter.TextViewHolder>() {

    override fun getItemCount() = textCards.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): TextViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.line_list_card, viewGroup, false)
        return TextViewHolder(v)
    }

    override fun onBindViewHolder(holder: TextViewHolder, i: Int) {
        holder.text.text = textCards[i].titre
        holder.ligneCode = textCards[i].code
    }

    internal class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var text: TextView = itemView.findViewById(R.id.line_list_text)
        var ligneCode: String = ""

        init {
            val cv = itemView.findViewById<CardView>(R.id.line_list)
            cv.setOnClickListener { v ->
                val intent = Intent(v.context, ChooseStopActivity::class.java)
                intent.putExtra("code", ligneCode)
                v.context.startActivity(intent)
            }
        }
    }
}
