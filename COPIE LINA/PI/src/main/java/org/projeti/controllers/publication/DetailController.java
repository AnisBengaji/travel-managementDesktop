package org.projeti.controllers.publication;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.projeti.Service.PublicationService;
import org.projeti.Service.CategorieService;
import org.projeti.entites.Category;
import org.projeti.entites.Publication;
import org.projeti.entites.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DetailController {

    @FXML private TableView<Publication> publicationsTableView;
    @FXML private TableColumn<Publication, Integer> idColumn;
    @FXML private TableColumn<Publication, String> titleColumn;
    @FXML private TableColumn<Publication, String> contentColumn;
    @FXML private TableColumn<Publication, String> authorColumn;
    @FXML private TableColumn<Publication, String> dateColumn;
    @FXML private TableColumn<Publication, String> visibilityColumn;
    @FXML private TableColumn<Publication, String> imageColumn;
    @FXML private TableColumn<Publication, String> idCategory;
    @FXML private TableColumn<Publication, Void> actionColumn;

    @FXML private Button buttonreturn;
    @FXML private Button downloadPdfButton;
    @FXML private Button downloadExcelButton;
    @FXML private Button categoryButton;
    @FXML private Button commentsButton;

    private PublicationService publicationService = new PublicationService();
    private CategorieService categorieService = new CategorieService();
    private ObservableList<Publication> publications = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        try {
            loadPublications();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load publications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_publication"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        authorColumn.setCellValueFactory(cellData -> {
            User author = cellData.getValue().getAuthor();
            return new SimpleStringProperty(author != null && author.getEmail() != null ? author.getEmail() : "Unknown");
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_publication"));
        visibilityColumn.setCellValueFactory(new PropertyValueFactory<>("visibility"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        idCategory.setCellValueFactory(cellData -> {
            Category category = cellData.getValue().getCategory();
            return new SimpleStringProperty(category != null ? category.getIdCategory() + " - " + category.getNomCategory() : "No Category");
        });

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                try {
                    ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/newedit.png")));
                    editIcon.setFitHeight(20);
                    editIcon.setFitWidth(20);
                    editButton.setGraphic(editIcon);
                    editButton.getStyleClass().add("edit-button");

                    ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/newdel.png")));
                    deleteIcon.setFitHeight(20);
                    deleteIcon.setFitWidth(20);
                    deleteButton.setGraphic(deleteIcon);
                    deleteButton.getStyleClass().add("delete-button");
                } catch (Exception e) {
                    System.err.println("Error loading icons: " + e.getMessage());
                    editButton.setText("Edit");
                    deleteButton.setText("Delete");
                }

                editButton.setOnAction(event -> {
                    Publication publication = getTableView().getItems().get(getIndex());
                    showEditDialog(publication);
                });

                deleteButton.setOnAction(event -> {
                    Publication publication = getTableView().getItems().get(getIndex());
                    deletePublication(publication);
                });

                HBox buttonsBox = new HBox(10, editButton, deleteButton);
                setGraphic(buttonsBox);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });

        publicationsTableView.setItems(publications);
    }

    private void deletePublication(Publication publication) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this publication?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                publicationService.delete(publication);
                publications.remove(publication);
                publicationsTableView.refresh();
                showAlert("Success", "Publication deleted successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete publication: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadPublications() throws SQLException {
        List<Publication> publicationList = publicationService.showAll();
        publications.setAll(publicationList);
    }

    @FXML
    private void GoBackHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Detail.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) buttonreturn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load homepub.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailCat.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) categoryButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Categories");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load DetailCat.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToComments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailComments.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) commentsButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Comments");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load DetailComments.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showEditDialog(Publication publication) {
        Dialog<Pair<Boolean, Publication>> dialog = new Dialog<>();
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.setTitle("Edit Publication");
        dialog.setHeaderText("Modify Publication Details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField titleField = new TextField(publication.getTitle());
        titleField.setPromptText("Enter publication title");
        TextArea contentArea = new TextArea(publication.getContenu());
        contentArea.setPromptText("Enter publication content");
        contentArea.setPrefRowCount(4);
        ComboBox<String> visibilityCombo = new ComboBox<>(FXCollections.observableArrayList("Public", "Private"));
        visibilityCombo.setValue(publication.getVisibility() != null && publication.getVisibility().equals("Private") ? "Private" : "Public");
        ComboBox<Category> categoryCombo = new ComboBox<>();
        Label imagePathLabel = new Label(publication.getImage() != null ? publication.getImage() : "No image selected");
        imagePathLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
        Button browseImageButton = new Button("Browse Image");

        // Populate categories
        try {
            List<Category> categories = categorieService.showAll();
            categoryCombo.setItems(FXCollections.observableArrayList(categories));
            categoryCombo.setConverter(new javafx.util.StringConverter<Category>() {
                @Override
                public String toString(Category category) {
                    return category != null ? category.getNomCategory() : "";
                }

                @Override
                public Category fromString(String string) {
                    return null;
                }
            });
            categoryCombo.getItems().stream()
                    .filter(c -> c.getIdCategory() == (publication.getCategory() != null ? publication.getCategory().getIdCategory() : 0))
                    .findFirst()
                    .ifPresent(categoryCombo::setValue);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage());
            e.printStackTrace();
        }

        // Image file chooser
        final File[] selectedImage = {null};
        browseImageButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedImage[0] = file;
                imagePathLabel.setText(file.getAbsolutePath());
                imagePathLabel.setStyle("-fx-text-fill: #333; -fx-font-style: normal;");
            }
        });

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(contentArea, 1, 1);
        grid.add(new Label("Visibility:"), 0, 2);
        grid.add(visibilityCombo, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryCombo, 1, 3);
        grid.add(new Label("Image:"), 0, 4);
        grid.add(browseImageButton, 1, 4);
        grid.add(imagePathLabel, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        titleField.textProperty().addListener((obs, old, newVal) -> saveButton.setDisable(!isInputValid(titleField, contentArea, visibilityCombo, categoryCombo)));
        contentArea.textProperty().addListener((obs, old, newVal) -> saveButton.setDisable(!isInputValid(titleField, contentArea, visibilityCombo, categoryCombo)));
        visibilityCombo.valueProperty().addListener((obs, old, newVal) -> saveButton.setDisable(!isInputValid(titleField, contentArea, visibilityCombo, categoryCombo)));
        categoryCombo.valueProperty().addListener((obs, old, newVal) -> saveButton.setDisable(!isInputValid(titleField, contentArea, visibilityCombo, categoryCombo)));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (!isInputValid(titleField, contentArea, visibilityCombo, categoryCombo)) {
                    showAlert("Invalid Input", "Required fields must be filled.");
                    return null;
                }

                publication.setTitle(titleField.getText());
                publication.setContenu(contentArea.getText());
                publication.setDate_publication(Date.valueOf(LocalDate.now()));
                User author = new User();
                author.setId(25);
                publication.setAuthor(author);
                publication.setVisibility(visibilityCombo.getValue());
                publication.setImage(selectedImage[0] != null ? selectedImage[0].getAbsolutePath() : null);
                Category category = categoryCombo.getValue();
                publication.setCategory(category);
                return new Pair<>(true, publication);
            }
            return null;
        });

        Optional<Pair<Boolean, Publication>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            if (pair.getKey()) {
                try {
                    publicationService.update(pair.getValue());
                    publicationsTableView.refresh();
                    showAlert("Success", "Publication updated successfully!");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to update publication: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isInputValid(TextField title, TextArea content, ComboBox<String> visibility, ComboBox<Category> category) {
        return !title.getText().trim().isEmpty() &&
                !content.getText().trim().isEmpty() &&
                visibility.getValue() != null &&
                category.getValue() != null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void downloadPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        Stage stage = (buttonreturn != null && buttonreturn.getScene() != null) ? (Stage) buttonreturn.getScene().getWindow() : null;
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Draw logo
                    PDImageXObject logo = PDImageXObject.createFromFile("src/main/resources/icons/iconTripit.png", document);
                    contentStream.drawImage(logo, 50, 700, 100, 50);

                    // Title
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                    contentStream.newLineAtOffset(160, 730);
                    contentStream.showText("Trip It - Publications Report");
                    contentStream.endText();

                    // Separator line
                    contentStream.setLineWidth(1);
                    contentStream.moveTo(50, 690);
                    contentStream.lineTo(550, 690);
                    contentStream.stroke();

                    // Table headers
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(50, 670);
                    contentStream.showText("ID");
                    contentStream.newLineAtOffset(50, 0);
                    contentStream.showText("Title");
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText("Author");
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText("Date");
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText("Visibility");
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText("Category");
                    contentStream.endText();

                    // Table rows
                    int y = 650;
                    for (Publication publication : publicationsTableView.getItems()) {
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.newLineAtOffset(50, y);
                        contentStream.showText(String.valueOf(publication.getId_publication()));
                        contentStream.newLineAtOffset(50, 0);
                        String title = publication.getTitle() != null ? publication.getTitle() : "";
                        if (title.length() > 20) title = title.substring(0, 17) + "...";
                        contentStream.showText(title);
                        contentStream.newLineAtOffset(150, 0);
                        String author = (publication.getAuthor() != null && publication.getAuthor().getEmail() != null) ? publication.getAuthor().getEmail() : "Unknown";
                        if (author.length() > 15) author = author.substring(0, 12) + "...";
                        contentStream.showText(author);
                        contentStream.newLineAtOffset(100, 0);
                        contentStream.showText(publication.getDate_publication() != null ? publication.getDate_publication().toString() : "");
                        contentStream.newLineAtOffset(100, 0);
                        contentStream.showText(publication.getVisibility() != null ? publication.getVisibility() : "");
                        contentStream.newLineAtOffset(100, 0);
                        String category = publication.getCategory() != null ? publication.getCategory().getNomCategory() : "None";
                        if (category.length() > 15) category = category.substring(0, 12) + "...";
                        contentStream.showText(category);
                        contentStream.endText();

                        contentStream.setLineWidth(0.5f);
                        contentStream.moveTo(50, y - 5);
                        contentStream.lineTo(550, y - 5);
                        contentStream.stroke();

                        y -= 20;
                    }

                    // Footer
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                    contentStream.newLineAtOffset(50, 30);
                    contentStream.showText("Thank you for using Trip It!");
                    contentStream.endText();
                }

                document.save(file);
                showAlert("Success", "PDF report saved successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to generate PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void downloadExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        Stage stage = (buttonreturn != null && buttonreturn.getScene() != null) ? (Stage) buttonreturn.getScene().getWindow() : null;
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Publications");

                Row headerRow = sheet.createRow(0);
                String[] columns = {"ID", "Title", "Content", "Author", "Date", "Visibility", "Image", "Category"};
                for (int i = 0; i < columns.length; i++) {
                    headerRow.createCell(i).setCellValue(columns[i]);
                }

                int rowNum = 1;
                for (Publication publication : publicationsTableView.getItems()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(publication.getId_publication());
                    row.createCell(1).setCellValue(publication.getTitle() != null ? publication.getTitle() : "");
                    row.createCell(2).setCellValue(publication.getContenu() != null ? publication.getContenu() : "");
                    row.createCell(3).setCellValue(publication.getAuthor() != null && publication.getAuthor().getEmail() != null ? publication.getAuthor().getEmail() : "Unknown");
                    row.createCell(4).setCellValue(publication.getDate_publication() != null ? publication.getDate_publication().toString() : "");
                    row.createCell(5).setCellValue(publication.getVisibility() != null ? publication.getVisibility() : "");
                    row.createCell(6).setCellValue(publication.getImage() != null ? publication.getImage() : "");
                    row.createCell(7).setCellValue(publication.getCategory() != null ? publication.getCategory().getNomCategory() : "None");
                }

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                showAlert("Success", "Excel file saved successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to save Excel file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}