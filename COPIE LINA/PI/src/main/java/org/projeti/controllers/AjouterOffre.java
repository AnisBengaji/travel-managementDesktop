package org.projeti.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.projeti.Service.OffreService;
import org.projeti.entites.Offre;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class AjouterOffre {
    @FXML
    public TextField rating;
    @FXML
    public TextField ratingcount;
    @FXML
    private TextField titreField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField prixField;
    @FXML
    private TextField destinationField;
    @FXML
    private ImageView imageView;
    @FXML
    private Button uploadButton;

    private final OffreService offreService = new OffreService();
    private int idOffre = -1; // Stocke l'ID pour la modification
    private String imagePath = null; // Chemin de l'image sélectionnée

    public void setOffreAModifier(Offre offre) {
        idOffre = offre.getIdOffre();
        titreField.setText(offre.getTitre());
        descriptionField.setText(offre.getDescription());
        prixField.setText(String.valueOf(offre.getPrix()));
        destinationField.setText(offre.getDestination());
        rating.setText(String.valueOf(offre.getRating()));
        ratingcount.setText(String.valueOf(offre.getRating_count()));
        
        if (offre.getImage() != null && !offre.getImage().isEmpty()) {
            imagePath = offre.getImage();
            imageView.setImage(new Image(imagePath));
        }
    }

    @FXML
    private void sauvegarderOffre() {
        System.out.println("Le bouton 'Sauvegarder' a été cliqué !");

        // Récupérer les valeurs des champs
        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();
        String prixText = prixField.getText().trim();
        String destination = destinationField.getText().trim();
        String ratingText = rating.getText().trim();
        String ratingCountText = ratingcount.getText().trim();

        // Vérification que les champs ne sont pas vides
        if (titre.isEmpty() || description.isEmpty() || prixText.isEmpty() || destination.isEmpty() || ratingText.isEmpty() || ratingCountText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            // Vérifier si le prix est valide
            float prix = Float.parseFloat(prixText);
            double ratingValue = Double.parseDouble(ratingText);
            int ratingCount = Integer.parseInt(ratingCountText);

            // Créer l'objet Offre
            Offre offre = new Offre(idOffre, titre, description, prix, destination, imagePath);
            offre.setRating(ratingValue);
            offre.setRating_count(ratingCount);
            System.out.println("Objet Offre créé : " + offre);

            // Ajouter ou modifier l'offre dans la base de données
            if (idOffre == -1) {
                boolean isAdded = offreService.insert(offre) > 0;
                if (isAdded) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre ajoutée avec succès !");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ajout de l'offre.");
                }
            } else {
                boolean isUpdated = offreService.update(offre) > 0;
                if (isUpdated) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre modifiée avec succès !");
                    idOffre = -1; // Réinitialiser après modification
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la modification de l'offre.");
                }
            }

            clearFields();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez entrer des valeurs numériques valides pour le prix, le rating et le nombre d'avis.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ajout/modification de l'offre.");
        }
    }

    @FXML
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            imagePath = file.toURI().toString(); // Chemin de l'image
            imageView.setImage(new Image(imagePath)); // Afficher l'image sélectionnée
        }
    }

    @FXML
    private void clearFields() {
        titreField.clear();
        descriptionField.clear();
        prixField.clear();
        destinationField.clear();
        rating.clear();
        ratingcount.clear();
        imageView.setImage(null);
        imagePath = null;
        idOffre = -1;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void afficherOffres(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OffreDetails.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Offres");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la liste des offres");
        }
    }
}
