package org.projeti.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.projeti.Service.UserService;
import org.projeti.entites.User;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class DetailControllerUser {
    @FXML
    private AnchorPane anchorPane; // Add this to get the AnchorPane for the layout

    @FXML
    private TableView<User> tableView;
    @FXML
    private TableColumn<User, String> colNom;
    @FXML
    private TableColumn<User, String> colPrenom;
    @FXML
    private TableColumn<User, Integer> colTel;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, Void> colActions;
    @FXML
    private TableColumn<User, Void> colUpdate;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;

    private final ObservableList<User> filteredList = FXCollections.observableArrayList();
    private final UserService userService = new UserService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("num_tel"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        addDeleteButtonToTable();
        addUpdateButtonToTable();
        loadUsers();
        // Set search button action
        searchButton.setOnAction(event -> handleSearch());
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminInterface.fxml")); // Change path as needed
            Parent mainPage = loader.load();
            Scene mainScene = new Scene(mainPage);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(mainScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            tableView.setItems(userList); // Reset if search is empty
            return;
        }

        filteredList.clear();

        for (User user : userList) {
            if (matchesFilter(user, searchText)) {
                filteredList.add(user);
            }
        }

        tableView.setItems(filteredList);
    }

    private boolean matchesFilter(User user, String searchText) {
        return user.getNom().toLowerCase().contains(searchText) ||
                user.getPrenom().toLowerCase().contains(searchText) ||
                user.getEmail().toLowerCase().contains(searchText) ||
                String.valueOf(user.getNum_tel()).contains(searchText) ||
                user.getRole().toLowerCase().contains(searchText);
    }
    public void loadUsers() {
        try {
            List<User> users = userService.showAll();

            // Sort users first by role priority, then by name alphabetically
            users.sort(Comparator.comparing((User user) -> {
                switch (user.getRole().toLowerCase()) {
                    case "admin":
                        return 1;
                    case "fournisseur":
                        return 2;
                    case "client":
                        return 3;
                    default:
                        return 4;
                }
            }).thenComparing(User::getNom, String.CASE_INSENSITIVE_ORDER));

            userList.setAll(users);
            tableView.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /*public void loadUsers() {
        try {
            List<User> users = userService.showAll();
            userList.setAll(users);
            tableView.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
*/
    private void addDeleteButtonToTable() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnDelete = new Button("Supprimer");

            {
                btnDelete.setStyle("-fx-background-color: linear-gradient(to bottom, #FF742C, #FF9E6B); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: normal; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-background-radius: 5px;");

                btnDelete.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        };

        colActions.setCellFactory(cellFactory);
    }

    private void addUpdateButtonToTable() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnUpdate = new Button("Modifier");

            {
                btnUpdate.setStyle("-fx-background-color: linear-gradient(to bottom, #007A8C, #66b2b2); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: normal; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-background-radius: 5px;");

                btnUpdate.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    openUpdateWindow(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnUpdate);
            }
        };

        colUpdate.setCellFactory(cellFactory);
    }

    private void openUpdateWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateUser.fxml")); // Change path as needed
            Parent root = loader.load();

            org.projeti.controllers.UpdateUser controller = loader.getController();
            controller.setUserData(user, this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'utilisateur");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de modification.");
        }
    }

    private void deleteUser(User user) {
        try {
            userService.delete(user);
            userList.remove(user);
            showAlert("Succès", "Utilisateur supprimé avec succès.");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression.");
        }
    }

    public void refreshTable() {
        loadUsers();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
