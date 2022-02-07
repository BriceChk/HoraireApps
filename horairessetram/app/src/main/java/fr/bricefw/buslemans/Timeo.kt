package fr.bricefw.buslemans

import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.ArrayList

object Timeo {

    @Throws(IOException::class)
    fun getPassages(vararg ref: String): List<PassagesArret> {
        val list = ArrayList<String>()
        ref.forEach {
            list.add(it)
        }
        return getPassages(list)
    }

    @Throws(IOException::class, SAXException::class)
    fun getPassages(refs: List<String>): ArrayList<PassagesArret> {
        val param = refs.joinToString(";")

        val result = ArrayList<PassagesArret>()

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc: Document
        try {
            doc = dBuilder.parse(URL("http://timeo3.keolis.com/relais/105.php?xml=3&ran=1&refs=$param").openStream())
        } catch (e: DOMException) {
            e.printStackTrace()
            return result
        }

        doc.documentElement.normalize()

        val arrets = doc.getElementsByTagName("horaire")

        // Pour chaque arrêt récupéré
        for (i in 0 until arrets.length) {
            val e = arrets.item(i) as Element
            val desc = e.getElementsByTagName("description").item(0) as Element
            val nomArret = desc.getElementsByTagName("arret").item(0).textContent
            val codeLigne = desc.getElementsByTagName("ligne").item(0).textContent
            val direction = desc.getElementsByTagName("vers").item(0).textContent

            // Récupération des passages
            val passagesList = ArrayList<String>()
            val passages = e.getElementsByTagName("passages").item(0) as Element
            if (passages.getAttribute("nb") != "0") {
                val passagesElements = passages.getElementsByTagName("passage")
                (0 until passagesElements.length)
                        .map { (passagesElements.item(it) as Element).getElementsByTagName("duree").item(0) as Element }
                        .mapTo(passagesList) { timeToRemainingTime(it.textContent) }
            } else {
                passagesList.add("Aucun horaire")
            }

            // Récupération des messages
            val messagesList = ArrayList<Message>()
            val messages = e.getElementsByTagName("messages").item(0) as Element
            if (messages.getAttribute("nb") != "0") {
                val messagesElements = messages.getElementsByTagName("message")
                for (j in 0 until messagesElements.length) {
                    val element = messagesElements.item(j) as Element
                    val titre = element.getElementsByTagName("titre").item(0).textContent
                    val texte = element.getElementsByTagName("texte").item(0).textContent

                    val msg = Message(titre, texte)
                    messagesList.add(msg)
                }
            }

            val passageArret = PassagesArret(nomArret, codeLigne, direction, passagesList, messagesList)
            result.add(passageArret)

        }

        return result
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
