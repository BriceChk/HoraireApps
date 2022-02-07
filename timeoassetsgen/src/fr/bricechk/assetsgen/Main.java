package fr.bricechk.assetsgen;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Timeo t = new Timeo();


        System.out.println("---------------------------------------------------------");
        System.out.println("Villes disponibles :");
        for (Map.Entry<String, String> entry : t.getVilles().entrySet()) {
            System.out.println(" " + entry.getValue() + " > " + entry.getKey());
        }
        System.out.println("---");
        System.out.println("Enter le code de la ville : ");
        Scanner reader = new Scanner(System.in);
        String codeVille = reader.next();
        while (!t.getVilles().containsKey(codeVille)) {
            System.out.println("Ce code n'existe pas.");
            System.out.println("Enter le code de la ville : ");
            codeVille = reader.next();
        }

        System.out.println("---------------------------------------------------------");
        System.out.println("Filtrer les listes ? (o/n) : ");
        String f = reader.next();
        while (!f.equals("o") && !f.equals("n")) {
            System.out.println("Invalide.");
            System.out.println("Filtrer les listes ? (o/n) : ");
            f = reader.next();
        }
        boolean filtre = true;
        boolean ref = true;
        if (f.equals("n")) {
            filtre = false;
            System.out.println("---------------------------------------------------------");
            System.out.println("Garder les références ? (o/n) : ");
            f = reader.next();
            while (!f.equals("o") && !f.equals("n")) {
                System.out.println("Invalide.");
                System.out.println("Garder les références ? (o/n) : ");
                f = reader.next();
            }
            if (f.equals("n")) {
                ref = false;
            }
        }

        if (codeVille.equals("000")) {
            for (Map.Entry<String, String> entry : t.getVilles().entrySet()) {
                if (!entry.getKey().equals("000"))
                    genererVille(entry.getKey(), entry.getValue(), filtre, ref);
            }
        } else {
            genererVille(codeVille, t.getVilles().get(codeVille), filtre, ref);
        }
    }

    private static void genererVille(String codeVille, String nomVille, boolean filtre, boolean refs) {
        Timeo t = new Timeo();
        System.out.println("---------------------------------------------------------");
        System.out.println("Création des fichiers " + (filtre ? "(filtré)" : "(non filtré)") + " pour la ville " + nomVille + " ...");
        System.out.println("---------------------------------------------------------");

        File dossier = new File(nomVille);
        if (!dossier.isDirectory()) {
            dossier.mkdir();
        }

        HashMap<String, String> lignes = new HashMap<>();

        for (Map.Entry<String, String> entry : t.getLignes(codeVille).entrySet()) {
            String codeLigne = entry.getValue();

            System.out.println(entry.getKey() + " (" + codeLigne + ")");

            String ligne = codeLigne.split("_")[0];
            String sens = codeLigne.split("_")[1];

            HashMap<String, String> ancienneListe = t.getArrets(codeVille, ligne, sens);
            HashMap<String, String> nouvelleListe = new HashMap<>();

            for (Map.Entry<String, String> arret : ancienneListe.entrySet()) {
                String codeTimeo = arret.getKey().split("_")[1];
                String ref = arret.getKey().split("_")[0];
                String nom = arret.getValue();

                if (filtre) {
                    if (codeVille.equals("105")) {
                        if((codeLigne.equals("T1_A") && codeTimeo.equals("802")) || (codeLigne.equals("T1_R") && codeTimeo.equals("801")))
                            continue;
                        if((codeLigne.equals("T2_A") && codeTimeo.equals("852")) || (codeLigne.equals("T2_A") && codeTimeo.equals("850")) || (codeLigne.equals("T2_A") && codeTimeo.equals("856")) || (codeLigne.equals("T2_A") && codeTimeo.equals("858")) || (codeLigne.equals("T2_A") && codeTimeo.equals("854")))
                            continue;
                        if((codeLigne.equals("T2_R") && codeTimeo.equals("851")) || (codeLigne.equals("T2_R") && codeTimeo.equals("849")) || (codeLigne.equals("T2_R") && codeTimeo.equals("855")) || (codeLigne.equals("T2_R") && codeTimeo.equals("857")) || (codeLigne.equals("T2_R") && codeTimeo.equals("853")))
                            continue;
                    }

                    if (t.getPassages(codeVille, ref).size() > 0) {
                        System.out.println("  Arrêt " + nom + " (Code : " + codeTimeo + ")");
                        nouvelleListe.put(arret.getKey(), arret.getValue());
                    } else {
                        System.out.println("  Arrêt " + nom + " (Code : " + codeTimeo + ") - Pas d'horaire");
                    }
                } else {
                    if (refs) {
                        nouvelleListe.put(arret.getKey(), arret.getValue());
                    } else {
                        nouvelleListe.put(codeTimeo, arret.getValue());
                    }
                    System.out.println("  Arrêt " + nom + " (Code : " + codeTimeo + ")");
                }
            }

            LinkedHashMap<String, String> liste = sortHashMapByValues(nouvelleListe);

            if (liste.isEmpty()) {
                System.out.println("Ignorée - ligne : " + codeLigne);
            } else {
                lignes.put(entry.getKey(), entry.getValue());
                try {
                    File file = new File(nomVille + File.separator + codeLigne + ".txt");
                    file.createNewFile();
                    PrintWriter out = new PrintWriter(file);
                    for (Map.Entry<String, String> arret : liste.entrySet()) {
                        out.println(arret.getKey() + " : " + arret.getValue());
                    }
                    out.close();
                    System.out.println("Terminé - ligne : " + codeLigne);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("---------------------------------------------------------");
        }

        System.out.println("Enregistrement de la liste des lignes ...");
        LinkedHashMap<String, String> lignesSorted = sortHashMapByValues(lignes);
        try {
            File file = new File(nomVille + File.separator + "lignes.txt");
            file.createNewFile();
            PrintWriter out = new PrintWriter(file);
            for (Map.Entry<String, String> ligne : lignesSorted.entrySet()) {
                out.println(ligne.getKey() + " : " + ligne.getValue());
            }
            out.close();
            System.out.println("Fichiers pour la ville " + nomVille + " générés !");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("---------------------------------------------------------");
    }

    private static LinkedHashMap<String, String> sortHashMapByValues(HashMap<String, String> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<String> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, String> sortedMap =
                new LinkedHashMap<>();

        for (String val : mapValues) {
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                String comp1 = passedMap.get(key);

                if (comp1.equals(val)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}
