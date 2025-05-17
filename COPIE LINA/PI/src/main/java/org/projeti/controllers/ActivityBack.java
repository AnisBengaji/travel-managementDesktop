package org.projeti.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.projeti.Service.ActivityService;
import org.projeti.Service.DestinationService;
import org.projeti.entites.Activity;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ActivityBack {

    @FXML
    private Button btnajout;
    @FXML
    private Button btnsup;
    @FXML
    private TextField tfrechercher;
    @FXML
    private ComboBox<String> cbtri;
    @FXML
    private ComboBox<String> cbdestination;
    @FXML
    private TableColumn<Activity, Float> colactivity_price;
    @FXML
    private TableColumn<Activity, String> coldescription;
    @FXML
    private TableColumn<Activity, String> colidDestination;
    @FXML
    private TableColumn<Activity, String> colimage_activity;
    @FXML
    private TableColumn<Activity, String> colimage_activity2;
    @FXML
    private TableColumn<Activity, String> colimage_activity3;
    @FXML
    private TableColumn<Activity, String> colnom_activity;
    @FXML
    private TableColumn<Activity, String> coltype;
    @FXML
    private TableColumn<Activity, Date> coldt_activite;
    @FXML
    private Button modifier;
    @FXML
    private TableView<Activity> tabActivity;
    @FXML
    private TextField tfactivity_price;
    @FXML
    private TextField tfdescription;
    @FXML
    private TextField tfimage_activity;
    @FXML
    private TextField tfimage_activity2;
    @FXML
    private TextField tfimage_activity3;
    @FXML
    private TextField tfnom_activity;
    @FXML
    private TextField tftype;
    @FXML
    private ComboBox<String> cbCurrency;
    @FXML
    private DatePicker dpDateActivite;
    @FXML
    private Button btnUploadImage1;
    @FXML
    private Button btnUploadImage2;
    @FXML
    private Button btnUploadImage3;

    private ObservableList<Activity> obslist;
    private final ActivityService as = new ActivityService();
    private final DestinationService ds = new DestinationService();
    private final String IMAGE_FOLDER = "C:\\Users\\21658\\Desktop\\ines_pidev\\Pi-3A4-tripit\\src\\main\\resources\\img";

    @FXML
    public void initialize() throws SQLException {
        try {
            cbdestination.setItems(FXCollections.observableArrayList(ds.getPaysVille()));
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Impossible de charger les destinations : " + e.getMessage());
            throw e;
        }
        if (tabActivity == null) {
            showErrorAlert("Erreur", "Tableau des activités non initialisé. Vérifiez le fichier FXML.");
            return;
        }
        tabActivity.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        refresh(as.showAll());
        cbtri.setItems(FXCollections.observableArrayList("Nom", "Prix", "Type"));
        recherche_avance();
        cbCurrency.setItems(FXCollections.observableArrayList("TND", "USD", "EUR", "CAD"));
        cbCurrency.setValue("TND");
        cbCurrency.valueProperty().addListener((obs, oldValue, newValue) -> tabActivity.refresh());
    }

    void refresh(List<Activity> activities) {
        if (tabActivity == null) {
            showErrorAlert("Erreur", "Tableau des activités non initialisé. Vérifiez le fichier FXML.");
            return;
        }

        colnom_activity.setCellValueFactory(new PropertyValueFactory<>("nom_activity"));
        colimage_activity.setCellValueFactory(new PropertyValueFactory<>("image_activity"));
        colimage_activity2.setCellValueFactory(new PropertyValueFactory<>("image_activity2"));
        colimage_activity3.setCellValueFactory(new PropertyValueFactory<>("image_activity3"));
        coltype.setCellValueFactory(new PropertyValueFactory<>("type"));
        coldescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colactivity_price.setCellFactory(col -> new TableCell<Activity, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Activity activity = getTableRow().getItem();
                    String currency = cbCurrency.getValue();
                    double basePrice = activity.getActivity_price();


                }
            }
        });
        coldt_activite.setCellValueFactory(new PropertyValueFactory<>("date_activite"));
        colidDestination.setCellValueFactory(cell -> {
            try {
                String payville = ds.getPaysVilleById(cell.getValue().getIdDestination());
                return new SimpleStringProperty(payville);
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur: " + e.getMessage());
            }
        });

        obslist = FXCollections.observableArrayList(activities);
        tabActivity.setItems(obslist);
        if (obslist.isEmpty()) {
            System.out.println("Aucune activité trouvée dans la base de données.");
        }
    }

    @FXML
    void ajouter(ActionEvent event) {
        if (!controle_saisie()) {
            return;
        }
        try {
            LocalDate localDate = dpDateActivite.getValue();
            Date date_activite = localDate != null ? Date.valueOf(localDate) : null;

            Activity a = new Activity(
                    tfnom_activity.getText(),
                    tfimage_activity.getText(),
                    tfimage_activity2.getText(),
                    tfimage_activity3.getText(),
                    tftype.getText(),
                    tfdescription.getText(),
                    Float.parseFloat(tfactivity_price.getText()),
                    date_activite,
                    ds.getIdByPaysVille(cbdestination.getValue())
            );
            as.insert(a);
            showInfoAlert("Succès", "Activité insérée avec succès!");
            refresh(as.showAll());
            viderChamps();
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors de l'insertion de l'activité : " + e.getMessage());
            e.printStackTrace();
        }
    }

    boolean controle_saisie() {
        StringBuilder erreur = new StringBuilder();
        if (tfnom_activity.getText().trim().isEmpty()) {
            erreur.append("Nom de l'activité vide\n");
        }
        if (tftype.getText().trim().isEmpty()) {
            erreur.append("Type d'activité vide\n");
        }
        if (tfdescription.getText().trim().isEmpty()) {
            erreur.append("Description vide\n");
        }
        if (tfactivity_price.getText().trim().isEmpty()) {
            erreur.append("Prix de l'activité vide\n");
        } else {
            try {
                float price = Float.parseFloat(tfactivity_price.getText());
                if (price < 0) {
                    erreur.append("Le prix de l'activité doit être positif\n");
                }
            } catch (NumberFormatException e) {
                erreur.append("Le prix de l'activité doit être un nombre valide\n");
            }
        }
        if (dpDateActivite.getValue() == null) {
            erreur.append("Date de l'activité non sélectionnée\n");
        }
        if (cbdestination.getValue() == null) {
            erreur.append("Destination non sélectionnée\n");
        }
        if (!erreur.isEmpty()) {
            showErrorAlert("Erreur de saisie", erreur.toString());
            return false;
        }
        return true;
    }

    private void viderChamps() {
        tfnom_activity.clear();
        tfimage_activity.clear();
        tfimage_activity2.clear();
        tfimage_activity3.clear();
        tftype.clear();
        tfdescription.clear();
        tfactivity_price.clear();
        dpDateActivite.setValue(null);
        cbdestination.setValue(null);
    }

    @FXML
    void supprimer(ActionEvent event) {
        Activity selected = tabActivity.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Avertissement", "Veuillez sélectionner une activité à supprimer.");
            return;
        }
        try {
            as.delete(selected.getId_activity());
            showInfoAlert("Succès", "Activité supprimée avec succès!");
            refresh(as.showAll());
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void modifier(ActionEvent event) {
        Activity selected = tabActivity.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Avertissement", "Veuillez sélectionner une activité à modifier.");
            return;
        }
        if (!controle_saisie()) {
            return;
        }
        try {
            selected.setNom_activity(tfnom_activity.getText());
            selected.setImage_activity(tfimage_activity.getText());
            selected.setImage_activity2(tfimage_activity2.getText());
            selected.setImage_activity3(tfimage_activity3.getText());
            selected.setType(tftype.getText());
            selected.setDescription(tfdescription.getText());
            selected.setActivity_price(Float.parseFloat(tfactivity_price.getText()));
            LocalDate localDate = dpDateActivite.getValue();
            selected.setDate_activite(localDate != null ? Date.valueOf(localDate) : null);
            selected.setIdDestination(ds.getIdByPaysVille(cbdestination.getValue()));
            as.update(selected);
            showInfoAlert("Succès", "Activité modifiée avec succès!");
            refresh(as.showAll());
            viderChamps();
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void gotoDestination(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/destination-back.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tfactivity_price.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Destination Management");
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible de charger destination-back.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void tri(ActionEvent event) {
        try {
            refresh(as.triParCritere(cbtri.getValue()));
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors du tri : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void recherche_avance() {
        try {
            ObservableList<Activity> data = FXCollections.observableArrayList(as.showAll());
            FilteredList<Activity> filteredList = new FilteredList<>(data, b -> true);
            tfrechercher.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredList.setPredicate(activity -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return activity.getNom_activity().toLowerCase().contains(lowerCaseFilter) ||
                            activity.getType().toLowerCase().contains(lowerCaseFilter) ||
                            activity.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                            String.valueOf(activity.getActivity_price()).contains(lowerCaseFilter);
                });
                refresh(filteredList);
            });
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors de l'initialisation de la recherche : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void uploadImage(TextField targetField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers image", "*.jpg", "*.png", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) tfactivity_price.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                Path targetDir = Paths.get(IMAGE_FOLDER);
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                Path targetFile = targetDir.resolve(file.getName());
                Files.copy(file.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
                targetField.setText(file.getName());
            } catch (IOException ex) {
                showErrorAlert("Erreur d'upload", "Impossible d'uploader le fichier : " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @FXML
    void gotoDetails(ActionEvent event) {
        Activity selected = tabActivity.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Avertissement", "Veuillez sélectionner une activité à afficher en détails.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activity-details.fxml"));
            Parent root = loader.load();
            ActivityDetailsController detailsController = loader.getController();
            detailsController.setActivity(selected);
            Stage stage = (Stage) tfactivity_price.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Détails de l'activité");
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible de charger la vue des détails : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void uploadImage1(ActionEvent event) {
        uploadImage(tfimage_activity);
    }

    @FXML
    void uploadImage2(ActionEvent event) {
        uploadImage(tfimage_activity2);
    }

    @FXML
    void uploadImage3(ActionEvent event) {
        uploadImage(tfimage_activity3);
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}