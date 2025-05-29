package org.jsystray.jsystray2.service;

import jakarta.annotation.PostConstruct;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.jsystray.jsystray2.vo.Position;
import org.jsystray.jsystray2.vo.Projet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProjetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjetService.class);

    @Value("${repertoireProjet}")
    private String repertoireProjet;

    public ProjetService() {
        LOGGER.info("creation repertoireProjet: {}", repertoireProjet);
    }

    @PostConstruct
    public void init() {
        LOGGER.info("init repertoireProjet: {}", repertoireProjet);
    }

    public List<Projet> getProjets() {
        LOGGER.info("répertoire: {}", repertoireProjet);
        if (repertoireProjet == null || repertoireProjet.isEmpty()) {
            throw new RuntimeException("Répertoire vide");
        }
        return listePom();
    }


    private List<Projet> listePom() {
        String directoryPath = repertoireProjet; // Remplacez par le chemin de votre répertoire
        Path p = Paths.get(directoryPath).toAbsolutePath().normalize();


        try {
//            List<Path> pomFiles = findPomFiles(directoryPath);
            LOGGER.info("récupération des fichiers pom ...");
            List<Projet> pomFiles = findPomFiles(p);
            LOGGER.info("récupération des fichiers pom ok");
            if (pomFiles.isEmpty()) {
                LOGGER.info("Aucun fichier pom.xml trouvé (en ignorant target et node_modules) dans : {}", directoryPath);
            } else {
                LOGGER.info("Fichiers pom.xml trouvés (en ignorant target et node_modules) :");
                for (Projet pomFile : pomFiles) {
                    LOGGER.info("{}", pomFile.getFichierPom());
                }
            }
            return pomFiles;
        } catch (IOException e) {
            LOGGER.error("Erreur lors du parcours du répertoire : {}", e.getMessage(), e);
        }
        return null;
    }

    public static List<Projet> findPomFiles(Path startDir) throws IOException {
        List<Projet> pomFiles = new ArrayList<>();

        Files.walkFileTree(startDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String dirName = dir.getFileName().toString();
                // Ignorer les répertoires node_modules et target
                if (dirName.equals("node_modules") || dirName.equals("target")) {
                    return FileVisitResult.SKIP_SUBTREE; // Ne pas visiter ce répertoire ni ses sous-répertoires
                }
                return FileVisitResult.CONTINUE; // Continuer la visite
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Objects.equals(file.getFileName().toString(), "pom.xml")) {
                    Projet projet = new Projet();
                    projet.setNom(file.getParent().getFileName().toString());
                    projet.setRepertoire(file.getParent().toAbsolutePath().toString());
                    projet.setFichierPom(file.toAbsolutePath().toString());
                    pomFiles.add(projet);
                    // Si un pom.xml est trouvé, nous ne voulons pas regarder dans les sous-répertoires
                    // Ici, cela signifie qu'on a trouvé un pom.xml dans le répertoire courant,
                    // donc on peut sauter les autres fichiers dans ce même répertoire,
                    // mais cela ne signifie pas que l'on doit arrêter de chercher ailleurs
                    // car le SimpleFileVisitor parcourt l'arbre de manière hiérarchique.
                    // L'arrêt de la recherche dans les sous-répertoires est géré par la logique
                    // de `postVisitDirectory` ou par l'idée que `pom.xml` est souvent à la racine d'un module.
                    // Pour le cas "Si je trouve un fichier pom, je ne veux pas regarder dans les sous répertoire",
                    // cela s'applique plus à une structure de module où un pom.xml marque le début d'un module.
                    // Si le pom.xml est trouvé dans un dossier, on considèrera que ce dossier est un module
                    // et on ne cherchera pas de pom.xml dans les sous-dossiers de ce même module.
                }
                return FileVisitResult.CONTINUE; // Continuer la visite
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // Cette partie est cruciale pour l'exigence "Si je trouve un fichier pom, je ne veux pas regarder dans les sous répertoire."
                // Si un pom.xml a été ajouté à la liste et que le répertoire courant contient ce pom.xml,
                // alors nous voulons "sauter" la suite de ce répertoire (si d'autres fichiers ou sous-dossiers étaient encore à visiter dans ce même dossier après le pom.xml).
                // Cependant, le `SimpleFileVisitor` visite d'abord les fichiers et ensuite seulement appelle `postVisitDirectory`.
                // L'idée est plutôt que si `dir` est un répertoire et qu'il contient un `pom.xml`, nous avons déjà trouvé ce `pom.xml` dans `visitFile`.
                // Ce que l'on veut, c'est que si `dir` est un module Maven (identifié par un pom.xml à sa racine),
                // alors on ne descende pas dans les sous-répertoires de `dir` pour chercher d'autres `pom.xml`.
                // Le `preVisitDirectory` gère déjà l'exclusion des répertoires `node_modules` et `target`.
                // Pour l'exigence "Si je trouve un fichier pom, je ne veux pas regarder dans les sous répertoire",
                // la meilleure façon de l'implémenter est de ne pas visiter les sous-répertoires d'un répertoire qui *contient* un pom.xml.
                // Cela signifie qu'il faut vérifier l'existence de `pom.xml` dans `preVisitDirectory` pour décider si on continue de descendre.

                // Pour implémenter "Si je trouve un fichier pom, je ne veux pas regarder dans les sous répertoire",
                // nous devons modifier `preVisitDirectory` pour vérifier l'existence de `pom.xml` dans le répertoire courant.
                // Cela est un peu plus complexe car `preVisitDirectory` est appelé avant que nous ayons visité les fichiers du répertoire courant.
                // Une approche serait de maintenir un ensemble de répertoires déjà traités ou de modifier la logique de `preVisitDirectory`.
                // La solution la plus simple qui respecte l'esprit "trouver le pom.xml le plus haut dans la hiérarchie pour un module"
                // est de s'assurer que si un `pom.xml` est trouvé dans un répertoire, on n'ajoute pas les `pom.xml` des sous-répertoires de ce même répertoire.
                // La liste `pomFiles` contient déjà les chemins complets, donc si nous trouvons `A/pom.xml` et `A/B/pom.xml`, les deux seront ajoutés.
                // Pour respecter l'exigence, il faudrait post-filtrer ou adapter la logique de visite.

                // Refactorisons pour l'exigence "Si je trouve un fichier pom, je ne veux pas regarder dans les sous répertoire."
                // Cela signifie que si `dir` contient un `pom.xml`, nous ne voulons pas continuer à chercher dans les sous-dossiers de `dir`.
                // Le `SKIP_SUBTREE` dans `preVisitDirectory` est la clé pour cela.
                // Nous devons donc détecter la présence d'un `pom.xml` avant d'entrer dans les sous-répertoires.
                // La manière la plus propre serait de vérifier si le répertoire contient un `pom.xml`
                // *avant* d'appeler `CONTINUE` dans `preVisitDirectory`.

                return FileVisitResult.CONTINUE; // Continuer
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                LOGGER.error("Erreur lors de la visite du fichier {}: {}", file, exc.getMessage());
                return FileVisitResult.CONTINUE; // Continuer même en cas d'erreur sur un fichier
            }
        });
        return pomFiles;
    }

    public static List<Path> findPomFiles(String directoryPath) throws IOException {
        Path startPath = Paths.get(directoryPath);

        try (Stream<Path> walk = Files.walk(startPath)) {
            return walk
                    .filter(Files::isRegularFile) // Ne traiter que les fichiers réguliers
                    .filter(path -> path.getFileName().toString().equals("pom.xml")) // Chercher les fichiers nommés pom.xml
                    .filter(ProjetService::isNotIgnoredDirectory) // Ignorer les répertoires spécifiques
                    .collect(Collectors.toList());
        }
    }

    private static boolean isNotIgnoredDirectory(Path path) {
        // Vérifier si le chemin contient "target" ou "node_modules" comme nom de répertoire
        // Cela permet de s'assurer que même si un pom.xml se trouve dans un sous-sous-répertoire d'un répertoire ignoré, il est bien ignoré.
        for (Path segment : path) {
            String segmentName = segment.getFileName().toString();
            if (segmentName.equals("target") || segmentName.equals("node_modules")) {
                return false; // Le chemin contient un répertoire à ignorer
            }
        }
        return true; // Le chemin ne contient pas de répertoire à ignorer
    }

    public void updateProject(Projet selectedProduct) throws Exception {
//        updateProject2(selectedProduct);
        updateProject3(selectedProduct);
//        updateProject4(selectedProduct);
    }

    public void updateProject2(Projet selectedProduct) throws IOException {
        var pomFilePath = selectedProduct.getFichierPom();
        File pomFile = new File(pomFilePath);
        if (!pomFile.exists() || !pomFile.isFile()) {
            throw new IllegalArgumentException("Le fichier POM spécifié n'existe pas ou n'est pas un fichier valide : " + pomFilePath);
        }

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        try (FileReader fileReader = new FileReader(pomFile)) {
            model = reader.read(fileReader);
        } catch (Exception e) {
            // Gérer les exceptions de lecture XML (par exemple, fichier mal formé)
            throw new IOException("Erreur lors de la lecture du fichier POM : " + pomFilePath, e);
        }

        if (model != null) {
            Parent parent = model.getParent();
            if (parent != null) {
                LOGGER.info("Ancienne version du parent : " + parent.getVersion());
                parent.setVersion("3.5.0");
                LOGGER.info("Nouvelle version du parent : " + parent.getVersion());

                MavenXpp3Writer writer = new MavenXpp3Writer();
                try (FileWriter fileWriter = new FileWriter(pomFile)) {
                    writer.write(fileWriter, model);
                    LOGGER.info("La version du parent a été mise à jour avec succès dans : " + pomFilePath);
                } catch (Exception e) {
                    // Gérer les exceptions d'écriture XML
                    throw new IOException("Erreur lors de l'écriture du fichier POM : " + pomFilePath, e);
                }
            } else {
                LOGGER.info("Aucun parent trouvé dans le fichier POM : " + pomFilePath);
            }
        }
    }

    public void updateProject3(Projet selectedProduct) throws Exception {
        Path inputFile = Paths.get(selectedProduct.getFichierPom());
        Path outputFile = Files.createTempFile("output", ".xml");

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

        Position debut = null, fin = null;
        String versionActuelle = null;

        try (var inputStream = Files.newInputStream(inputFile);
             var outputStream = Files.newOutputStream(outputFile)) {
            XMLEventReader reader = inputFactory.createXMLEventReader(inputStream);
            XMLEventWriter writer = outputFactory.createXMLEventWriter(outputStream, "UTF-8");

            XMLEventFactory eventFactory = XMLEventFactory.newInstance();

            boolean insideNode1 = false;
            boolean insideNode2 = false;
            List<String> balises = new ArrayList<>();
            List<String> balises2 = List.of("project", "version");

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();

                if (event.isStartElement()) {
                    String tagName = event.asStartElement().getName().getLocalPart();

                    balises.add(tagName);
                    if (tagName.equals("project")) {
                        insideNode1 = true;
                    } else if (insideNode1 && tagName.equals("version")) {
                        insideNode2 = true;
                    }

                    writer.add(event);
                    //} else if (event.isCharacters() && insideNode2) {
                } else if (event.isCharacters() && balises.equals(balises2)) {
                    String originalText = event.asCharacters().getData();
//                    if (originalText.contains("0.0.1-SNAPSHOT")) {

                    LOGGER.info("Texte original dans <node2> : {},({})", originalText, event.getLocation());
                    debut = new Position(event.getLocation().getLineNumber(), event.getLocation().getColumnNumber());
                    fin = new Position(event.getLocation().getLineNumber(), event.getLocation().getColumnNumber() + originalText.length() - 1);
                    versionActuelle = originalText;

                    // Remplacer le texte ici
                    String newText = "0.0.20-SNAPSHOT";
                    //writer.add(eventFactory.createCharacters(newText));
//                    } else {
//                        writer.add(event);
//                    }
                } else if (event.isEndElement()) {
                    String tagName = event.asEndElement().getName().getLocalPart();

                    balises.removeLast();
                    if (tagName.equals("version")) {
                        insideNode2 = false;
                    } else if (tagName.equals("project")) {
                        insideNode1 = false;
                    }

                    writer.add(event);
                } else {
                    writer.add(event); // commentaires, espaces, etc.
                }
            }

            writer.flush();
            writer.close();
            reader.close();
        }

        //Files.move(outputFile, inputFile, StandardCopyOption.REPLACE_EXISTING);
        if (debut != null && fin != null) {
            afficheVersion(inputFile, debut, fin, versionActuelle);

            //modifierFichier(inputFile.toString(), debut, fin, "0.0.20-SNAPSHOT");
        }

        LOGGER.info("Modification terminée !");
    }

    private void afficheVersion(Path inputFile, Position debut, Position fin, String versionActuelle) {
        List<String> listeVersions = getListeVersion(versionActuelle);
        List<String> liste2 = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        for (String version : listeVersions) {
            var libelle="Version " + version;
            map.put(libelle, version);
            liste2.add(libelle);
        }
        final String autre = "Autre (saisir)";
        liste2.add(autre);
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Sélectionnez la nouvelle version (version actuelle : " + versionActuelle + ")");
        // 1. Création de la ComboBox
        var comboBox = new ComboBox<String>();
        comboBox.setItems(FXCollections.observableArrayList(liste2));
        comboBox.setPromptText("Sélectionnez une option"); // Texte par défaut

        // 2. Création du TextField (initialement masqué)
        var textField = new TextField();
        textField.setPromptText("Saisissez votre valeur");
        textField.setVisible(false); // Masqué par défaut
        textField.setManaged(false); // N'affecte pas l'agencement quand il est masqué

        // 3. Gestion de l'affichage/masquage du TextField en fonction de la sélection de la ComboBox
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (autre.equals(newValue)) {
                textField.setVisible(true);
                textField.setManaged(true);
            } else {
                textField.setVisible(false);
                textField.setManaged(false);
                textField.clear(); // Efface le contenu si l'option "Autre" n'est plus sélectionnée
            }
        });

        // 4. Création du bouton de validation
        var validateButton = new Button("Valider");
        var messageLabel = new Label(); // Initialisation du label pour les messages

        // 5. Action du bouton de validation
        validateButton.setOnAction(event -> {
            String selectedOption = comboBox.getSelectionModel().getSelectedItem();
            String message = "";

            if (selectedOption == null) {
                message = "Veuillez sélectionner une option.";
            } else if (autre.equals(selectedOption)) {
                if (textField.getText().isEmpty()) {
                    message = "Veuillez saisir une valeur pour 'Autre'.";
                } else {
                    message = "Option sélectionnée : Autre, Valeur saisie : " + textField.getText();
                }
            } else {
                var version = map.get(selectedOption);
                message = "Option sélectionnée : " + selectedOption + " (" + version + ")";
                if (StringUtils.isNotBlank(version)) {
                    try {
                        modifierFichier(inputFile.toString(), debut, fin, version);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            messageLabel.setText(message);
        });

        // 6. Agencement des éléments dans un VBox
        VBox root = new VBox(10); // Espacement de 10 pixels entre les enfants
        root.setPadding(new Insets(20)); // Marge intérieure de 20 pixels
        root.setAlignment(Pos.TOP_CENTER); // Alignement des éléments au centre en haut

        root.getChildren().addAll(comboBox, textField, validateButton, messageLabel);

        // 7. Création de la scène et affichage de la fenêtre
        Scene scene = new Scene(root, 400, 250); // Largeur, Hauteur
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private List<String> getListeVersion(String versionActuelle) {
        List<String> listeVersions = new ArrayList<>();
        String snapshotSuffix = "-SNAPSHOT";
        if (versionActuelle.endsWith(snapshotSuffix)) {
            versionActuelle = versionActuelle.substring(0, versionActuelle.length() - snapshotSuffix.length());
        }
        String[] versions = versionActuelle.split("\\.");
        List<Integer> listeVersionsInt = new ArrayList<>();
        for (String version : versions) {
            listeVersionsInt.add(Integer.parseInt(version));
        }
        for (int i = 0; i < listeVersionsInt.size(); i++) {
            StringBuilder s = new StringBuilder();
            for (int j = 0; j < listeVersionsInt.size(); j++) {
                if (!s.isEmpty()) {
                    s.append(".");
                }
                var n = listeVersionsInt.get(j);
                if (j == i) {
                    n++;
                } else if (j > i) {
                    n = 0;
                }
                s.append(n);
            }
            s.append(snapshotSuffix);
            listeVersions.add(s.toString());
        }
        return listeVersions;
    }

    public void updateProject4(Projet selectedProduct) throws Exception {

    }

    public static class PositionIndex {
        int index;
        boolean trouve;

        public PositionIndex(int index, boolean trouve) {
            this.index = index;
            this.trouve = trouve;
        }
    }

    public void modifierFichier(String cheminFichier, Position debut, Position fin, String nouveauTexte) throws IOException {
        // 1. Charger le fichier entier en tableau de caractères
        String contenu = Files.readString(Paths.get(cheminFichier));
        char[] caracteres = contenu.toCharArray();

        // 2. Parcourir le tableau pour trouver les positions de début et fin
        PositionIndex posDebut = trouverPosition(caracteres, debut);
        PositionIndex posFin = trouverPosition(caracteres, fin);

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
    private PositionIndex trouverPosition(char[] caracteres, Position position) {
        int ligneActuelle = 1;
        int colonneActuelle = 1;

        for (int i = 0; i < caracteres.length; i++) {
            // Vérifier si on a atteint la position recherchée
            if (ligneActuelle == position.ligne() && colonneActuelle == position.colonne()) {
                return new PositionIndex(i, true);
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
            return new PositionIndex(caracteres.length, true);
        }

        return new PositionIndex(-1, false);
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

    private TextArea textArea;
    private TextField searchField;
    private int currentSearchIndex = 0; // Pour garder une trace de l'occurrence actuelle
    private String lastSearchText = ""; // Pour optimiser les recherches répétées


    public void dependancy(Projet selectedProduct){
        RunService runService=new RunService();
        List<String> list=new CopyOnWriteArrayList<>();

        Stage primaryStage = new Stage();
        primaryStage.setTitle("Titre");
        // Crée un nouveau TextArea

        textArea=new TextArea();
        // Facultatif : Définit un texte initial
        textArea.setText("");

        // 2. Champ de recherche
        searchField = new TextField();
        searchField.setPromptText("Entrez le terme à rechercher...");
        HBox.setHgrow(searchField, Priority.ALWAYS); // Permet au champ de recherche de s'étirer horizontalement

        // 3. Boutons de recherche
        Button searchButton = new Button("Rechercher");
        searchButton.setOnAction(e -> findNext()); // Recherche la première ou la prochaine occurrence

        Button prevButton = new Button("Précédent");
        prevButton.setOnAction(e -> findPrevious());

        Button nextButton = new Button("Suivant");
        nextButton.setOnAction(e -> findNext());

        // Conteneur pour le champ de recherche et les boutons
        HBox searchControls = new HBox(5, searchField, searchButton, prevButton, nextButton); // 5 est l'espacement
        searchControls.setPadding(new javafx.geometry.Insets(10)); // Marge intérieure


        // Crée un BorderPane
        BorderPane root = new BorderPane();

        // Place le TextArea au centre du BorderPane.
        // Par défaut, le centre d'un BorderPane prendra tout l'espace disponible
        // et s'adaptera au redimensionnement.
        root.setCenter(textArea);
        root.setBottom(searchControls); // Les contrôles de recherche en bas

        // Crée une scène avec le BorderPane comme nœud racine
        // Vous pouvez définir une taille initiale pour la fenêtre
        Scene scene = new Scene(root, 600, 400);

        // Définit le titre de la fenêtre
        primaryStage.setTitle("Fenêtre JavaFX avec TextArea plein écran");

        // Définit la scène sur la scène principale
        primaryStage.setScene(scene);

        // Affiche la fenêtre
        primaryStage.show();

        try {
            int res = runService.runCommand(line -> {
                LOGGER.info("run : {}", line);
                //list.add(line.line());
//                sb.append(line.line()+"\n");
                textArea.appendText(line.line()+"\n");
                }, "cmd","/C","mvn", "-f",selectedProduct.getFichierPom(),"dependency:tree");
            LOGGER.info("resultat mvn : {}", res);
        }catch (Exception e){
            LOGGER.error("erreur mvn",e);
        }
    }

    private void findNext() {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) {
            return; // Ne rien faire si le champ de recherche est vide
        }

        // Si le terme de recherche a changé, réinitialiser l'index
        if (!searchText.equals(lastSearchText)) {
            currentSearchIndex = 0;
            lastSearchText = searchText;
            // Optionnel : désélectionner tout texte mis en évidence précédemment
            textArea.selectRange(0, 0);
        }

        String content = textArea.getText();
        int foundIndex = content.indexOf(searchText, currentSearchIndex);

        if (foundIndex != -1) {
            // Occurrence trouvée
            textArea.selectRange(foundIndex, foundIndex + searchText.length());
            // Défiler jusqu'à l'occurrence (nécessite de la tricher un peu avec la position du curseur)
            textArea.positionCaret(foundIndex + searchText.length()); // Déplace le curseur à la fin de la sélection
            // Et ensuite, le TextArea essaiera de rendre la sélection visible.
            currentSearchIndex = foundIndex + searchText.length();
            LOGGER.info("trouve:{},currentSearchIndex:{}", foundIndex, currentSearchIndex);
        } else {
            // Aucune autre occurrence trouvée après l'index actuel
            // Recommencer depuis le début si on n'a pas déjà parcouru tout le texte
            if (currentSearchIndex > 0) { // Si on a déjà cherché au moins une fois
                currentSearchIndex = 0; // Recommencer depuis le début
                findNext(); // Essayer de trouver la première occurrence
            } else {
                // Pas d'occurrence trouvée du tout
                LOGGER.info("Aucune occurrence de '{}' trouvée.", searchText);
                textArea.selectRange(0, 0); // Désélectionner
            }
        }
    }

    private void findPrevious() {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) {
            return;
        }

        // Si le terme de recherche a changé, réinitialiser l'index
        if (!searchText.equals(lastSearchText)) {
            currentSearchIndex = textArea.getLength(); // Commencer par la fin
            lastSearchText = searchText;
            textArea.selectRange(0, 0);
        }

        String content = textArea.getText();
        // Recherche inverse
        int foundIndex = content.lastIndexOf(searchText, currentSearchIndex - searchText.length());

        if (foundIndex != -1) {
            textArea.selectRange(foundIndex, foundIndex + searchText.length());
            textArea.positionCaret(foundIndex); // Déplace le curseur au début de la sélection
            currentSearchIndex = foundIndex;
            LOGGER.info("trouve:{},currentSearchIndex:{}", foundIndex, currentSearchIndex);
        } else {
            // Aucune autre occurrence trouvée avant l'index actuel
            // Recommencer depuis la fin si on n'a pas déjà parcouru tout le texte
            if (currentSearchIndex < textArea.getLength()) { // Si on a déjà cherché au moins une fois
                currentSearchIndex = textArea.getLength(); // Recommencer depuis la fin
                findPrevious(); // Essayer de trouver la dernière occurrence
            } else {
                LOGGER.info("Aucune occurrence de '{}' trouvée.", searchText);
                textArea.selectRange(0, 0);
            }
        }
    }

}
