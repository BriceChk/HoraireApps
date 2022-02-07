package fr.bricefw.buslemans.chooseline

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import fr.bricefw.buslemans.ListCard
import fr.bricefw.buslemans.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class ChooseStopActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_line_stop)

        val llm = LinearLayoutManager(applicationContext)
        val rv = findViewById<RecyclerView>(R.id.rv_choose_linestop)
        rv.setHasFixedSize(true)
        rv.layoutManager = llm

        val ligneCode = intent.getStringExtra("code")
        val cards = ArrayList<ListCard>()

        val reader = BufferedReader(InputStreamReader(assets.open("$ligneCode.txt")))
        reader.forEachLine {
            cards.add(ListCard(it.split(" : ")[1], "Code Timeo : " + it.split(" : ")[0].split("_")[1], it.split(" : ")[0].split("_")[0]))
        }

        rv.adapter = RVStopListAdapter(cards)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 16908332) {
            this.finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
