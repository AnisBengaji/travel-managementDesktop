package org.projeti.controllers.publication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.projeti.entites.Category;
import org.projeti.Service.CategorieService;

import java.io.IOException;
import java.sql.SQLException;

public class ajouterCategorieController {

    @FXML private TextField nomCategorieTextField;
    @FXML private TextField descriptionTextField;
    @FXML private Button returnButton;

    @FXML
    void ajouterCategorie(ActionEvent event) {
        String nomCategory = nomCategorieTextField.getText().trim();
        String description = descriptionTextField.getText().trim();

        // Debugging output
        System.out.println("Nom Category: " + nomCategory);
        System.out.println("Description: " + description);

        // Validate input
        if (nomCategory.isEmpty() || description.isEmpty()) {
            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }



        // Validate input format (letters and spaces only)
        if (!nomCategory.matches("[a-zA-Z\\s]+") || !description.matches("[a-zA-Z\\s]+")) {
            showAlert("Error", "Category name and description should only contain letters and spaces.", Alert.AlertType.ERROR);
            return;
        }

        // Create new Category instance
        Category category = new Category();
        category.setNomCategory(nomCategory);
        category.setDescription(description);

        CategorieService categorieService = new CategorieService();

        try {
            int rowsAffected = categorieService.insert(category);
            if (rowsAffected > 0) {
                showAlert("Success", "Category added successfully!", Alert.AlertType.INFORMATION);
                nomCategorieTextField.clear();
                descriptionTextField.clear();
            } else {
                showAlert("Error", "Failed to add category!", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }



    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void returnToDetailCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailCat.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) returnButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Category Details");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load DetailCat.fxml: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}