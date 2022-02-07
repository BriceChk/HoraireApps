package fr.bricefw.busangers

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fr.bricefw.busangers.activities.MainActivity
import fr.bricefw.busangers.adapters.TimeoRecyclerViewAdapter
import java.util.*

internal class SimpleItemTouchHelperCallback(private val adapter: TimeoRecyclerViewAdapter) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition

        // Si c'est une erreur, osef, on recharge
        if (adapter.items[0].erreur != "no") {
            (viewHolder.itemView.context as MainActivity).updateTimeoData()
            return true
        }

        adapter.onItemMove(fromPosition, toPosition)

        if (adapter.items[fromPosition].erreur != "no") {
            return true
        }

        val userStops = MainActivity.getUserStops(recyclerView.context)
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(userStops, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(userStops, i, i - 1)
            }
        }
        MainActivity.setUserStops(userStops, recyclerView.context)
        return true
    }

    override fun isLongPressDragEnabled() = true

    override fun isItemViewSwipeEnabled() = true

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val i = viewHolder.adapterPosition
        if (adapter.items[0].erreur != "no") {
            (viewHolder.itemView.context as MainActivity).updateTimeoData()
            return
        }
        val dismissedItem = adapter.items[i]
        adapter.onItemDismiss(i)
        val userStops = MainActivity.getUserStops(viewHolder.itemView.context)
        val stopToRemove = userStops[i]
        userStops.removeAt(i)
        MainActivity.setUserStops(userStops, viewHolder.itemView.context)
        val bar = Snackbar.make((viewHolder.itemView.context as MainActivity).findViewById(R.id.timeo_rv), "Arrêt supprimé", Snackbar.LENGTH_LONG)
        bar.setAction("Annuler") {
            bar.dismiss()
            userStops.add(i, stopToRemove)
            MainActivity.setUserStops(userStops, viewHolder.itemView.context)
            adapter.onItemDismissCancel(dismissedItem, i)
        }
        bar.show()
        if (userStops.size == 0) {
            (viewHolder.itemView.context as MainActivity).updateTimeoData()
        }
    }
}