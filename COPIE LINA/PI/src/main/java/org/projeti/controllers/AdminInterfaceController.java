package org.projeti.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.control.Tooltip;
import org.projeti.Service.UserSession;

import java.io.IOException;
import java.net.URL;

public class AdminInterfaceController {

    @FXML
    private VBox sidebar;

    @FXML
    private HBox navbar;

    @FXML
    private VBox content;

    @FXML
    private TextField searchBar;

    @FXML
    private ImageView userIcon;  // Make sure this is declared like this
    @FXML
    private Label userNameLabel;  // Make sure this is declared like this

    @FXML
    private Label offre, userManagementLabel, pubLabel, Destination,Activity,Tutorial;

    @FXML
    public void initialize() {
        // Retrieve the user's name from the UserSession class
        String userName = UserSession.getName();

        // Set the greeting dynamically
        if (userName != null && !userName.isEmpty()) {
            userNameLabel.setText("Hi, " + userName);  // Display the name in the greeting
        }

        // Optionally, you can set a tooltip for the userIcon or perform other logic here.
        if (userIcon != null) {
            Tooltip tooltip = new Tooltip("Logged in as: " + userName);
            Tooltip.install(userIcon, tooltip);  // Set a tooltip on the icon with the username
        }

        // Add event listeners for menu items
        if (offre != null)
            offre.setOnMouseClicked(event -> loadSection("AjouterOffre.fxml"));
        if (userManagementLabel != null)
            userManagementLabel.setOnMouseClicked(event -> loadSection("DetailUser.fxml"));
        if (pubLabel != null)
            pubLabel.setOnMouseClicked(event -> loadSection("Detail.fxml"));
        if (Destination != null)
            Destination.setOnMouseClicked(event -> loadSection("destination-back.fxml"));
        if (Activity != null)
            Activity.setOnMouseClicked(event -> loadSection("activity-back.fxml"));
        if (Tutorial != null)
            Tutorial.setOnMouseClicked(event -> loadSection("AjouterTutorial.fxml"));
    }

    private void loadSection(String fxmlFile) {
        try {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Signin.fxml"));
            Parent root = loader.load();

            // Create a new stage for Sign-In
            Stage signInStage = new Stage();
            signInStage.setScene(new Scene(root));
            signInStage.show();

            // Close the current window (Admin Interface)
            Stage currentStage = (Stage) ((Node) sidebar).getScene().getWindow();  // Use sidebar node to get the window stage
            if (currentStage != null) {
                currentStage.close();  // Close the main stage
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AdminInterfaceController {

    @FXML
    private VBox sidebar;

    @FXML
    private HBox navbar;

    @FXML
    private VBox content;

    @FXML
    private TextField searchBar;

    @FXML
    private ImageView userIcon;

    @FXML
    private Label dashboardLabel, userManagementLabel, reportsLabel, settingsLabel;
    @FXML
    public void initialize() {
        // Add event listeners for menu items
        if (dashboardLabel != null)
            dashboardLabel.setOnMouseClicked(event -> loadSection("Main.fxml"));
        if (userManagementLabel != null)
            userManagementLabel.setOnMouseClicked(event -> loadSection("DetailUser.fxml"));
        if (reportsLabel != null)
            reportsLabel.setOnMouseClicked(event -> loadSection("Reports.fxml"));
        if (settingsLabel != null)
            settingsLabel.setOnMouseClicked(event -> loadSection("Settings.fxml"));
    }

    private void loadSection(String fxmlFile) {
        try {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Signin.fxml"));
            Parent root = loader.load();

            // Create a new stage for Sign-In
            Stage signInStage = new Stage();
            signInStage.setScene(new Scene(root));
            signInStage.show();

            // Close the current window (Admin Interface)
            Stage currentStage = (Stage) ((Node) sidebar).getScene().getWindow();  // Use sidebar node to get the window stage
            if (currentStage != null) {
                currentStage.close();  // Close the main stage
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
*/