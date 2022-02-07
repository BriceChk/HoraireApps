package fr.bricefw.buslemans

internal class ListCard(val titre: String, val texte: String = "", val code: String)

class Message(val titre: String, val texte: String)

class PassagesArret(val nomArret: String, val numeroLigne: String, val nomLigne: String, private val passages: List<String> = ArrayList(), val messages: List<Message> = ArrayList(), val erreur: Boolean = false) {
    fun oneLinePassages(): String {
        return passages.joinToString(" | ")
    }

    fun tempStopFormat(): String {
        val m = StringBuilder()
        m.append(ligne())
        m.append("\n\n")
        m.append(passages.joinToString("\n"))
        for (msg in messages) {
            m.append("\n\n")
            m.append(msg.titre)
            m.append("\n")
            m.append(msg.texte)
        }
        return m.toString()
    }

    fun ligne(): String {
        return "Ligne $numeroLigne > $nomLigne"
    }

    fun hasMessage() = messages.isNotEmpty()
}