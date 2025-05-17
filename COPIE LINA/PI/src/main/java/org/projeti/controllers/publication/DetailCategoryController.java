package org.projeti.controllers.publication;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.poi.ss.usermodel.Cell;
import org.projeti.Service.CategorieService;
import org.projeti.entites.Category;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DetailCategoryController implements Initializable {

    @FXML private TableView<Category> categoriesTableView;
    @FXML private TableColumn<Category, Integer> categoryIdColumn;
    @FXML private TableColumn<Category, String> categoryNameColumn;
    @FXML private TableColumn<Category, String> descriptionColumn;
    @FXML private TableColumn<Category, String> publicationsIdColumn;
    @FXML private TableColumn<Category, Void> actionsColumn;
    @FXML private Button buttonreturn;

    private final CategorieService categorieService = new CategorieService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadCategories();
    }

    private void setupTableColumns() {
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("idCategory"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("nomCategory"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        publicationsIdColumn.setCellValueFactory(cellData -> {
            Category category = cellData.getValue();
            String publicationIds = "No Publications";
            if (category.getPublications() != null && !category.getPublications().isEmpty()) {
                publicationIds = category.getPublications().stream()
                        .map(publication -> String.valueOf(publication.getId_publication()))
                        .collect(Collectors.joining(", "));
            }
            return new javafx.beans.property.SimpleStringProperty(publicationIds);
        });

        actionsColumn.setCellFactory(createActionsCellFactory());
    }

    private Callback<TableColumn<Category, Void>, TableCell<Category, Void>> createActionsCellFactory() {
        return param -> new TableCell<>() {
            private final Button updateButton = new Button();
            private final Button deleteButton = new Button();

            {
                ImageView updateIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/newedit.png")));
                updateIcon.setFitWidth(20);
                updateIcon.setFitHeight(20);

                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/newdel.png")));
                deleteIcon.setFitWidth(20);
                deleteIcon.setFitHeight(20);

                updateButton.setGraphic(updateIcon);
                deleteButton.setGraphic(deleteIcon);

                updateButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                updateButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    updateCategory(category);
                });

                deleteButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    deleteCategory(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttonContainer = new HBox(15, updateButton, deleteButton);
                    buttonContainer.setStyle("-fx-alignment: center;");
                    setGraphic(buttonContainer);
                }
            }
        };
    }

    private void updateCategory(Category category) {
        TextInputDialog dialog = new TextInputDialog(category.getNomCategory());
        dialog.setTitle("Update Category");
        dialog.setHeaderText("Modify the category name and description");
        dialog.setContentText("Enter new name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            TextInputDialog descriptionDialog = new TextInputDialog(category.getDescription());
            descriptionDialog.setTitle("Update Description");
            descriptionDialog.setHeaderText("Modify the description");
            descriptionDialog.setContentText("Enter description:");

            Optional<String> descriptionResult = descriptionDialog.showAndWait();
            descriptionResult.ifPresent(newDescription -> {
                if (controlSaisie(newName, newDescription, category)) {
                    category.setNomCategory(newName);
                    category.setDescription(newDescription);
                    try {
                        categorieService.update(category);
                        loadCategories();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully!");
                    } catch (SQLException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update category: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private boolean isCategoryNameTaken(String nomCategory, int excludeId) {
        try {
            return categorieService.exists(nomCategory, excludeId);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Database error occurred: " + e.getMessage());
            return false;
        }
    }

    private boolean controlSaisie(String nomCategory, String description, Category category) {
        if (!nomCategory.matches("[a-zA-Z\\s]+") || !description.matches("[a-zA-Z\\s]+")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Category name and description must contain only letters and spaces.");
            return false;
        }

        if (isCategoryNameTaken(nomCategory, category.getIdCategory())) {
            showAlert(Alert.AlertType.ERROR, "Error", "Category name already exists!");
            return false;
        }

        return true;
    }

    private void deleteCategory(Category category) {
        try {
            // Confirm deletion
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete category '" + category.getNomCategory() + "'? This will also delete all associated publications.",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText(null);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                categorieService.delete(category);
                loadCategories();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category and associated publications deleted successfully!");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        try {
            List<Category> categories = categorieService.showAll();
            ObservableList<Category> observableCategories = FXCollections.observableArrayList(categories);
            categoriesTableView.setItems(observableCategories);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void returnHomeCat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Detail.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) buttonreturn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Publications");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Detail.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void downloadCategoryPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                createCategoriesPdf(document, categoriesTableView.getItems());
                document.save(file);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Categories report saved successfully!");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void createCategoriesPdf(PDDocument document, ObservableList<Category> categories) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        int y = 750;
        boolean isFirstPage = true;

        for (int i = 0; i < categories.size(); i++) {
            if (y < 100) {
                contentStream.close();
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                y = 750;
                isFirstPage = false;
            }

            if ((i == 0) || (y == 750 && !isFirstPage)) {
                y = drawHeader(document, contentStream, y);
            }

            y = drawCategoryRow(contentStream, categories.get(i), y);
        }

        drawFooter(contentStream);
        contentStream.close();
    }

    private int drawHeader(PDDocument document, PDPageContentStream contentStream, int y) throws IOException {
        PDImageXObject logo = PDImageXObject.createFromFile("src/main/resources/icons/iconTripit.png", document);

        contentStream.drawImage(logo, 50, y - 50, 100, 50);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
        contentStream.newLineAtOffset(160, y - 20);
        contentStream.showText("Trip It - Categories Report");
        contentStream.endText();

        contentStream.setLineWidth(1);
        contentStream.moveTo(50, y - 60);
        contentStream.lineTo(550, y - 60);
        contentStream.stroke();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(50, y - 80);
        contentStream.showText("ID");
        contentStream.newLineAtOffset(50, 0);
        contentStream.showText("Category Name");
        contentStream.newLineAtOffset(150, 0);
        contentStream.showText("Description");
        contentStream.newLineAtOffset(150, 0);
        contentStream.showText("Publications");
        contentStream.endText();

        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(50, y - 85);
        contentStream.lineTo(550, y - 85);
        contentStream.stroke();

        return y - 100;
    }

    private int drawCategoryRow(PDPageContentStream contentStream, Category category, int y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(String.valueOf(category.getIdCategory()));
        contentStream.newLineAtOffset(50, 0);

        String categoryName = category.getNomCategory();
        if (categoryName.length() > 20) {
            categoryName = categoryName.substring(0, 17) + "...";
        }
        contentStream.showText(categoryName);

        contentStream.newLineAtOffset(150, 0);

        String description = category.getDescription();
        if (description.length() > 25) {
            description = description.substring(0, 22) + "...";
        }
        contentStream.showText(description);

        contentStream.newLineAtOffset(150, 0);

        String publicationIds = "None";
        if (category.getPublications() != null && !category.getPublications().isEmpty()) {
            publicationIds = category.getPublications().stream()
                    .map(publication -> String.valueOf(publication.getId_publication()))
                    .collect(Collectors.joining(", "));
            if (publicationIds.length() > 20) {
                publicationIds = publicationIds.substring(0, 17) + "...";
            }
        }
        contentStream.showText(publicationIds);
        contentStream.endText();

        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(50, y - 5);
        contentStream.lineTo(550, y - 5);
        contentStream.stroke();

        return y - 20;
    }

    private void drawFooter(PDPageContentStream contentStream) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
        contentStream.newLineAtOffset(50, 30);
        contentStream.showText("Thank you for using Trip It!");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(400, 30);
        contentStream.showText("Report generated: " + java.time.LocalDate.now().toString());
        contentStream.endText();
    }

    @FXML
    private void downloadCategoryExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Categories");

                Row headerRow = sheet.createRow(0);
                String[] columns = {"ID", "Category Name", "Description", "Publications"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                }

                int rowNum = 1;
                for (Category category : categoriesTableView.getItems()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(category.getIdCategory());
                    row.createCell(1).setCellValue(category.getNomCategory());
                    row.createCell(2).setCellValue(category.getDescription());

                    String publicationIds = "None";
                    if (category.getPublications() != null && !category.getPublications().isEmpty()) {
                        publicationIds = category.getPublications().stream()
                                .map(publication -> String.valueOf(publication.getId_publication()))
                                .collect(Collectors.joining(", "));
                    }
                    row.createCell(3).setCellValue(publicationIds);
                }

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Excel file saved successfully!");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save Excel file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}