package fr.bricefw.busangers.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import fr.bricefw.busangers.ListCard

import fr.bricefw.busangers.R
import fr.bricefw.busangers.activities.ChooseStopActivity

internal class LineRecyclerViewAdapter(private val textCards: List<ListCard>) : RecyclerView.Adapter<LineRecyclerViewAdapter.TextViewHolder>() {

    override fun getItemCount() = textCards.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): TextViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.line_list_card, viewGroup, false)
        return TextViewHolder(v)
    }

    override fun onBindViewHolder(holder: TextViewHolder, i: Int) {
        holder.text.text = textCards[i].titre
        holder.ligne = textCards[i].code
    }

    internal class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var text: TextView = itemView.findViewById(R.id.line_list_text)
        var ligne: String = ""

        init {
            val cv = itemView.findViewById<CardView>(R.id.cv_line_list)
            cv.setOnClickListener { v ->
                val intent = Intent(v.context, ChooseStopActivity::class.java)
                intent.putExtra("code", ligne)
                v.context.startActivity(intent)
            }
        }
    }
}
