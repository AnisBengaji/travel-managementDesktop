package org.projeti.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.projeti.Service.TutorialService;
import org.projeti.entites.Tutorial;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjouterTutorial {

    @FXML
    private TextField url;
    @FXML
    private TextField offre_id;
    @FXML
    private TextField nomField;
    @FXML
    private DatePicker dateDebutField;
    @FXML
    private DatePicker dateFinField;
    @FXML
    private TextField prixField;

    private final TutorialService tutorialService = new TutorialService();
    private int idTutorial = -1; // Stocke l'ID pour la modification

    @FXML
    private void sauvegarderTutorial() {
        try {
            // Récupérer et valider les valeurs des champs
            String nom = validateTextField(nomField, "Nom du tutoriel");
            LocalDate dateDebut = validateDatePicker(dateDebutField, "Date de début");
            LocalDate dateFin = validateDatePicker(dateFinField, "Date de fin");
            String urlText = validateTextField(url, "URL");
            String offreIdText = validateTextField(offre_id, "ID de l'offre");
            float prix = validatePrix(prixField);

            // Vérifier que la date de fin est après la date de début
            if (dateFin.isBefore(dateDebut)) {
                showAlert(Alert.AlertType.ERROR, "Erreur de dates", 
                    "La date de fin doit être après la date de début.");
                return;
            }

            // Créer l'objet Tutorial
            Tutorial tutorial = new Tutorial(
                idTutorial,
                Integer.parseInt(offreIdText),
                nom,
                dateDebut,
                dateFin,
                prix,
                urlText
            );

            // Sauvegarder ou mettre à jour le tutoriel
            boolean isSuccessful;
            if (idTutorial == -1) {
                isSuccessful = tutorialService.insert(tutorial) > 0;
                if (isSuccessful) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "Tutoriel ajouté avec succès !");
                }
            } else {
                isSuccessful = tutorialService.update(tutorial) > 0;
                if (isSuccessful) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "Tutoriel modifié avec succès !");
                    idTutorial = -1;
                }
            }

            if (!isSuccessful) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Une erreur est survenue lors de l'enregistrement du tutoriel.");
            } else {
                clearFields();
                // Retourner à la liste des tutoriels après sauvegarde
                afficherTutorials(null);
            }

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", 
                "Une erreur est survenue lors de l'enregistrement du tutoriel.");
            e.printStackTrace();
        }
    }

    private String validateTextField(TextField field, String fieldName) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Le champ '" + fieldName + "' est requis.");
        }
        return value;
    }

    private LocalDate validateDatePicker(DatePicker datePicker, String fieldName) {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            throw new IllegalArgumentException("Le champ '" + fieldName + "' est requis.");
        }
        return date;
    }

    private float validatePrix(TextField prixField) {
        String prixText = prixField.getText().trim();
        if (prixText.isEmpty()) {
            throw new IllegalArgumentException("Le champ 'Prix' est requis.");
        }
        try {
            float prix = Float.parseFloat(prixText);
            if (prix < 0) {
                throw new IllegalArgumentException("Le prix ne peut pas être négatif.");
            }
            return prix;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le prix doit être un nombre valide.");
        }
    }

    public void setTutorialAModifier(Tutorial tutorial) {
        if (tutorial == null) {
            throw new IllegalArgumentException("Le tutoriel ne peut pas être null.");
        }
        
        idTutorial = tutorial.getId_tutorial();
        nomField.setText(tutorial.getNom_tutorial());
        dateDebutField.setValue(tutorial.getDateDebut());
        dateFinField.setValue(tutorial.getDateFin());
        prixField.setText(String.valueOf(tutorial.getPrix_tutorial()));
        url.setText(tutorial.getUrl());
        offre_id.setText(String.valueOf(tutorial.getOffre_id()));
    }

    @FXML
    private void clearFields() {
        nomField.clear();
        dateDebutField.setValue(null);
        dateFinField.setValue(null);
        prixField.clear();
        url.clear();
        offre_id.clear();
        idTutorial = -1;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void afficherTutorials(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TutorialDetails.fxml"));
            Parent root = loader.load();
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) nomField.getScene().getWindow();
            }
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Tutoriels");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors du chargement de la liste des tutoriels");
            e.printStackTrace();
        }
    }
}
