package org.jsystray.jsystray2.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jsystray.jsystray2.service.ProjetService;
import org.jsystray.jsystray2.vo.Projet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

public class SelectionUI {


    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionUI.class);

    private ConfigurableApplicationContext applicationContext;

    public SelectionUI(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void selection(){

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


        // 4. Création du bouton de validation
        Button validateButton2 = new Button("Valider la sélection2");
        validateButton2.setOnAction(event -> {
            Projet selectedProduct = tableView.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                try {
                    ProjetService projetService = applicationContext.getBean("projetService", ProjetService.class);
                    projetService.updateProject(selectedProduct);
                }catch (Exception e){
                    LOGGER.error("Erreur", e);
                }
            }
        });

        // 4. Création du bouton de validation
        Button validateButton3 = new Button("Valider la sélection3");
        validateButton3.setOnAction(event -> {
            Projet selectedProduct = tableView.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                try {
                    ProjetService projetService = applicationContext.getBean("projetService", ProjetService.class);
                    projetService.dependancy(selectedProduct,applicationContext);
                }catch (Exception e){
                    LOGGER.error("Erreur", e);
                }
            }
        });

        //VBox root = new VBox(new Text("texte"));
        VBox root = new VBox(10);
        root.setSpacing(10);
        root.setStyle("-fx-padding: 10;");
        root.getChildren().addAll(tableView, validateButton, validateButton2, validateButton3);

        Scene newScene = new Scene(root, 300, 200);
        newStage.setScene(newScene);
        newStage.show();
    }


    private List<Projet> listePom() {
//        ProjetService projetService=applicationContext.getBean(ProjetService.class);
        ProjetService projetService=applicationContext.getBean("projetService",ProjetService.class);
        return projetService.getProjets();
    }
}
