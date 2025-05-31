package org.jsystray.jsystray2.ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

public class AbstractViewUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractViewUI.class);

    private ConfigurableApplicationContext applicationContext;

    private TextArea textArea;
    private TextField searchField;
    private int currentSearchIndex = 0; // Pour garder une trace de l'occurrence actuelle
    private String lastSearchText = ""; // Pour optimiser les recherches répétées

    public AbstractViewUI(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    protected void displayTextArea() {
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Titre");
        // Crée un nouveau TextArea

        textArea=new TextArea();
        // Facultatif : Définit un texte initial
        textArea.setText("");
        textArea.setEditable(false);

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

    public TextArea getTextArea() {
        return textArea;
    }
}
