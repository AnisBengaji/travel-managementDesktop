package org.projeti.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.projeti.Service.OffreService;
import org.projeti.entites.Offre;

import java.io.File;
import java.sql.SQLException;
public class ModifierOffreController {

    @FXML
    private TextField txtTitre, txtDescription, txtPrix, txtTutorial, txtDestination;

    private Offre offreSelectionnee;
    private OffreDetailController offreDetailController;
    private final OffreService offreService = new OffreService();
    private String imagePath;  // Pour stocker le chemin de l'image

    @FXML
    private ImageView imageView;  // ImageView pour afficher l'image

    @FXML
    private void handleModifierOffre() {
        // Récupération des données des champs
        String titre = txtTitre.getText().trim();
        String description = txtDescription.getText().trim();
        String tutorial = txtTutorial.getText().trim();
        String destination = txtDestination.getText().trim();

        // Validation des champs
        if (titre.isEmpty() || description.isEmpty() || tutorial.isEmpty() || destination.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        // Récupérer le prix
        float prix;
        try {
            prix = Float.parseFloat(txtPrix.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix doit être un nombre valide.");
            return;
        }

        // Si une nouvelle image a été choisie, on récupère son chemin via le FileChooser

        if (imageView.getImage() != null) {
            imagePath = imageView.getImage().getUrl();  // Récupère le chemin de l'image (URL absolu)
        }

        // Mise à jour de l'offre avec les nouvelles données
        offreSelectionnee.setTitre(titre);
        offreSelectionnee.setDescription(description);
        offreSelectionnee.setPrix(prix);
       // offreSelectionnee.setTutorial(tutorial);
        offreSelectionnee.setDestination(destination);

        // Mise à jour du chemin de l'image dans l'objet Offre
        if (imagePath != null && !imagePath.isEmpty()) {
            offreSelectionnee.setImage(imagePath);  // Mise à jour de l'image dans l'objet
        }

        // Mise à jour de l'offre dans la base de données
        try {
            offreService.update(offreSelectionnee);
            offreDetailController.refreshTable();
            showAlert("Succès", "Offre modifiée avec succès.");
            ((Stage) txtTitre.getScene().getWindow()).close();
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors de la mise à jour de l'offre: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setOffreData(Offre offre, OffreDetailController offreDetailController) {
        this.offreSelectionnee = offre;
        this.offreDetailController = offreDetailController;
        txtTitre.setText(offre.getTitre());
        txtDescription.setText(offre.getDescription());
        txtPrix.setText(String.valueOf(offre.getPrix()));
        //txtTutorial.setText(offre.getTutorial());
        txtDestination.setText(offre.getDestination());

        // Vérifier si l'offre a une image et l'afficher
        if (offre.getImage() != null && !offre.getImage().isEmpty()) {
            Image image = new Image("file:" + offre.getImage());  // Charger l'image depuis le chemin enregistré
            imageView.setImage(image);  // Afficher l'image dans l'ImageView
        } else {
            imageView.setImage(null);  // Si aucune image n'est définie
        }
    }

    @FXML
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            // Récupérer le chemin de l'image sélectionnée
            imagePath = file.toURI().toString();  // Chemin absolu de l'image
            imageView.setImage(new Image(imagePath)); // Afficher l'image dans l'ImageView
        }
    }
}
