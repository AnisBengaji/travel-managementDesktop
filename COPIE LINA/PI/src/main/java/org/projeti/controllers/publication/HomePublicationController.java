package org.projeti.controllers.publication;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.projeti.Service.PublicationService;
import org.projeti.entites.Publication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class HomePublicationController {

    @FXML
    private Button btnDetail;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button switchToCatButton;
    @FXML
    private VBox publicationsContainer;
    @FXML
    private TextField searchTextField;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<String> visibilityComboBox;
    @FXML
    private Button btnTranslate;

    private PublicationService publicationService = new PublicationService();
    private ObservableList<Publication> publications = FXCollections.observableArrayList();
    private FilteredList<Publication> filteredPublications;
    private ListView<Publication> publicationsListView;

    public void initialize() {
        // Create a ListView and add it to the publicationsContainer
        publicationsListView = new ListView<>();
        publicationsContainer.getChildren().add(publicationsListView);
        publicationsListView.setPrefHeight(500.0);
        publicationsListView.getStyleClass().add("publications-list");

        try {
            loadPublications();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load publications: " + e.getMessage());
        }

        // Initialize incremental search
        setupIncrementalSearch();

        // Initialize sorting options
        setupDateSorting();

        // Initialize category filter
        setupCategoryFilter();

        // Initialize visibility filter
        setupVisibilityFilter();

        // Initialize language ComboBox if it wasn't already defined in FXML
        if (languageComboBox.getItems().isEmpty()) {
            languageComboBox.getItems().addAll("English", "French", "Spanish", "German", "Arabic");
            languageComboBox.setValue("English"); // Set default language
        }

        // Setup button actions
        if (switchToCatButton != null) {
            switchToCatButton.setOnAction(event -> switchToHomeCat());
        }

        if (btnTranslate != null) {
            btnTranslate.setOnAction(event -> translatePublication());
        }
    }

    private void loadPublications() throws SQLException {
        List<Publication> publicationList = publicationService.showAll();
        publications.setAll(publicationList);
        publicationsListView.setItems(publications);
        publicationsListView.setCellFactory(param -> new PublicationListCell());
    }

    private void setupIncrementalSearch() {
        filteredPublications = new FilteredList<>(publications, p -> true);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPublications.setPredicate(publication -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Show all publications if search field is empty
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Check if title, author, or content contains the search term
                return publication.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        publication.getContenu().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Publication> sortedPublications = new SortedList<>(filteredPublications);
        publicationsListView.setItems(sortedPublications);
    }

    private void setupDateSorting() {
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            switch (newValue) {
                case "Newest to Oldest":
                    publications.sort(Comparator.comparing(Publication::getDate_publication).reversed());
                    break;
                case "Oldest to Newest":
                    publications.sort(Comparator.comparing(Publication::getDate_publication));
                    break;
                case "Title A-Z":
                    publications.sort(Comparator.comparing(p -> p.getTitle().toLowerCase()));
                    break;
                case "Title Z-A":
                    publications.sort(Comparator.comparing(p -> p.getTitle().toLowerCase()));
                    break;
                case "Category":
                    publications.sort(Comparator.comparing(p ->
                            p.getCategory() != null && p.getCategory().getNomCategory() != null ?
                                    p.getCategory().getNomCategory().toLowerCase() : "zzz"));
                    break;
            }
        });
    }

    private void setupCategoryFilter() {
        categoryComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filteredPublications.setPredicate(publication -> {
                // If filter text is empty, display all publications
                if (searchTextField.getText() == null || searchTextField.getText().isEmpty()) {
                    // No search filter, apply only category filter
                    return applyCategoryFilter(publication, newValue) && applyVisibilityFilter(publication, visibilityComboBox.getValue());
                }

                // If filter text is not empty, combine with search filter
                String lowerCaseFilter = searchTextField.getText().toLowerCase();
                boolean matchesSearch = publication.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        publication.getContenu().toLowerCase().contains(lowerCaseFilter);

                return matchesSearch && applyCategoryFilter(publication, newValue) &&
                        applyVisibilityFilter(publication, visibilityComboBox.getValue());
            });
        });
    }

    private boolean applyCategoryFilter(Publication publication, String categoryFilter) {
        if (categoryFilter == null || categoryFilter.equals("All Categories")) {
            return true;
        }

        return publication.getCategory() != null &&
                publication.getCategory().getNomCategory() != null &&
                publication.getCategory().getNomCategory().equals(categoryFilter);
    }

    private void setupVisibilityFilter() {
        visibilityComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filteredPublications.setPredicate(publication -> {
                // If filter text is empty, display all publications
                if (searchTextField.getText() == null || searchTextField.getText().isEmpty()) {
                    // No search filter, apply only visibility filter
                    return applyVisibilityFilter(publication, newValue) &&
                            applyCategoryFilter(publication, categoryComboBox.getValue());
                }

                // If filter text is not empty, combine with search filter
                String lowerCaseFilter = searchTextField.getText().toLowerCase();
                boolean matchesSearch = publication.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        publication.getContenu().toLowerCase().contains(lowerCaseFilter);

                return matchesSearch && applyVisibilityFilter(publication, newValue) &&
                        applyCategoryFilter(publication, categoryComboBox.getValue());
            });
        });
    }

    private boolean applyVisibilityFilter(Publication publication, String visibilityFilter) {
        if (visibilityFilter == null || visibilityFilter.equals("All")) {
            return true;
        }

        return publication.getVisibility() != null && publication.getVisibility().equals(visibilityFilter);
    }

    @FXML
    private void goToDetail() {
        Publication selectedPublication = publicationsListView.getSelectionModel().getSelectedItem();
        if (selectedPublication == null) {
            showAlert("No Selection", "Please select a publication to view details.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Detail.fxml"));
            Parent root = loader.load();

            // Pass selected publication to detail controller if needed
            // DetailController controller = loader.getController();
            // controller.setPublication(selectedPublication);

            Stage stage = (Stage) btnDetail.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Detail");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load Detail.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void goToAjouterPub() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterPub.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Add Publication");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load ajouterPub.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void switchToHomeCat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/homeCat.fxml"));
            Parent homeCatView = loader.load();

            Stage stage = (Stage) switchToCatButton.getScene().getWindow();
            Scene scene = new Scene(homeCatView);
            stage.setScene(scene);
            stage.setTitle("Les categories");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Error loading homeCat.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void translatePublication() {
        String selectedLanguage = languageComboBox.getValue();
        Publication selectedPublication = publicationsListView.getSelectionModel().getSelectedItem();

        if (selectedLanguage != null && selectedPublication != null) {
            String targetLanguage = getLanguageCode(selectedLanguage);
            String translatedText = translateText(selectedPublication.getContenu(), "en", targetLanguage);

            if (translatedText != null) {
                selectedPublication.setContenu(translatedText);
                publicationsListView.refresh();
            } else {
                showAlert("Translation Error", "Failed to translate the content. Please try again later.");
            }
        } else {
            showAlert("Selection Error", "Please select a language and a publication.");
        }
    }

    private String translateText(String text, String sourceLang, String targetLang) {
        try {
            String apiUrl = String.format(
                    "https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s",
                    text.replace(" ", "%20"), sourceLang, targetLang
            );

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getJSONObject("responseData").getString("translatedText");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getLanguageCode(String language) {
        switch (language) {
            case "English": return "en";
            case "French": return "fr";
            case "Spanish": return "es";
            case "German": return "de";
            case "Arabic": return "ar";
            default: return "en"; // Default to English
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}