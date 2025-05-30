package org.jsystray.jsystray2.service;

import org.jsystray.jsystray2.vo.Position;
import org.jsystray.jsystray2.vo.Projet;
import org.jsystray.jsystray2.vo.ResultatBalise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XmlParserService {


    private static final Logger LOGGER = LoggerFactory.getLogger(XmlParserService.class);

    public List<ResultatBalise> parse(Path fichier, List<List<String>> balisesRecherche) throws Exception {
        Path inputFile = fichier;
//        Path outputFile = Files.createTempFile("output", ".xml");
        List<ResultatBalise> resultat = new ArrayList<>();

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
//        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

        Position debut = null, fin = null;
//        String versionActuelle = null;

        try (var inputStream = Files.newInputStream(inputFile);
             /*var outputStream = Files.newOutputStream(outputFile)*/) {
            XMLEventReader reader = inputFactory.createXMLEventReader(inputStream);
//            XMLEventWriter writer = outputFactory.createXMLEventWriter(outputStream, "UTF-8");

//            XMLEventFactory eventFactory = XMLEventFactory.newInstance();

//            boolean insideNode1 = false;
//            boolean insideNode2 = false;
            List<String> balises = new ArrayList<>();
//            List<String> balises2 = List.of("project", "version");

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();

                if (event.isStartElement()) {
                    String tagName = event.asStartElement().getName().getLocalPart();

                    balises.add(tagName);
//                    if (tagName.equals("project")) {
//                        insideNode1 = true;
//                    } else if (insideNode1 && tagName.equals("version")) {
//                        insideNode2 = true;
//                    }

//                    writer.add(event);
                    //} else if (event.isCharacters() && insideNode2) {
                } else if (event.isCharacters() && contient(balisesRecherche,balises)) {
                    String originalText = event.asCharacters().getData();
//                    if (originalText.contains("0.0.1-SNAPSHOT")) {

                    LOGGER.info("Texte original dans <node2> : {},({})", originalText, event.getLocation());
                    debut = new Position(event.getLocation().getLineNumber(), event.getLocation().getColumnNumber());
                    fin = new Position(event.getLocation().getLineNumber(), event.getLocation().getColumnNumber() + originalText.length() - 1);
//                    versionActuelle = originalText;
                    resultat.add(new ResultatBalise(List.copyOf(balises),originalText,debut,fin));

                    // Remplacer le texte ici
//                    String newText = "0.0.20-SNAPSHOT";
                    //writer.add(eventFactory.createCharacters(newText));
//                    } else {
//                        writer.add(event);
//                    }
                } else if (event.isEndElement()) {
//                    String tagName = event.asEndElement().getName().getLocalPart();

                    balises.removeLast();
//                    if (tagName.equals("version")) {
//                        insideNode2 = false;
//                    } else if (tagName.equals("project")) {
//                        insideNode1 = false;
//                    }

                    //writer.add(event);
                } else {
                    //writer.add(event); // commentaires, espaces, etc.
                }
            }

//            writer.flush();
//            writer.close();
            reader.close();
        }

        //Files.move(outputFile, inputFile, StandardCopyOption.REPLACE_EXISTING);
//        if (debut != null && fin != null) {
//            //afficheVersion(inputFile, debut, fin, versionActuelle);
//
//            //modifierFichier(inputFile.toString(), debut, fin, "0.0.20-SNAPSHOT");
//        }

//        LOGGER.info("Modification terminée !");
        return resultat;
    }

    public void modifierFichier(String cheminFichier, Position debut, Position fin, String nouveauTexte) throws IOException {
        // 1. Charger le fichier entier en tableau de caractères
        String contenu = Files.readString(Paths.get(cheminFichier));
        char[] caracteres = contenu.toCharArray();

        // 2. Parcourir le tableau pour trouver les positions de début et fin
        ProjetService.PositionIndex posDebut = trouverPosition(caracteres, debut);
        ProjetService.PositionIndex posFin = trouverPosition(caracteres, fin);

        // 3. Valider que les positions ont été trouvées
        if (!posDebut.trouve) {
            throw new IllegalArgumentException("Position de début " + debut + " non trouvée dans le fichier");
        }
        if (!posFin.trouve) {
            throw new IllegalArgumentException("Position de fin " + fin + " non trouvée dans le fichier");
        }
        if (posDebut.index > posFin.index) {
            throw new IllegalArgumentException("La position de début doit être avant la position de fin");
        }

        // 4. Remplacer en mémoire dans le tableau de caractères
        char[] nouveauContenu = remplacerDansTableau(caracteres, posDebut.index, posFin.index, nouveauTexte);

        // 5. Écrire le nouveau contenu dans le fichier
        Files.writeString(Paths.get(cheminFichier), new String(nouveauContenu));

        System.out.println("Remplacement effectué :");
        System.out.println("- Position début: " + debut + " (index " + posDebut.index + ")");
        System.out.println("- Position fin: " + fin + " (index " + posFin.index + ")");
        System.out.println("- Texte remplacé par: \"" + nouveauTexte + "\"");
    }

    /**
     * Parcourt le tableau de caractères pour trouver l'index correspondant à la position ligne/colonne
     */
    private ProjetService.PositionIndex trouverPosition(char[] caracteres, Position position) {
        int ligneActuelle = 1;
        int colonneActuelle = 1;

        for (int i = 0; i < caracteres.length; i++) {
            // Vérifier si on a atteint la position recherchée
            if (ligneActuelle == position.ligne() && colonneActuelle == position.colonne()) {
                return new ProjetService.PositionIndex(i, true);
            }

            // Gérer les sauts de ligne
            if (caracteres[i] == '\n') {
                ligneActuelle++;
                colonneActuelle = 1;
            } else if (caracteres[i] == '\r') {
                // Gérer les retours chariot (Windows: \r\n, Mac classique: \r)
                if (i + 1 < caracteres.length && caracteres[i + 1] == '\n') {
                    // Windows: \r\n - on passe le \r, le \n sera traité au prochain tour
                    continue;
                } else {
                    // Mac classique: \r seul
                    ligneActuelle++;
                    colonneActuelle = 1;
                }
            } else {
                colonneActuelle++;
            }
        }

        // Vérifier si la position est à la fin du fichier
        if (ligneActuelle == position.ligne() && colonneActuelle == position.colonne()) {
            return new ProjetService.PositionIndex(caracteres.length, true);
        }

        return new ProjetService.PositionIndex(-1, false);
    }

    /**
     * Remplace la portion du tableau entre indexDebut et indexFin par le nouveau texte
     */
    private char[] remplacerDansTableau(char[] original, int indexDebut, int indexFin, String nouveauTexte) {
        char[] nouveauTexteChars = nouveauTexte.toCharArray();

        // Calculer la taille du nouveau tableau
        int tailleOriginale = original.length;
        int tailleASupprimer = indexFin - indexDebut + 1;
        int tailleAAjouter = nouveauTexteChars.length;
        int nouvelleTaille = tailleOriginale - tailleASupprimer + tailleAAjouter;

        // Créer le nouveau tableau
        char[] resultat = new char[nouvelleTaille];

        // Copier la partie avant le remplacement
        System.arraycopy(original, 0, resultat, 0, indexDebut);

        // Copier le nouveau texte
        System.arraycopy(nouveauTexteChars, 0, resultat, indexDebut, nouveauTexteChars.length);

        // Copier la partie après le remplacement
        System.arraycopy(original, indexFin + 1, resultat, indexDebut + nouveauTexteChars.length,
                tailleOriginale - indexFin - 1);

        return resultat;
    }

    private <T> boolean contient(List<List<T>> liste, List<T> element) {
        for (List<T> l : liste) {
            if (l.equals(element)) {
                return true;
            }
        }
        return false;
    }

}
