package org.projeti.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.projeti.Service.TutorialService;
import org.projeti.entites.Tutorial;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ModifierTutorialController {
    @FXML
    private TextField txtNomTutorial;
    @FXML
    private TextField txtDateDebut;
    @FXML
    private TextField txtDateFin;
    @FXML
    private TextField txtPrixTutorial;
    @FXML
    private TextField txtOffreId;
    @FXML
    private TextField txtUrl;

    private Tutorial tutorialSelectionne;
    private TutorialDetailController tutorialDetailController;
    private final TutorialService tutorialService = new TutorialService();

    public void setTutorialData(Tutorial tutorial, TutorialDetailController tutorialDetailController) {
        this.tutorialSelectionne = tutorial;
        this.tutorialDetailController = tutorialDetailController;

        // Remplir les champs avec les données du tutoriel sélectionné
        txtNomTutorial.setText(tutorial.getNom_tutorial());
        txtDateDebut.setText(tutorial.getDateDebut().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        txtDateFin.setText(tutorial.getDateFin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        txtPrixTutorial.setText(String.valueOf(tutorial.getPrix_tutorial()));
        txtOffreId.setText(String.valueOf(tutorial.getOffre_id()));
        txtUrl.setText(tutorial.getUrl());
    }

    @FXML
    private void handleModifierTutorial() {
        try {
            // Récupération et validation des champs
            String nomTutorial = validateTextField(txtNomTutorial, "Nom du tutoriel");
            String dateDebutStr = validateTextField(txtDateDebut, "Date de début");
            String dateFinStr = validateTextField(txtDateFin, "Date de fin");
            String prixTutorialStr = validateTextField(txtPrixTutorial, "Prix");
            String offreIdStr = validateTextField(txtOffreId, "ID de l'offre");
            String url = validateTextField(txtUrl, "URL");

            // Conversion des données
            LocalDate dateDebut = LocalDate.parse(dateDebutStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate dateFin = LocalDate.parse(dateFinStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            float prixTutorial = validatePrix(prixTutorialStr);
            int offreId = validateOffreId(offreIdStr);

            // Vérification des dates
            if (dateFin.isBefore(dateDebut)) {
                showAlert(Alert.AlertType.ERROR, "Erreur de dates", 
                    "La date de fin doit être après la date de début.");
                return;
            }

            // Mettre à jour l'objet Tutorial
            tutorialSelectionne.setNom_tutorial(nomTutorial);
            tutorialSelectionne.setDateDebut(dateDebut);
            tutorialSelectionne.setDateFin(dateFin);
            tutorialSelectionne.setPrix_tutorial(prixTutorial);
            tutorialSelectionne.setOffre_id(offreId);
            tutorialSelectionne.setUrl(url);

            // Mettre à jour le tutoriel dans la base de données
            if (tutorialService.update(tutorialSelectionne) > 0) {
                // Rafraîchir la table dans le contrôleur parent
                tutorialDetailController.refreshTable();
                // Afficher un message de succès
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Tutoriel modifié avec succès.");
                // Fermer la fenêtre de modification
                ((Stage) txtNomTutorial.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "La modification du tutoriel a échoué.");
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", 
                "Erreur lors de la mise à jour du tutoriel: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) txtNomTutorial.getScene().getWindow();
        stage.close();
    }

    private String validateTextField(TextField field, String fieldName) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Le champ '" + fieldName + "' est requis.");
        }
        return value;
    }

    private float validatePrix(String prixStr) {
        try {
            float prix = Float.parseFloat(prixStr);
            if (prix < 0) {
                throw new IllegalArgumentException("Le prix ne peut pas être négatif.");
            }
            return prix;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le prix doit être un nombre valide.");
        }
    }

    private int validateOffreId(String offreIdStr) {
        try {
            int offreId = Integer.parseInt(offreIdStr);
            if (offreId <= 0) {
                throw new IllegalArgumentException("L'ID de l'offre doit être un nombre positif.");
            }
            return offreId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("L'ID de l'offre doit être un nombre valide.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}