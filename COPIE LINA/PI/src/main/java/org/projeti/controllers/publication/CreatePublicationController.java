package org.projeti.controllers.publication;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.projeti.Service.CategorieService;
import org.projeti.Service.EmailValidation;
import org.projeti.Service.PublicationService;
import org.projeti.Service.UserSession;
import org.projeti.entites.Category;
import org.projeti.entites.Publication;
import org.projeti.entites.User;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class CreatePublicationController {
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> visibilityComboBox;
    @FXML private TextField imagePathField;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private PublicationService publicationService;
    private CategorieService categorieService;
    private EmailValidation emailValidation;
    private List<Category> categories;
    private File selectedImage;

    public void initialize() {
        visibilityComboBox.getItems().addAll("public", "private");
        visibilityComboBox.getSelectionModel().select("public");

        emailValidation = new EmailValidation();

        browseButton.setOnAction(e -> browseForImage());
        cancelButton.setOnAction(e -> closeWindow());
        saveButton.setOnAction(e -> savePublication());
    }

    public void setPublicationService(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    public void setCategorieService(CategorieService categorieService) {
        this.categorieService = categorieService;
        loadCategories();
    }

    private void loadCategories() {
        try {
            categoryComboBox.getItems().clear();
            categories = categorieService.showAll();
            for (Category category : categories) {
                categoryComboBox.getItems().add(category.getNomCategory());
            }
            if (!categoryComboBox.getItems().isEmpty()) {
                categoryComboBox.getSelectionModel().select(0);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void browseForImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedImage = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (selectedImage != null && selectedImage.exists()) {
            imagePathField.setText(selectedImage.getAbsolutePath());
        } else {
            showAlert("Error", "Selected image file does not exist or is invalid.");
        }
    }

    private void savePublication() {
        try {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String selectedCategoryName = categoryComboBox.getSelectionModel().getSelectedItem();
            String visibility = visibilityComboBox.getSelectionModel().getSelectedItem();

            if (title.isEmpty()) {
                showAlert("Error", "Title cannot be empty.");
                return;
            }
            if (content.isEmpty()) {
                showAlert("Error", "Content cannot be empty.");
                return;
            }
            if (selectedCategoryName == null) {
                showAlert("Error", "Please select a category.");
                return;
            }
            if (visibility == null) {
                showAlert("Error", "Please select visibility.");
                return;
            }

            Publication publication = new Publication();
            publication.setTitle(title);
            publication.setContenu(content);
            publication.setDate_publication(Date.valueOf(LocalDate.now()));
            publication.setVisibility(visibility);
            publication.setImage(selectedImage != null && selectedImage.exists() ? selectedImage.getAbsolutePath() : null);

            Category selectedCategory = categories.stream()
                    .filter(category -> category.getNomCategory().equals(selectedCategoryName))
                    .findFirst()
                    .orElse(null);
            if (selectedCategory == null) {
                showAlert("Error", "Selected category not found.");
                return;
            }
            publication.setCategory(selectedCategory);

            if (UserSession.getUserId() == 0) {
                showAlert("Error", "No logged-in user found. Please log in.");
                redirectToLogin();
                return;
            }
            User currentUser = new User();
            currentUser.setId(UserSession.getUserId());
            currentUser.setEmail(UserSession.getEmail());
            currentUser.setRole(UserSession.getRole());
            String fullName = UserSession.getName();
            if (fullName != null && !fullName.isEmpty()) {
                String[] nameParts = fullName.trim().split("\\s+", 2);
                currentUser.setPrenom(nameParts[0]);
                currentUser.setNom(nameParts.length > 1 ? nameParts[1] : "");
            } else {
                currentUser.setPrenom("");
                currentUser.setNom("");
            }
            publication.setAuthor(currentUser);

            if (publicationService == null) {
                showAlert("Error", "Publication service not initialized.");
                return;
            }

            int result = publicationService.insert(publication);
            if (result > 0) {
                if (emailValidation != null && UserSession.getEmail() != null) {
                    String subject = "New Publication Created";
                    String message = "A new publication has been created:\n\nTitle: " + publication.getTitle() + "\nContent: " + publication.getContenu();
                    emailValidation.sendEmail(UserSession.getEmail(), subject, message);
                }
                showAlert("Success", "Publication added successfully!");
                closeWindow();
            } else {
                showAlert("Error", "Failed to save publication.");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("No logged-in user found")) {
                showAlert("Error", "User session expired. Please log in again.");
                redirectToLogin();
            } else {
                showAlert("Error", "Database error: " + e.getMessage());
            }
        } catch (Exception e) {
            showAlert("Error", "Unexpected error: " + e.getMessage());
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/projeti/views/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load login page: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}