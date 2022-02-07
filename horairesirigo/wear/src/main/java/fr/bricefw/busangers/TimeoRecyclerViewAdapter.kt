package fr.bricefw.busangers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

internal class RVStopCardAdapter : RecyclerView.Adapter<RVStopCardAdapter.ArretViewHolder>() {
    private var _items = ArrayList<PassagesArret>()

    var items: ArrayList<PassagesArret>
        get() = this._items
        set(list) {
            _items = list
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArretViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.timeo_card, viewGroup, false)
        return ArretViewHolder(v)
    }

    override fun onBindViewHolder(holder: ArretViewHolder, i: Int) {
        if (items[i].erreur == "") {
            holder.titre.text = items[i].nomArret
            holder.contenu.text = items[i].timeoCardFormat()
        } else {
            holder.titre.text = items[i].erreur
            holder.contenu.text = items[i].contenuErreur
        }
    }

    internal class ArretViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titre: TextView = itemView.findViewById(R.id.cv_title)
        val contenu: TextView = itemView.findViewById(R.id.cv_content)
    }
}
