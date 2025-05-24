package org.jsystray.jsystray2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Text; // Pour le contenu des nouvelles fenêtres

//@SpringBootApplication
public class Jsystray2Application extends Application  {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        // Initialise le contexte Spring Boot
        applicationContext = new SpringApplicationBuilder(Jsystray2Application.class)
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

        MenuItem exitItem = new MenuItem("Quitter");
        exitItem.setOnAction(e -> primaryStage.close()); // Ferme l'application

        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);


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
