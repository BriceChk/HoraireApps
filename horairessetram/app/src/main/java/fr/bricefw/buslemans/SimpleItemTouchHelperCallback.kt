package fr.bricefw.buslemans

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import fr.bricefw.buslemans.activities.MainActivity
import java.util.*

internal class SimpleItemTouchHelperCallback(private val adapter: RVStopCardAdapter) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
    }

    // Activer le déplacement par appui long et le swipe
    override fun isLongPressDragEnabled() = true
    override fun isItemViewSwipeEnabled() = true

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition

        // Si c'est une erreur, osef, on recharge
        if (adapter.items[0].erreur) {
            (viewHolder.itemView.context as MainActivity).updateTimeoData()
            return true
        }

        adapter.onItemMove(fromPosition, toPosition)

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

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val i = viewHolder.adapterPosition

        // Si c'est une carte d'erreur, ne rien faire et recharger la liste pour la réafficher
        if (adapter.items[0].erreur) {
            (viewHolder.itemView.context as MainActivity).updateTimeoData()
            return
        }

        // Sinon supprimer l'arrêt
        val dismissedItem = adapter.items[i]
        adapter.onItemDismiss(i)
        val userStops = MainActivity.getUserStops(viewHolder.itemView.context)
        val stopToRemove = userStops[i]
        userStops.removeAt(i)
        MainActivity.setUserStops(userStops, viewHolder.itemView.context)
        // Puis afficher la snackbar de notif avec le bouton annuler
        val bar = Snackbar.make((viewHolder.itemView.context as MainActivity).findViewById(R.id.timeo_rv), "Arrêt supprimé", Snackbar.LENGTH_LONG)
        bar.setAction("Annuler") {
            bar.dismiss()
            userStops.add(i, stopToRemove)
            MainActivity.setUserStops(userStops, viewHolder.itemView.context)
            adapter.onItemDismissCancel(dismissedItem, i)
        }
        bar.show()
        // Si il n'y a plus d'arrets apres, recharger la liste pour afficher le message "c'est vide !"
        if (userStops.size == 0) {
            (viewHolder.itemView.context as MainActivity).updateTimeoData()
        }
    }
}