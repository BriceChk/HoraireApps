package fr.bricefw.busangers

import android.app.Activity
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

object Timeo {

    fun getPassages(activity: Activity, vararg ref: String): List<PassagesArret> {
        return getPassages(ref.map { PassagesArret(activity, it) })
    }

    fun getPassages(userstopsList: List<PassagesArret>): ArrayList<PassagesArret> {
        val userstops = ArrayList(userstopsList)

        for (userstop in userstops) {
            val passagesAller = ArrayList<String>()
            val passagesRetour = ArrayList<String>()
            val ligne = userstop.ligne
            val codeTimeo = userstop.timeoAller
            val url = URL("http://server-url/RPC/SP/SP_RT_ProchainBus.php?ligne=line:ANG:$ligne&arret=$codeTimeo")
            var retourFait = false
            url.readText().lines().forEach {
                if (it.contains("|")) {
                    val parts = it.split("|")
                    if (retourFait) {
                        when {
                            parts[2] == "END" -> {
                                passagesAller.add("Aucun horaire.")
                            }
                            parts[3] == "END" -> {
                                passagesAller.add(timeToRemainingTime(parts[2].split(">")[1].split(" (")[0]))
                            }
                            else -> {
                                passagesAller.add(timeToRemainingTime(parts[2].split(">")[1].split(" (")[0]))
                                passagesAller.add(timeToRemainingTime(parts[3].split(">")[1].split(" (")[0]))
                            }
                        }
                    } else {
                        when {
                            parts[2] == "END" -> {
                                passagesRetour.add("Aucun horaire.")
                            }
                            parts[3] == "END" -> {
                                passagesRetour.add(timeToRemainingTime(parts[2].split(">")[1].split(" (")[0]))
                            }
                            else -> {
                                passagesRetour.add(timeToRemainingTime(parts[2].split(">")[1].split(" (")[0]))
                                passagesRetour.add(timeToRemainingTime(parts[3].split(">")[1].split(" (")[0]))
                            }
                        }
                        retourFait = true
                    }
                }
            }
            userstop.passagesAller = passagesAller
            userstop.passagesRetour = passagesRetour
        }

        return userstops
    }

    private fun timeToRemainingTime(time: String): String {
        var heure = time.replace(":", " ")
        var now = Date()
        var nowFormat = SimpleDateFormat("yyyy dd MM", Locale.FRENCH)
        val nowString = nowFormat.format(now)
        nowFormat = SimpleDateFormat("yyyy dd MM HH mm", Locale.FRENCH)
        now = nowFormat.parse(nowFormat.format(now))

        heure = "$nowString $heure"
        val date = nowFormat.parse(heure)

        if (Math.abs(date.time - now.time) < 60000)
            return "En approche"

        if (date.before(now))
            date.time = date.time + 86400000

        val diffMillies = date.time - now.time

        val hours = TimeUnit.MILLISECONDS.toHours(diffMillies)
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(diffMillies) - TimeUnit.HOURS.toMinutes(hours)).toInt()

        var minutesString : String = minutes.toString()
        if (minutes < 10) {
            minutesString = if (minutes == 0)
                ""
            else
                "0$minutesString"
        }

        if (hours.toInt() == 0) {
            if (minutes == 1)
                return minutesString + " minute" + " (" + time.replace(":", "h") + ")"
            return minutesString + " minutes" + " (" + time.replace(":", "h") + ")"
        }
        return hours.toInt().toString() + "h" + minutesString + " (" + time.replace(":", "h") + ")"
    }
}
