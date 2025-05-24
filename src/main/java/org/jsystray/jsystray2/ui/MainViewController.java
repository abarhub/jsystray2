package org.jsystray.jsystray2.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

@Component
public class MainViewController {

    @FXML
    private Label messageLabel;

    // Vous pouvez injecter d'autres beans Spring ici, par exemple un service
    // @Autowired
    // private MyService myService;

    @FXML
    public void initialize() {
        // Cette méthode est appelée après que les éléments FXML ont été injectés
        // et après que les dépendances Spring ont été injectées.
        messageLabel.setText("Bienvenue dans votre application JavaFX Spring Boot!");
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}
