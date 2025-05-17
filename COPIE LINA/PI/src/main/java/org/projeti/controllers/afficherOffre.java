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
import org.projeti.Service.OffreService;
import org.projeti.entites.Offre;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class afficherOffre {
    private final OffreService ps = new OffreService();
    
    @FXML
    private TableColumn<Offre, String> titre;
    @FXML
    private TableColumn<Offre, String> description;
    @FXML
    private TableColumn<Offre, Float> prix;
    @FXML
    private TableColumn<Offre, String> destination;
    @FXML
    private TableColumn<Offre, Double> rating;
    @FXML
    private TableColumn<Offre, Integer> ratingCount;
    @FXML
    private TableColumn<Offre, Void> modifier;
    @FXML
    private TableColumn<Offre, Void> supprimer;
    @FXML
    private TableView<Offre> tableView;

    @FXML
    public void initialize() {
        System.out.println("initialize() appelé");

        try {
            List<Offre> offres = ps.showAll();
            System.out.println("Offres récupérées: " + offres);

            ObservableList<Offre> observableList = FXCollections.observableList(offres);
            tableView.setItems(observableList);

            // Configuration des colonnes
            titre.setCellValueFactory(new PropertyValueFactory<>("titre"));
            description.setCellValueFactory(new PropertyValueFactory<>("description"));
            prix.setCellValueFactory(new PropertyValueFactory<>("prix"));
            destination.setCellValueFactory(new PropertyValueFactory<>("destination"));
            rating.setCellValueFactory(new PropertyValueFactory<>("rating"));
            ratingCount.setCellValueFactory(new PropertyValueFactory<>("rating_count"));

            // Configuration des colonnes d'action
            modifier.setCellFactory(col -> new TableCell<>() {
                private final Button btn = new Button("Modifier");
                {
                    btn.setOnAction(event -> {
                        Offre offre = getTableView().getItems().get(getIndex());
                        try {
                            modifierOffre(offre);
                        } catch (IOException e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire de modification");
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
                        Offre offre = getTableView().getItems().get(getIndex());
                        try {
                            if (ps.delete(offre) > 0) {
                                tableView.getItems().remove(offre);
                                showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre supprimée avec succès");
                            }
                        } catch (SQLException e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'offre");
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            System.out.println("TableView configurée avec succès.");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger les offres: " + e.getMessage());
        }
    }

    private void modifierOffre(Offre offre) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterOffre.fxml"));
        Parent root = loader.load();
        
        // Obtenir le contrôleur de la nouvelle scène
        AjouterOffre controller = loader.getController();
        
        // Passer l'offre à modifier au contrôleur
        controller.setOffreAModifier(offre);
        
        // Afficher la nouvelle scène
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Modifier l'offre");
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
    private void afficherAjouterOffre(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterOffre.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une Offre");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page d'ajout d'offre");
        }
    }
}





