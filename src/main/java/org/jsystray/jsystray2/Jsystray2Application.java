package org.jsystray.jsystray2;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jsystray.jsystray2.config.AppConfig;
import org.jsystray.jsystray2.service.ProjetService;
import org.jsystray.jsystray2.vo.Projet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Text; // Pour le contenu des nouvelles fenêtres

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@SpringBootApplication
public class Jsystray2Application extends Application  {


    private static final Logger LOGGER = LoggerFactory.getLogger(Jsystray2Application.class);

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        // Initialise le contexte Spring Boot
        applicationContext = new SpringApplicationBuilder(Jsystray2Application.class, AppConfig.class)
                .run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Ici, vous pouvez charger votre FXML et utiliser le contexte Spring
        // pour injecter des dépendances dans vos contrôleurs JavaFX.

        // Exemple simple sans FXML pour le moment :
        // StageManager (voir section suivante pour une implémentation plus propre)
//         StageManager stageManager = applicationContext.getBean(StageManager.class);
//         stageManager.displayScene(stage, "/ui/MainView.fxml");

        // Pour un exemple très simple sans StageManager :
        // Vous pouvez charger un FXML et obtenir le contrôleur depuis le contexte Spring
//         FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/MainView.fxml"));
//         fxmlLoader.setControllerFactory(applicationContext::getBean); // Important pour l'injection Spring
//         Parent root = fxmlLoader.load();
//         stage.setScene(new Scene(root));
//         stage.setTitle("Spring Boot JavaFX App");
//         stage.show();

        BorderPane root=menu(stage);
        // Créer la scène principale
        Scene scene = new Scene(root, 600, 400);

        // Si vous voulez juste une fenêtre vide pour tester :
        stage.setTitle("Hello Spring Boot JavaFX!");
        stage.setScene(scene);
        stage.show();
    }

    private BorderPane menu(Stage primaryStage){
        // 1. Créer la barre de menu
        MenuBar menuBar = new MenuBar();

        // 2. Créer les menus

        // Menu "Fichier"
        Menu fileMenu = new Menu("Fichier");

        MenuItem newItem = new MenuItem("Nouveau");
        newItem.setOnAction(e -> openNewWindow("Nouvelle Fenêtre", "Ceci est la fenêtre 'Nouveau'."));

        MenuItem openItem = new MenuItem("Ouvrir");
        openItem.setOnAction(e -> openNewWindow("Fenêtre d'Ouverture", "Ceci est la fenêtre 'Ouvrir'."));

        MenuItem saveItem = new MenuItem("Enregistrer");
        saveItem.setOnAction(e -> System.out.println("Action : Enregistrer")); // Exemple d'action simple

        MenuItem selection = new MenuItem("Selection");
        selection.setOnAction(e -> selection());

        MenuItem exitItem = new MenuItem("Quitter");
        exitItem.setOnAction(e -> primaryStage.close()); // Ferme l'application

        fileMenu.getItems().addAll(newItem, openItem, saveItem,selection, new SeparatorMenuItem(), exitItem);


        // Menu "Édition"
        Menu editMenu = new Menu("Édition");
        MenuItem cutItem = new MenuItem("Couper");
        cutItem.setOnAction(e -> System.out.println("Action : Couper"));

        MenuItem copyItem = new MenuItem("Copier");
        copyItem.setOnAction(e -> System.out.println("Action : Copier"));

        MenuItem pasteItem = new MenuItem("Coller");
        pasteItem.setOnAction(e -> System.out.println("Action : Coller"));

        editMenu.getItems().addAll(cutItem, copyItem, pasteItem);

        // Menu "Aide"
        Menu helpMenu = new Menu("Aide");
        MenuItem aboutItem = new MenuItem("À propos");
        aboutItem.setOnAction(e -> openNewWindow("À Propos", "Application de démonstration de menus JavaFX."));
        helpMenu.getItems().add(aboutItem);

        // 3. Ajouter les menus à la barre de menu
        menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);

        // 4. Mettre la barre de menu dans le layout principal
        BorderPane root = new BorderPane();
        root.setTop(menuBar);

        return root;
    }

    /**
     * Méthode générique pour ouvrir une nouvelle fenêtre.
     * @param title Le titre de la nouvelle fenêtre.
     * @param content Le texte à afficher dans la nouvelle fenêtre.
     */
    private void openNewWindow(String title, String content) {
        Stage newStage = new Stage();
        newStage.setTitle(title);

        VBox root = new VBox(new Text(content));
        root.setSpacing(10);
        root.setStyle("-fx-padding: 10;");

        Scene newScene = new Scene(root, 300, 200);
        newStage.setScene(newScene);
        newStage.show();
    }

    private void selection(){

        var liste=listePom();

        Stage newStage = new Stage();
        newStage.setTitle("selection");

        // 1. Création du TableView
        var tableView = new TableView<Projet>();

        // 2. Définition des colonnes
        TableColumn<Projet, String> nameColumn = new TableColumn<>("Nom");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom")); // "name" correspond au nom de la propriété dans la classe Product

        TableColumn<Projet, String> descriptionColumn = new TableColumn<>("répertoire");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("repertoire"));

        TableColumn<Projet, String> priceColumn = new TableColumn<>("pom");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("fichierPom"));

        // Ajout des colonnes au TableView
        tableView.getColumns().addAll(nameColumn, descriptionColumn, priceColumn);

        // 3. Ajout de données d'exemple au TableView
        ObservableList<Projet> products = FXCollections.observableArrayList(
                /*new Product("Ordinateur Portable", "Puissant ordinateur portable pour le gaming et le travail", 1200.00),
                new Product("Clavier Mécanique", "Clavier rétroéclairé avec switches MX Brown", 80.50),
                new Product("Souris Gaming", "Souris haute précision avec capteur optique", 45.99),
                new Product("Écran 27 pouces", "Moniteur Full HD avec dalle IPS", 250.00)*/
                liste
        );
        tableView.setItems(products);

        // Permettre la sélection d'une seule ligne (par défaut, mais bon à spécifier)
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // 4. Création du bouton de validation
        Button validateButton = new Button("Valider la sélection");
        validateButton.setOnAction(event -> {
            Projet selectedProduct = tableView.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                // Affiche les informations de la ligne sélectionnée
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Projet sélectionné");
                alert.setHeaderText(null);
                alert.setContentText("Vous avez sélectionné : \n" +
                        "Nom: " + selectedProduct.getNom() + "\n" +
                        "Description: " + selectedProduct.getDescription() + "\n" +
                        "Répertoire: " + String.format("%s", selectedProduct.getRepertoire()));
                alert.showAndWait();

                // Vous pouvez faire d'autres actions ici, par exemple :
                // - Fermer la fenêtre : primaryStage.close();
                // - Passer la sélection à une autre partie de votre application
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Aucune sélection");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez sélectionner une ligne avant de valider.");
                alert.showAndWait();
            }
        });

        //VBox root = new VBox(new Text("texte"));
        VBox root = new VBox(10);
        root.setSpacing(10);
        root.setStyle("-fx-padding: 10;");
        root.getChildren().addAll(tableView, validateButton);

        Scene newScene = new Scene(root, 300, 200);
        newStage.setScene(newScene);
        newStage.show();
    }

    private List<Projet> listePom() {
//        ProjetService projetService=applicationContext.getBean(ProjetService.class);
        ProjetService projetService=applicationContext.getBean("projetService",ProjetService.class);
        return projetService.getProjets();
    }


    @Override
    public void stop() {
        // Ferme le contexte Spring Boot lors de l'arrêt de l'application JavaFX
        applicationContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        Application.launch(Jsystray2Application.class, args);
    }

//    public static void main(String[] args) {
//        SpringApplication.run(Jsystray2Application.class, args);
//    }

}
