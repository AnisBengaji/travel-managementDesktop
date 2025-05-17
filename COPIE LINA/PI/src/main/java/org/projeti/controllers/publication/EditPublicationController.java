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
import org.projeti.Service.PublicationService;
import org.projeti.entites.Category;
import org.projeti.entites.Publication;

import java.io.File;
import java.util.List;

public class EditPublicationController {
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
    private List<Category> categories;
    private Publication publication;
    private File selectedImage;

    public void initialize() {
        // Initialize visibility options to match database values
        visibilityComboBox.getItems().addAll("public", "private");
        visibilityComboBox.getSelectionModel().select("public");

        // Set up button actions
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

    public void initData(Publication publication) {
        this.publication = publication;

        // Populate fields with publication data
        titleField.setText(publication.getTitle());
        contentArea.setText(publication.getContenu());
        imagePathField.setText(publication.getImage());
        visibilityComboBox.getSelectionModel().select(publication.getVisibility());

        // Select the correct category
        if (publication.getCategory() != null) {
            for (int i = 0; i < categoryComboBox.getItems().size(); i++) {
                String categoryName = categoryComboBox.getItems().get(i);
                if (categoryName.equals(publication.getCategory().getNomCategory())) {
                    categoryComboBox.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }

    private void loadCategories() {
        try {
            categoryComboBox.getItems().clear();
            categories = categorieService.showAll();
            for (Category category : categories) {
                categoryComboBox.getItems().add(category.getNomCategory());
            }
        } catch (Exception e) {
            showAlert("Error loading categories: " + e.getMessage());
        }
    }

    private void browseForImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedImage = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (selectedImage != null) {
            imagePathField.setText(selectedImage.getAbsolutePath());
        }
    }

    private void savePublication() {
        try {
            // Validate inputs
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String selectedCategoryName = categoryComboBox.getSelectionModel().getSelectedItem();

            if (title.isEmpty()) {
                showAlert("Title cannot be empty");
                return;
            }
            if (content.isEmpty()) {
                showAlert("Content cannot be empty");
                return;
            }
            if (selectedCategoryName == null) {
                showAlert("Please select a category");
                return;
            }

            // Update publication object
            publication.setTitle(title);
            publication.setContenu(content);
            publication.setVisibility(visibilityComboBox.getSelectionModel().getSelectedItem());

            // Handle image
            if (selectedImage != null) {
                publication.setImage(selectedImage.getAbsolutePath());
            } else if (!imagePathField.getText().isEmpty()) {
                publication.setImage(imagePathField.getText());
            } else {
                publication.setImage("");
            }

            // Find selected category
            Category selectedCategory = categories.stream()
                    .filter(category -> category.getNomCategory().equals(selectedCategoryName))
                    .findFirst()
                    .orElse(null);

            if (selectedCategory == null) {
                showAlert("Selected category not found");
                return;
            }

            publication.setCategory(selectedCategory);

            // Save publication
            int result = publicationService.update(publication);

            if (result > 0) {
                closeWindow();
            } else {
                showAlert("Failed to update publication");
            }
        } catch (Exception e) {
            showAlert("Error updating publication: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Publication getPublication() {
        return publication;
    }
}