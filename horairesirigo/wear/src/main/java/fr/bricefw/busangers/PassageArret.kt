package fr.bricefw.busangers

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PassagesArret(resultatWeb: String = "i|i|i|i|i|i|i", val erreur: String = "", val contenuErreur: String = "") {
    private val passagesAller: List<String>
    private val passagesRetour: List<String>
    private val destinationAller: String
    private val destinationRetour: String
    private val ligne: String

    val nomArret: String
    private val option: String

    init {
        val parts = resultatWeb.split("|")
        nomArret =  parts[0]
        ligne = parts[1]
        destinationAller = parts[2]
        destinationRetour = parts[3]
        passagesAller = parts[4].split(";")
        passagesRetour = parts[5].split(";")
        option = parts[6]
    }

    fun timeoCardFormat(): String {
        if (passagesAller.isEmpty() || passagesRetour.isEmpty()) {
            return "Un problème est survenu, Timeo n'est peut être pas disponible. Essayez d'actualiser."
        } else {
            val m = StringBuilder()
            if (option == "0" || option == "2") {
                m.append("Ligne $ligne > $destinationAller\n")
                m.append(passagesAller.joinToString(" | ") { timeToRemainingTime(it) })
            }
            if (option == "2") {
                m.append("\n\n")
            }
            if (option == "1" || option == "2") {
                m.append("Ligne $ligne > $destinationRetour\n")
                m.append(passagesRetour.joinToString(" | ") { timeToRemainingTime(it) })
            }
            return m.toString()
        }
    }

    private fun timeToRemainingTime(time: String): String {
        if (time == "Aucun horaire.") {
            return "Aucun horaire."
        }

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
            return minutesString + " min" + " (" + time.replace(":", "h") + ")"
        }
        return hours.toInt().toString() + "h" + minutesString + " (" + time.replace(":", "h") + ")"
    }
}