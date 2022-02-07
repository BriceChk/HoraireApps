package fr.bricefw.busangers

import android.app.Activity
import java.io.BufferedReader
import java.io.InputStreamReader

internal class ListCard(val titre: String, val texte: String = "", val code: String)

class PassagesArret(val activity: Activity, val code: String = "no|no|no", val erreur: String = "no", val messageDev: String = "") {
    private var parts = code.split("|")
    var ligne = parts[0]
    val timeoAller = parts[1]
    val option = parts[2]

    var passagesAller: List<String> = ArrayList()
    var passagesRetour: List<String> = ArrayList()

    var nomArret: String = ""
    var destinationAller: String = ""
    var destinationRetour: String = ""
    private var timeoRetour: String = ""

    init {
        if (erreur == "no") {
            // Nom ligne
            var reader = BufferedReader(InputStreamReader(activity.assets.open("lignes.txt")))
            reader.forEachLine {
                if (it.split("|")[0] == ligne) {
                    destinationAller = it.split("|")[2]
                    destinationRetour = it.split("|")[3]
                    return@forEachLine
                }
            }

            // Nom arret + code timeo retour
            reader = BufferedReader(InputStreamReader(activity.assets.open("$ligne.txt")))
            reader.forEachLine {
                if (it.split("|")[0] == timeoAller) {
                    nomArret = it.split("|")[2]
                    timeoRetour = it.split("|")[1]
                }
            }
        }
    }

    fun timeoCardFormat(): String {
        if (messageDev == "") {
            if (passagesAller.isEmpty() || passagesRetour.isEmpty()) {
                return "Un problème est survenu, Timeo n'est peut être pas disponible. Essayez d'actualiser."
            } else {
                val m = StringBuilder()
                if (option == "0" || option == "2") {
                    m.append("Ligne $ligne > $destinationAller\n")
                    m.append(passagesAller.joinToString(" | "))
                }
                if (option == "2") {
                    m.append("\n\n")
                }
                if (option == "1" || option == "2") {
                    m.append("Ligne $ligne > $destinationRetour\n")
                    m.append(passagesRetour.joinToString(" | "))
                }
                return m.toString()
            }
        } else {
            return messageDev
        }
    }

    fun tempStopFormat(): String {
        val m = StringBuilder()
        m.append("Ligne $ligne > $destinationAller")
        m.append("\n\n")
        when {
            passagesAller.isNotEmpty() -> m.append(passagesAller.joinToString("\n"))
            else -> m.append("Un problème est survenu, Timeo n'est peut être pas disponible. Essayez d'actualiser.")
        }

        m.append("\n\n")
        m.append("Ligne $ligne > $destinationRetour")
        m.append("\n\n")
        when {
            passagesRetour.isNotEmpty() -> m.append(passagesRetour.joinToString("\n"))
        }

        return m.toString()
    }
}