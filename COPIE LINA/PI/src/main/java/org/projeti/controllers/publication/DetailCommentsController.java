package org.projeti.controllers.publication;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.projeti.Service.CommentService;
import org.projeti.Service.PublicationService;
import org.projeti.entites.Comment;
import org.projeti.entites.Publication;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class DetailCommentsController {

    @FXML private TableView<Comment> commentsTableView;
    @FXML private TableColumn<Comment, Integer> idColumn;
    @FXML private TableColumn<Comment, String> contentColumn;
    @FXML private TableColumn<Comment, String> authorColumn;
    @FXML private TableColumn<Comment, Date> dateColumn;
    @FXML private TableColumn<Comment, String> publicationIdColumn;
    @FXML private TableColumn<Comment, String> publicationTitleColumn;
    @FXML private TableColumn<Comment, Void> editColumn;
    @FXML private TableColumn<Comment, Void> deleteColumn;
    @FXML private Button buttonreturn;
    @FXML private Button publicationsButton;
    @FXML private Button categoryButton;

    private CommentService commentService = new CommentService();
    private PublicationService publicationService = new PublicationService();
    private ObservableList<Comment> comments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        try {
            loadComments();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load comments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_comment"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_comment"));
        publicationIdColumn.setCellValueFactory(cellData -> {
            int publicationId = cellData.getValue().getPublication_id();
            return new SimpleStringProperty(String.valueOf(publicationId));
        });
        publicationTitleColumn.setCellValueFactory(cellData -> {
            Comment comment = cellData.getValue();
            int pubId = comment.getPublication_id();
            System.out.println("Fetching title for publication_id: " + pubId); // Debugging
            try {
                if (pubId <= 0) {
                    return new SimpleStringProperty("Invalid ID");
                }
                Publication publication = publicationService.findById(pubId);
                return new SimpleStringProperty(
                        publication != null && publication.getTitle() != null ? publication.getTitle() : "Unknown"
                );
            } catch (SQLException e) {
                System.err.println("Error fetching publication " + pubId + ": " + e.getMessage());
                e.printStackTrace();
                return new SimpleStringProperty("Error");
            }
        });

        // Set up Edit column
        editColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setOnAction(event -> {
                    Comment comment = getTableView().getItems().get(getIndex());
                    editComment(comment);
                });
                editButton.getStyleClass().add("button-main");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

        // Set up Delete column
        deleteColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    Comment comment = getTableView().getItems().get(getIndex());
                    deleteComment(comment);
                });
                deleteButton.getStyleClass().add("button-main");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        commentsTableView.setItems(comments);
    }

    private void loadComments() throws SQLException {
        comments.setAll(commentService.showAll());
    }

    private void editComment(Comment comment) {
        TextInputDialog dialog = new TextInputDialog(comment.getContent());
        dialog.setTitle("Edit Comment");
        dialog.setHeaderText("Modify the comment content:");
        dialog.setContentText("Content:");

        dialog.showAndWait().ifPresent(newContent -> {
            if (!newContent.trim().isEmpty()) {
                try {
                    comment.setContent(newContent);
                    comment.setDate_comment(Date.valueOf(LocalDate.now()));
                    commentService.update(comment);
                    loadComments();
                    showAlert("Success", "Comment updated successfully!");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to update comment: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                showAlert("Error", "Comment content cannot be empty.");
            }
        });
    }

    private void deleteComment(Comment comment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete comment: " + comment.getContent() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    commentService.delete(comment);
                    loadComments();
                    showAlert("Success", "Comment deleted successfully!");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete comment: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void goBackHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Detail.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) buttonreturn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load Detail.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToPublications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Detail.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) publicationsButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Publications");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load Detail.fxml: " + e.getMessage());
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}