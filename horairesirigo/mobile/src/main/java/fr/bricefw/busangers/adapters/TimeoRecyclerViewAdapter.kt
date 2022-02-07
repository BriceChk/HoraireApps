package fr.bricefw.busangers.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import fr.bricefw.busangers.ItemTouchHelperAdapter
import fr.bricefw.busangers.PassagesArret
import fr.bricefw.busangers.R
import fr.bricefw.busangers.dialogfragments.ChooseLineDialog
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class TimeoRecyclerViewAdapter(private val activity: FragmentActivity)
    : RecyclerView.Adapter<TimeoRecyclerViewAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    private var _items = ArrayList<PassagesArret>()

    var items: ArrayList<PassagesArret>
        get() = this._items
        set(list) {
            _items = list
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.timeo_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val card = items[i]
        with(holder) {
            if (card.erreur == "no_card") {
                titre.text = activity.getString(R.string.no_card_title)
                contenu.text = activity.getString(R.string.no_card_content)
                return
            }
            if (card.erreur == "no_internet") {
                titre.text = activity.getString(R.string.no_internet_title)
                contenu.text = activity.getString(R.string.no_internet_content)
                return
            }
            if (card.erreur == "failed_fetch") {
                titre.text = activity.getString(R.string.error_oops)
                contenu.text = activity.getString(R.string.error_loading)
                return
            }
            if (card.erreur == "Message du développeur") {
                titre.text = activity.getString(R.string.error_devmsg)
                contenu.text = items[i].timeoCardFormat()
                return
            }
            titre.text = items[i].nomArret
            contenu.text = items[i].timeoCardFormat()
        }

        holder.card.setOnClickListener { _ ->
            val codes = ArrayList<String>()
            val reader = BufferedReader(InputStreamReader(activity.assets.open("lignes.txt")))
            reader.forEachLine { s ->
                val line = s.split("|")[0]
                val reader2 = BufferedReader(InputStreamReader(activity.assets.open("$line.txt")))
                reader2.forEachLine {
                    if (it.split("|")[0] == card.timeoAller) {
                        codes.add(line + "|" + it.split("|")[0] + "|2")
                    }
                }
            }
            if (codes.size > 1) {
                val fragment = ChooseLineDialog()
                val bundle = Bundle()
                bundle.putString("arret", card.nomArret)
                bundle.putStringArrayList("codes", codes)
                fragment.arguments = bundle
                fragment.show(activity.supportFragmentManager, "chooseLine")
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun onItemDismissCancel(dismissedItem: PassagesArret, index: Int) {
        items.add(index, dismissedItem)
        notifyItemInserted(index)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titre: TextView = itemView.findViewById(R.id.cv_title)
        var contenu: TextView = itemView.findViewById(R.id.cv_content)
        var card: CardView = itemView.findViewById(R.id.cv_timeo)
    }
}
