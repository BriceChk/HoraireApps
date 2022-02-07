package fr.bricefw.busangers.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.bricefw.busangers.ListCard
import fr.bricefw.busangers.R
import fr.bricefw.busangers.adapters.StopRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_choose_line_stop.*
import java.io.BufferedReader
import java.io.InputStreamReader

class ChooseStopActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_line_stop)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val llm = LinearLayoutManager(applicationContext)
        val rv = findViewById<RecyclerView>(R.id.rv_choose_linestop)
        rv.setHasFixedSize(true)
        rv.layoutManager = llm

        val ligne = intent.getStringExtra("code")
        val cards = ArrayList<ListCard>()

        val reader = BufferedReader(InputStreamReader(assets.open("$ligne.txt")))
        reader.forEachLine {
            cards.add(ListCard(it.split("|")[2], it.split("|")[3] + " - Code Timeo : " + it.split("|")[0] + " / " + it.split("|")[1], it.split("|")[0]))
        }

        rv.adapter = StopRecyclerViewAdapter(cards, ligne, this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 16908332) {
            this.finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
