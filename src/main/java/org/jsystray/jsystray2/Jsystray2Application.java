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

        // Si vous voulez juste une fenêtre vide pour tester :
        stage.setTitle("Hello Spring Boot JavaFX!");
        stage.show();
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
