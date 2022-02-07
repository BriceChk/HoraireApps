package fr.bricechk.assetsgen

import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import kotlin.collections.HashMap

internal class Timeo {
    val villes = HashMap<String, String>()

    init {
        villes.put("105", "Le Mans")
        villes.put("117", "Pau")
        villes.put("120", "Soissons")
        villes.put("135", "Aix-en-Provence")
        villes.put("147", "Caen")
        villes.put("217", "Dijon")
        villes.put("297", "Brest")
        villes.put("402", "Pau-Agen")
        villes.put("416", "Blois")
        villes.put("422", "St-Etienne")
        villes.put("440", "Nantes")
        villes.put("457", "Montargis")
        villes.put("497", "Angers")
        villes.put("691", "Macon-Villefranche")
        villes.put("910", "Épinay-sur-Orge")
        villes.put("999", "Rennes")
        villes.put("000", "TOUTES")
    }

    /**
     * @param codeVille Le code de la ville
     *
     * @param ligne Le code de la ligne
     * *
     * @param sens Le sens de la ligne
     * *
     * @return Une HashMap ref_code = nom
     */
    fun getArrets(codeVille: String, ligne: String, sens: String): HashMap<String, String> {
        val arrets = HashMap<String, String>()

        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val builder = dbFactory.newDocumentBuilder()
            val doc = builder.parse(URL("http://timeo3.keolis.com/relais/$codeVille.php?xml=1&ligne=$ligne&sens=$sens").openStream())
            doc.documentElement.normalize()

            val arretsElement = doc.getElementsByTagName("als")
            for (i in 0..arretsElement.length - 1) {
                val als = arretsElement.item(i) as Element
                val arret = als.getElementsByTagName("arret").item(0) as Element
                val refs = als.getElementsByTagName("refs").item(0).textContent
                val code = arret.getElementsByTagName("code").item(0).textContent
                val nom = arret.getElementsByTagName("nom").item(0).textContent

                arrets.put(refs + "_" + code, nom)
            }
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        }

        return arrets
    }

    /**
     * @return HashMap "nLigne > destination" --> "codeLigne"
     */
    fun getLignes(codeVille: String): HashMap<String, String> {
            val lignes = HashMap<String, String>()

            try {
                val dbFactory = DocumentBuilderFactory.newInstance()
                val builder = dbFactory.newDocumentBuilder()
                val doc = builder.parse(URL("http://timeo3.keolis.com/relais/$codeVille.php?xml=1").openStream())
                doc.documentElement.normalize()

                val lignesElement = doc.getElementsByTagName("ligne")
                for (i in 0..lignesElement.length - 1) {
                    val ligne = lignesElement.item(i) as Element
                    var numLigne = ligne.getElementsByTagName("code").item(0).textContent
                    val sens = ligne.getElementsByTagName("sens").item(0).textContent
                    val dest = ligne.getElementsByTagName("vers").item(0).textContent

                    if (numLigne.startsWith("0")) {
                        //numLigne = numLigne.replaceFirst("0", "")
                    }

                    lignes.put("Ligne $numLigne > $dest", numLigne + "_" + sens)
                }
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SAXException) {
                e.printStackTrace()
            }

            return lignes
        }

    /**
     * @param refArret Référence de l'arrêt
     * *
     * @return Une liste des passages sous forme de texte
     */
    fun getPassages(codeVille: String, refArret: String): List<String> {
        val passagesList = ArrayList<String>()

        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val builder = dbFactory.newDocumentBuilder()
            val doc = builder.parse(URL("http://timeo3.keolis.com/relais/$codeVille.php?xml=3&ran=1&refs=$refArret").openStream())
            doc.documentElement.normalize()

            val e = doc.getElementsByTagName("horaire").item(0) as Element

            // Récupération des passages
            val passages = e.getElementsByTagName("passages").item(0) as Element
            if (passages.getAttribute("nb") != "0") {
                val passagesElements = passages.getElementsByTagName("passage")
                (0..passagesElements.length - 1)
                        .map { (passagesElements.item(it) as Element).getElementsByTagName("duree").item(0) as Element }
                        .mapTo(passagesList) { timeToRemainingTime(it.textContent) }
            }

        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        }

        return passagesList
    }

    fun timeToRemainingTime(time: String): String {
        var heure = time.replace(":", " ")
        val now = Date()
        val nowFormat = SimpleDateFormat("yyyy dd MM", Locale.FRENCH)
        val nowString = nowFormat.format(now)

        heure = nowString + " " + heure
        val dateFormat = SimpleDateFormat("yyyy dd MM HH mm", Locale.FRENCH)
        val date = dateFormat.parse(heure)

        if (date.before(now)) {
            date.time = date.time + 86400000
        }

        val diffMillies = date.time - now.time

        val hours = "" + TimeUnit.MILLISECONDS.toHours(diffMillies).toInt()
        var minutes = "" + (TimeUnit.MILLISECONDS.toMinutes(diffMillies) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffMillies))).toInt()
        if (Integer.parseInt(minutes) < 10) {
            if (Integer.parseInt(minutes) == 0)
                minutes = ""
            else
                minutes = "0" + minutes
        }

        val hFormat = SimpleDateFormat("HH mm", Locale.FRENCH)
        if (Integer.parseInt(hours) == 0) {
            return minutes + " minutes" + " (" + hFormat.format(date).replace(" ", "h") + ")"
        } else {
            return hours + "h" + minutes + " (" + hFormat.format(date).replace(" ", "h") + ")"
        }
    }
}