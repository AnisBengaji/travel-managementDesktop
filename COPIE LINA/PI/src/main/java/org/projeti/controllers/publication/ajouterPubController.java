package org.projeti.controllers.publication;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.projeti.Service.CategorieService;
import org.projeti.Service.EmailValidation;
import org.projeti.Service.PublicationService;
import org.projeti.entites.Category;
import org.projeti.entites.Publication;
import org.projeti.entites.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class ajouterPubController {

    @FXML private TextField titleTextField;
    @FXML private TextField contenuTextField;
    @FXML private Label currentDateLabel;
    @FXML private TextField visibilityTextField;
    @FXML private TextField imageTextField;
    @FXML private ComboBox<Category> categorieComboBox;
    @FXML private Button returnButton;
    @FXML private Button browseImageButton;

    private ObservableList<Category> categories;
    private PublicationService publicationService;
    private CategorieService categorieService;
    private EmailValidation emailValidation;
    private File selectedImage;

    @FXML
    public void initialize() {
        System.out.println("ajouterPubController initialize");

        categorieService = new CategorieService();
        publicationService = new PublicationService();
        emailValidation = new EmailValidation();

        try {
            categories = FXCollections.observableArrayList(categorieService.showAll());
            categorieComboBox.setItems(categories);
            categorieComboBox.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getNomCategory());
                }
            });
            categorieComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getNomCategory());
                }
            });
        } catch (SQLException e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage());
            e.printStackTrace();
        }

        if (currentDateLabel != null) {
            currentDateLabel.setText("Current date: " + LocalDate.now());
        } else {
            System.err.println("Warning: currentDateLabel is not initialized. Check FXML file.");
        }

        if (browseImageButton != null) {
            browseImageButton.setOnAction(e -> browseImage());
        } else {
            System.err.println("Warning: browseImageButton is not initialized. Check FXML file.");
        }
    }

    public void setPublicationService(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    public void setCategorieService(CategorieService categorieService) {
        this.categorieService = categorieService;
        try {
            categories = FXCollections.observableArrayList(categorieService.showAll());
            categorieComboBox.setItems(categories);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage());
        }
    }

    @FXML
    private void browseImage() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            Stage stage = (returnButton != null && returnButton.getScene() != null && returnButton.getScene().getWindow() != null)
                    ? (Stage) returnButton.getScene().getWindow()
                    : new Stage();
            if (stage == null) {
                System.err.println("browseImage: No stage available");
                showAlert("Error", "Cannot open file chooser: No window available.");
                return;
            }
            selectedImage = fileChooser.showOpenDialog(stage);
            if (selectedImage != null && selectedImage.exists()) {
                imageTextField.setText(selectedImage.getAbsolutePath());
                System.out.println("browseImage: Selected image: " + selectedImage.getAbsolutePath());
            } else {
                imageTextField.clear();
                selectedImage = null;
                System.out.println("browseImage: No valid image selected");
            }
        } catch (Exception e) {
            System.err.println("browseImage: FileChooser error: " + e.getMessage());
            showAlert("Error", "Failed to select image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void ajouterPub(ActionEvent event) {
        try {
            System.out.println("ajouterPub: Creating publication for userId=25");

            String title = titleTextField.getText().trim();
            String contenu = contenuTextField.getText().trim();
            Date sqlDate = Date.valueOf(LocalDate.now()); // Use current date
            String visibility = visibilityTextField.getText().trim();
            Category selectedCategorie = categorieComboBox.getSelectionModel().getSelectedItem();

            if (title.isEmpty()) {
                showAlert("Error", "Title cannot be empty.");
                return;
            }
            if (contenu.isEmpty()) {
                showAlert("Error", "Content cannot be empty.");
                return;
            }
            if (visibility.isEmpty()) {
                showAlert("Error", "Visibility cannot be empty.");
                return;
            }
            if (selectedCategorie == null) {
                showAlert("Error", "Please select a category.");
                return;
            }

            Publication publication = new Publication();
            publication.setTitle(title);
            publication.setContenu(contenu);
            publication.setDate_publication(sqlDate);
            publication.setVisibility(visibility);
            publication.setImage(selectedImage != null && selectedImage.exists() ? selectedImage.getAbsolutePath() : null);
            publication.setCategory(selectedCategorie);

            User currentUser = new User();
            currentUser.setId(25); // Hardcoded user ID
            currentUser.setEmail("user25@example.com");
            currentUser.setRole("USER");
            currentUser.setPrenom("User");
            currentUser.setNom("TwentyFive");
            publication.setAuthor(currentUser);
            System.out.println("ajouterPub: Setting author: ID=" + currentUser.getId() + ", Email=" + currentUser.getEmail());

            if (publicationService == null) {
                showAlert("Error", "Publication service not initialized.");
                return;
            }

            int rowsAffected = publicationService.insert(publication);
            if (rowsAffected > 0) {
                if (emailValidation != null) {
                    String subject = "New Publication Created";
                    String message = "A new publication has been created:\n\nTitle: " + publication.getTitle() + "\nContent: " + publication.getContenu();
                    emailValidation.sendEmail(currentUser.getEmail(), subject, message);
                }
                showAlert("Success", "Publication added successfully!");
                clearForm();
                returnHome();
            } else {
                showAlert("Error", "Failed to add publication!");
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void returnHome() {
        try {
            System.out.println("returnHome: Navigating to homepub.fxml");
            String fxmlPath = "/homepub.fxml";
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                resource = getClass().getClassLoader().getResource(fxmlPath);
                if (resource == null) {
                    System.err.println("returnHome: Resource not found: " + fxmlPath);
                    showAlert("Error", "Cannot find homepub.fxml. Please check the file location in src/main/resources/");
                    return;
                }
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Stage stage = (returnButton != null && returnButton.getScene() != null)
                    ? (Stage) returnButton.getScene().getWindow()
                    : new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load homepub.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void clearForm() {
        titleTextField.clear();
        contenuTextField.clear();
        visibilityTextField.clear();
        imageTextField.clear();
        selectedImage = null;
        categorieComboBox.getSelectionModel().clearSelection();
    }
}