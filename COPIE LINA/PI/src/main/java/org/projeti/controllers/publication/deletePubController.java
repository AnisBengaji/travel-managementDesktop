package org.projeti.controllers.publication;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.projeti.Service.PublicationService;
import org.projeti.entites.Publication;

import java.io.IOException;
import java.sql.SQLException;

public class deletePubController {

    @FXML
    private TextField idTextField;

    private PublicationService publicationService = new PublicationService();

    @FXML
    void deletePublication() {
        try {

            String idText = idTextField.getText();
            if (idText.isEmpty()) {
                showError("Publication ID is required!");
                return;
            }

            int id = Integer.parseInt(idText);

            // Create a Publication instance with the ID
            Publication publication = new Publication();
            publication.setId_publication(id);


            int rowsAffected = publicationService.delete(publication);

            if (rowsAffected > 0) {
                showSuccess("publication supprimée avec succès!");
                idTextField.clear(); // Clear the input field


                navigateToDetail();
            } else {
                showError("Aucune publication trouvée avec l'identifiant indiqué");
            }
        } catch (NumberFormatException e) {
            showError("ID de publication invalide.");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void navigateToDetail() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/homePub.fxml"));
            Parent root = loader.load();


            Stage stage = (Stage) idTextField.getScene().getWindow();


            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Detail Page");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading Detail.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.showAndWait();
    }


    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(message);
        alert.showAndWait();
    }


    @FXML
    private void goBackHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/homePub.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) idTextField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load homePub.fxml: " + e.getMessage());
        }
    }
}