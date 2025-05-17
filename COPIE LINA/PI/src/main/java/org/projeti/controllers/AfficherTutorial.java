package org.projeti.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.projeti.Service.TutorialService;
import org.projeti.entites.Tutorial;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherTutorial {
    private final TutorialService tutorialService = new TutorialService();
    
    @FXML
    private TableColumn<Tutorial, String> nom;
    @FXML
    private TableColumn<Tutorial, String> dateDebut;
    @FXML
    private TableColumn<Tutorial, String> dateFin;
    @FXML
    private TableColumn<Tutorial, Float> prix;
    @FXML
    private TableColumn<Tutorial, String> url;
    @FXML
    private TableColumn<Tutorial, Integer> offreId;
    @FXML
    private TableColumn<Tutorial, Void> modifier;
    @FXML
    private TableColumn<Tutorial, Void> supprimer;
    @FXML
    private TableView<Tutorial> tableView;

    @FXML
    public void initialize() {
        try {
            List<Tutorial> tutorials = tutorialService.showAll();
            ObservableList<Tutorial> observableList = FXCollections.observableList(tutorials);
            tableView.setItems(observableList);

            // Configuration des colonnes
            nom.setCellValueFactory(new PropertyValueFactory<>("nom_tutorial"));
            dateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
            dateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
            prix.setCellValueFactory(new PropertyValueFactory<>("prix_tutorial"));
            url.setCellValueFactory(new PropertyValueFactory<>("url"));
            offreId.setCellValueFactory(new PropertyValueFactory<>("offre_id"));

            // Configuration des colonnes d'action
            modifier.setCellFactory(col -> new TableCell<>() {
                private final Button btn = new Button("Modifier");
                {
                    btn.setOnAction(event -> {
                        Tutorial tutorial = getTableView().getItems().get(getIndex());
                        try {
                            modifierTutorial(tutorial);
                        } catch (IOException e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Erreur lors de l'ouverture du formulaire de modification");
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            supprimer.setCellFactory(col -> new TableCell<>() {
                private final Button btn = new Button("Supprimer");
                {
                    btn.setOnAction(event -> {
                        Tutorial tutorial = getTableView().getItems().get(getIndex());
                        try {
                            if (tutorialService.delete(tutorial) > 0) {
                                tableView.getItems().remove(tutorial);
                                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                                    "Tutoriel supprimé avec succès");
                            }
                        } catch (SQLException e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Erreur lors de la suppression du tutoriel");
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", 
                "Impossible de charger les tutoriels: " + e.getMessage());
        }
    }

    private void modifierTutorial(Tutorial tutorial) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierTutorial.fxml"));
        Parent root = loader.load();
        
        AjouterTutorial controller = loader.getController();
        controller.setTutorialAModifier(tutorial);
        
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Modifier le tutoriel");
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void afficherAjouterTutorial(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterTutorial.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Tutoriel");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors du chargement du formulaire d'ajout");
        }
    }
}
