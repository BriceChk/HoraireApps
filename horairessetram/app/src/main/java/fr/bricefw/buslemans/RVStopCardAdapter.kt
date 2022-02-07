package fr.bricefw.buslemans

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import fr.bricefw.buslemans.dialogfragments.ChooseStopLineDialog
import fr.bricefw.buslemans.dialogfragments.InfoDialog
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

internal class RVStopCardAdapter(private val activity: FragmentActivity) : RecyclerView.Adapter<RVStopCardAdapter.ArretViewHolder>(), ItemTouchHelperAdapter {
    private var _items = ArrayList<PassagesArret>()

    var items: ArrayList<PassagesArret>
        get() = this._items
        set(list) {
            _items = list
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArretViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.main_card, viewGroup, false)
        return ArretViewHolder(v)
    }

    override fun onBindViewHolder(holder: ArretViewHolder, i: Int) {
        val card = items[i]
        holder.arret.text = items[i].nomArret
        if (card.erreur) {
            holder.ligne.text = card.numeroLigne
            holder.passages.height = 0
            return
        }
        holder.ligne.text = items[i].ligne()
        holder.passages.text = items[i].oneLinePassages()
        if (card.hasMessage()) {
            holder.warningButton.visibility = View.VISIBLE
            holder.warningButton.setOnClickListener {
                val fragment = InfoDialog()
                val bundle = Bundle()
                val message = StringBuilder()

                card.messages.forEach {
                    message.append(it.titre)
                            .append("\n")
                            .append(it.texte)
                            .append("\n\n")
                }

                if (message.isNotEmpty()) {
                    message.delete(message.length - 2 , message.length)
                }

                bundle.putString("message", message.toString())
                fragment.arguments = bundle
                fragment.show(activity.fragmentManager, "infoFragment")
            }
        }
        holder.card.setOnClickListener {
            val refsEtLignes = ArrayList<String>()
            val reader = BufferedReader(InputStreamReader(activity.assets.open("lignes.txt")))
            reader.forEachLine {
                val line = it.split(" : ")[1]
                val reader2 = BufferedReader(InputStreamReader(activity.assets.open("$line.txt")))
                reader2.forEachLine {
                    if (it.split(" : ")[1] == card.nomArret) {
                        refsEtLignes.add(it.split(" : ")[0].split("_")[0] + "%" + line)
                    }
                }
            }
            if (refsEtLignes.size > 1) {
                val fragment = ChooseStopLineDialog()
                val bundle = Bundle()
                bundle.putString("arret", card.nomArret)
                bundle.putStringArrayList("refs", refsEtLignes)
                fragment.arguments = bundle
                fragment.show(activity.fragmentManager, "chooseLine")
            }
        }
    }

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

    internal class ArretViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var arret: TextView = itemView.findViewById(R.id.cv_first_line)
        var ligne: TextView = itemView.findViewById(R.id.cv_second_line)
        var passages: TextView = itemView.findViewById(R.id.cv_third_line)
        var warningButton: ImageButton = itemView.findViewById(R.id.warningButton)
        var card: CardView = itemView.findViewById(R.id.cv_text)
    }
}
