package org.projeti.controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import java.io.IOException;

import javafx.scene.Parent;
import javafx.scene.Scene;

import org.projeti.Service.UserSession;

import java.net.URL;

import javafx.scene.Node;

import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private Node sidebar;
    @FXML
    private VBox content;
    @FXML
    private ImageView userIcon;  // Make sure this is declared like this
    @FXML
    private Label userNameLabel;
    @FXML
    private Label homelabel ,Offrelabel,distinationlabel,publicationlabel,eventlabel,tuto;
    // Label for the greeting
    @FXML
    private void initialize() {
        // Retrieve the user's name from the UserSession class
        String userName = UserSession.getName();

        // Set the greeting dynamically
        if (userName != null && !userName.isEmpty()) {
            userNameLabel.setText("Hi, " + userName);  // Display the name in the greeting
        }

        // Add event listeners for menu items
        if (homelabel != null)
            homelabel.setOnMouseClicked(event -> loadSection("Main.fxml"));
        if (Offrelabel != null)
            Offrelabel.setOnMouseClicked(event -> loadSection("OffreDetails.fxml"));
        if (distinationlabel != null)
            distinationlabel.setOnMouseClicked(event -> loadSection("activity-details.fxml"));
        if (publicationlabel != null)
            publicationlabel.setOnMouseClicked(event -> loadSection("homepub.fxml"));

        if (eventlabel != null)
            eventlabel.setOnMouseClicked(event -> loadSection("views/Evenement.fxml"));
        if (tuto != null)
            tuto.setOnMouseClicked(event -> loadSection("TutorialDetails.fxml"));

    }


/*
    @FXML
    private void initialize() {
        // Retrieve the user's name from the UserSession class
        String userName = UserSession.getName();

        // Set the greeting dynamically
        if (userName != null && !userName.isEmpty()) {
            userNameLabel.setText("Hi, " + userName);  // Display the name in the greeting
        }
    } */

    /*@FXML
    private void showReservation() {
        loadSection("Reservation.fxml");
    }

    @FXML
    private void showEvenement() {
        loadSection("Evenement.fxml");
    }
    @FXML
    private void showhomePub(){
        loadView("social.fxml");
    }
    @FXML
    private void showdesination(){
        loadView("destination-back.fxml");
    }
    @FXML
    private void goToProfile(){
        loadView("AjouterUser.fxml");
    }*/
    private void loadSection(String fxmlFile) {
        try {
            String userName = UserSession.getName();
            String filePath = "/" + fxmlFile;
            System.out.println("Trying to load: " + filePath);

            URL resource = getClass().getResource(filePath);
            if (resource == null) {
                throw new IOException("FXML file not found: " + filePath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            AnchorPane section = loader.load();

            // Ensure the section fills the available space in StackPane
            section.prefWidthProperty().bind(content.widthProperty());
            section.prefHeightProperty().bind(content.heightProperty());

            content.getChildren().setAll(section); // Replace current content

            System.out.println("Successfully loaded: " + fxmlFile);
        } catch (IOException e) {
            System.err.println("⚠ Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignOut() {
        try {
            // Load the Sign-In Page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signIn.fxml"));
            Parent root = loader.load();

            // Create a new stage for Sign-In
            Stage signInStage = new Stage();
            signInStage.setScene(new Scene(root));
            signInStage.show();

            // Close the current window (Main.fxml) using all windows
            Stage currentStage = (Stage) Stage.getWindows().stream()
                    .filter(window -> window.isShowing())
                    .findFirst()
                    .orElse(null);

            if (currentStage != null) {
                currentStage.close();  // Close the main stage
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   /* private void loadView(String fxmlFile) {
        try {
            // Vérifier si le fichier FXML existe
            URL fxmlLocation = getClass().getResource("/" + fxmlFile);
            if (fxmlLocation == null) {
                throw new IOException("Fichier FXML non trouvé : " + fxmlFile);
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Récupérer la scène actuelle et vérifier si elle existe
            Stage stage = (Stage) (root.getScene() != null ? root.getScene().getWindow() : null);
            if (stage == null) {
                stage = new Stage();
            }

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de " + fxmlFile + " : " + e.getMessage());
            e.printStackTrace();
        }

    }*/
}