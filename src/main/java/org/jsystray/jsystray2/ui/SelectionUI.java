package org.jsystray.jsystray2.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jsystray.jsystray2.properties.AppProperties;
import org.jsystray.jsystray2.properties.RunMavenProperties;
import org.jsystray.jsystray2.service.ProjetService;
import org.jsystray.jsystray2.vo.Projet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelectionUI {


    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionUI.class);

    private ConfigurableApplicationContext applicationContext;

    public SelectionUI(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void selection(String repertoire) {

        var liste = listePom(repertoire);

        Stage newStage = new Stage();
        newStage.setTitle("selection");

        // 1. Création du TableView
        var tableView = new TableView<Projet>();

        // 2. Définition des colonnes
        TableColumn<Projet, String> nameColumn = new TableColumn<>("Nom");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom")); // "name" correspond au nom de la propriété dans la classe Product

        TableColumn<Projet, String> descriptionColumn = new TableColumn<>("répertoire");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("repertoire"));

        TableColumn<Projet, String> pomColumn = new TableColumn<>("pom");
        pomColumn.setCellValueFactory(new PropertyValueFactory<>("fichierPom"));

        TableColumn<Projet, String> packageJsonColumn = new TableColumn<>("packageJson");
        packageJsonColumn.setCellValueFactory(new PropertyValueFactory<>("packageJson"));

        TableColumn<Projet, String> goModColumn = new TableColumn<>("goMod");
        goModColumn.setCellValueFactory(new PropertyValueFactory<>("goMod"));

        TableColumn<Projet, String> cargoTomlColumn = new TableColumn<>("cargoToml");
        cargoTomlColumn.setCellValueFactory(new PropertyValueFactory<>("cargoToml"));

        // Ajout des colonnes au TableView
        tableView.getColumns().addAll(nameColumn, descriptionColumn, pomColumn, packageJsonColumn, goModColumn, cargoTomlColumn);

        // 3. Ajout de données d'exemple au TableView
        ObservableList<Projet> products = FXCollections.observableArrayList(liste);
        tableView.setItems(products);

        // Permettre la sélection d'une seule ligne (par défaut, mais bon à spécifier)
//        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 4. Création du bouton de validation
        Button validateButton = new Button("Valider la sélection");
        validateButton.setOnAction(event -> {
            tableView.getSelectionModel().getSelectedItems().stream().forEach(item -> {
                Projet selectedProduct = item;
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
                }
            });
//            Projet selectedProduct = tableView.getSelectionModel().getSelectedItem();
//            if (selectedProduct != null) {
//                // Affiche les informations de la ligne sélectionnée
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Projet sélectionné");
//                alert.setHeaderText(null);
//                alert.setContentText("Vous avez sélectionné : \n" +
//                        "Nom: " + selectedProduct.getNom() + "\n" +
//                        "Description: " + selectedProduct.getDescription() + "\n" +
//                        "Répertoire: " + String.format("%s", selectedProduct.getRepertoire()));
//                alert.showAndWait();
//
//                // Vous pouvez faire d'autres actions ici, par exemple :
//                // - Fermer la fenêtre : primaryStage.close();
//                // - Passer la sélection à une autre partie de votre application
//            } else {
//                Alert alert = new Alert(Alert.AlertType.WARNING);
//                alert.setTitle("Aucune sélection");
//                alert.setHeaderText(null);
//                alert.setContentText("Veuillez sélectionner une ligne avant de valider.");
//                alert.showAndWait();
//            }
        });


        // 4. Création du bouton de validation
        Button updateVersionButton = new Button("Mise à jour de la version");
        updateVersionButton.setOnAction(event -> {
            tableView.getSelectionModel().getSelectedItems().stream().forEach(item -> {
                Projet selectedProduct = item;
                if (selectedProduct != null) {
                    try {
                        ProjetService projetService = applicationContext.getBean("projetService", ProjetService.class);
                        projetService.updateProject(selectedProduct);
                    } catch (Exception e) {
                        LOGGER.error("Erreur pour le projet {}", selectedProduct.getRepertoire(), e);
                    }
                }
            });
//            Projet selectedProduct = tableView.getSelectionModel().getSelectedItem();
//            if (selectedProduct != null) {
//                try {
//                    ProjetService projetService = applicationContext.getBean("projetService", ProjetService.class);
//                    projetService.updateProject(selectedProduct);
//                } catch (Exception e) {
//                    LOGGER.error("Erreur", e);
//                }
//            }
        });

        // 4. Création du bouton de validation
        Button listDependenciesButton = new Button("Liste des dépendances");
        listDependenciesButton.setOnAction(event -> {
//            Projet selectedProduct = tableView.getSelectionModel().getSelectedItem();
            tableView.getSelectionModel().getSelectedItems().stream().forEach(item -> {
                Projet selectedProduct = item;
                if (selectedProduct != null) {
                    try {
                        ProjetService projetService = applicationContext.getBean("projetService", ProjetService.class);
                        projetService.dependancy(selectedProduct, applicationContext);
                    } catch (Exception e) {
                        LOGGER.error("Erreur pour le projet {}", selectedProduct.getRepertoire(), e);
                    }
                }
            });
//            if (selectedProduct != null) {
//                try {
//                    ProjetService projetService = applicationContext.getBean("projetService", ProjetService.class);
//                    projetService.dependancy(selectedProduct, applicationContext);
//                } catch (Exception e) {
//                    LOGGER.error("Erreur", e);
//                }
//            }
        });

        // 4. Création du bouton de validation
        Button gitStatusButton = new Button("Status git");
        gitStatusButton.setOnAction(event -> {
            tableView.getSelectionModel().getSelectedItems().stream().forEach(item -> {
                Projet selectedProduct = item;
                if (selectedProduct != null) {
                    try {
//                    ProjetService projetService = applicationContext.getBean("projetService", ProjetService.class);
//                    projetService.dependancy(selectedProduct,applicationContext);
                        GitStatusUI gitStatusUI = new GitStatusUI(applicationContext);
                        gitStatusUI.run(Path.of(selectedProduct.getFichierPom()).getParent());
                    } catch (Exception e) {
                        LOGGER.error("Erreur pour le projet {}", selectedProduct.getRepertoire(), e);
                    }
                }
            });
        });

        // 4. Création du bouton de validation
        Button testExecButton = new Button("test exec");
        testExecButton.setOnAction(event -> {
            executeTest();
        });

        // 4. Création du bouton de validation
        Button statusGlobalButton = new Button("status global");
        statusGlobalButton.setOnAction(event -> {
            tableView.getSelectionModel().getSelectedItems().stream().forEach(item -> {
                //LOGGER.info("selection ...");
                Projet selectedProduct = item;
                if (selectedProduct != null) {
                    try {
                        //LOGGER.info("selection: {}", selectedProduct);
                        StatusGlobalUI statusGlobalUI = new StatusGlobalUI(applicationContext);
                        statusGlobalUI.run(selectedProduct);
                    } catch (Exception e) {
                        LOGGER.error("Erreur pour le projet {}", selectedProduct.getRepertoire(), e);
                    }
                }
            });
        });

        // 4. Création du bouton de validation
        Button runMavenButton = new Button("Run Maven command");
        runMavenButton.setOnAction(event -> {

            runMaven(tableView);


        });

        //VBox root = new VBox(new Text("texte"));
        VBox root = new VBox(10);
        root.setSpacing(10);
        root.setStyle("-fx-padding: 10;");
        root.getChildren().addAll(tableView, validateButton, updateVersionButton,
                listDependenciesButton, gitStatusButton, testExecButton,
                statusGlobalButton, runMavenButton);

        Scene newScene = new Scene(root, 500, 700);
        newStage.setScene(newScene);
        newStage.show();
    }

    private void runMaven(TableView<Projet> tableView) {
        List<String> choix;

        AppProperties appProperties = applicationContext.getBean(AppProperties.class);
        var conf = appProperties.getRunMaven();
        choix = conf.stream().map(RunMavenProperties::getNom).collect(Collectors.toList());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choix.get(0), choix);
        dialog.setTitle("Sélection");
        dialog.setHeaderText("Veuillez choisir une option");
        dialog.setContentText("Choix :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            LOGGER.info("Option choisie : " + selected);

            tableView.getSelectionModel().getSelectedItems().stream().forEach(item -> {
                Projet selectedProduct = item;
                if (selectedProduct != null) {
                    try {
//                    ProjetService projetService = applicationContext.getBean("projetService", ProjetService.class);
//                    projetService.dependancy(selectedProduct,applicationContext);
                        var actionOpt = conf.stream()
                                .filter(x -> Objects.equals(x.getNom(), selected))
                                .findFirst();
                        if (actionOpt.isPresent()) {
                            var action = actionOpt.get();

                            RunMavenUI runMavenUI = new RunMavenUI(applicationContext);
                            runMavenUI.run(selectedProduct, action.getCommande());
                        }

                    } catch (Exception e) {
                        LOGGER.error("Erreur pour le projet {}", selectedProduct.getRepertoire(), e);
                    }
                }
            });

        });
    }

    private void executeTest() {
        try {
            if (false) {
                LOGGER.info("execution ...");
                // Ouvre une nouvelle fenêtre de l'invite de commandes (cmd.exe)
                Runtime.getRuntime().exec("cmd /c start cmd");
                LOGGER.info("execution ok");
            } else {
                LOGGER.info("execution 2 ...");
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "cmd");
                LOGGER.info("starting process");
                pb.start();
                LOGGER.info("waiting for process");
                pb.wait();
                LOGGER.info("execution 2 ok");
            }
        } catch (Exception e) {
            LOGGER.error("Erreur", e);
        }
    }


    private List<Projet> listePom(String fichier) {
        ProjetService projetService = applicationContext.getBean("projetService", ProjetService.class);
        if (fichier != null) {
            return projetService.getProjets(fichier);
        } else {
            return projetService.getProjets();
        }
    }
}
