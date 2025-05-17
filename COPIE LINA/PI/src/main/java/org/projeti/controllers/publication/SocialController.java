package org.projeti.controllers.publication;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.projeti.Service.CommentService;
import org.projeti.Service.UserSession;
import org.projeti.entites.Comment;
import org.projeti.entites.Publication;
import org.projeti.entites.Category;
import org.projeti.entites.User;
import org.projeti.Service.PublicationService;
import org.projeti.Service.CategorieService;

public class SocialController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> categoriesListView;
    @FXML
    private VBox publicationsContainer;
    @FXML
    private VBox publicationTemplate;
    @FXML
    private Button buttonnavigateadd;
    @FXML
    private Button buttonAddCat;
    String currentUser = "Guest";
    private PublicationService publicationService;
    private CategorieService categorieService;
    private List<Publication> currentPublications;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            publicationService = new PublicationService();
            categorieService = new CategorieService();
            currentPublications = new ArrayList<>();
            loadCategories();
            loadAllPublications();
        } catch (Exception e) {
            showAlert("Error initializing application: " + e.getMessage());
        }
    }

    private void loadCategories() {
        try {
            List<Category> categories = categorieService.showAll();
            categoriesListView.getItems().add("All Categories");
            for (Category category : categories) {
                categoriesListView.getItems().add(category.getNomCategory());
            }
            categoriesListView.getSelectionModel().select(0);
        } catch (Exception e) {
            showAlert("Error loading categories: " + e.getMessage());
        }
    }

    private void loadAllPublications() {
        try {
            currentPublications = publicationService.showAll();
            displayPublications(currentPublications);
        } catch (Exception e) {
            showAlert("Error loading publications: " + e.getMessage());
        }
    }

    private void displayPublications(List<Publication> publications) {
        publicationsContainer.getChildren().clear();
        if (publications.isEmpty()) {
            Label noPublicationsLabel = new Label("No publications found");
            noPublicationsLabel.getStyleClass().add("no-publications-label");
            publicationsContainer.getChildren().add(noPublicationsLabel);
            return;
        }
        for (Publication publication : publications) {
            VBox publicationCard = createPublicationCard(publication);
            publicationsContainer.getChildren().add(publicationCard);
        }
    }

    private VBox createPublicationCard(Publication publication) {
        VBox card = new VBox();
        card.getStyleClass().add("publication-card");
        card.setSpacing(10);
        card.setUserData(publication.getId_publication());

        ImageView authorAvatar = new ImageView();
        try {
            String avatarPath = "/images/default-avatar.png";
            File avatarFile = new File(avatarPath);
            if (avatarFile.exists()) {
                authorAvatar.setImage(new Image(avatarFile.toURI().toString()));
            } else {
                authorAvatar.setImage(new Image(getClass().getResourceAsStream(avatarPath)));
            }
        } catch (Exception e) {
            authorAvatar.setImage(null);
        }
        authorAvatar.setFitHeight(40);
        authorAvatar.setFitWidth(40);
        authorAvatar.getStyleClass().add("author-avatar");

        String authorName = publication.getAuthor() != null ? publication.getAuthor().getEmail() : "Unknown";
        Label authorLabel = new Label(authorName);
        authorLabel.getStyleClass().add("author-name");

        String formattedDate = publication.getDate_publication() != null ?
                publication.getDate_publication().toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) :
                "Unknown Date";
        Label dateLabel = new Label(formattedDate);
        dateLabel.getStyleClass().add("publication-date");

        Label visibilityBadge = new Label(publication.getVisibility());
        visibilityBadge.getStyleClass().add("visibility-badge");

        Label titleLabel = new Label(publication.getTitle());
        titleLabel.getStyleClass().add("publication-title");
        titleLabel.setWrapText(true);

        TextFlow contentFlow = new TextFlow();
        contentFlow.getStyleClass().add("publication-content");
        Text contentText = new Text(publication.getContenu());
        contentFlow.getChildren().add(contentText);

        ImageView publicationImageView = null;
        if (publication.getImage() != null && !publication.getImage().isEmpty()) {
            try {
                File imageFile = new File(publication.getImage());
                if (imageFile.exists()) {
                    publicationImageView = new ImageView(new Image(imageFile.toURI().toString()));
                } else {
                    publicationImageView = new ImageView(new Image(publication.getImage()));
                }
                publicationImageView.setFitWidth(400);
                publicationImageView.setPreserveRatio(true);
                publicationImageView.getStyleClass().add("publication-image");
            } catch (Exception e) {
                publicationImageView = null;
            }
        }

        String categoryName = publication.getCategory() != null ?
                publication.getCategory().getNomCategory() : "Uncategorized";
        Label categoryLabel = new Label(categoryName);
        categoryLabel.getStyleClass().add("category-tag");

        Button likeButton = new Button("Like");
        likeButton.getStyleClass().add("action-button");
        int pubId = publication.getId_publication();
        likeButton.setOnAction(e -> handleLike(pubId));

        Button commentButton = new Button("Comment");
        commentButton.getStyleClass().add("action-button");
        commentButton.setOnAction(e -> handleComment(pubId));

        Button shareButton = new Button("Share");
        shareButton.getStyleClass().add("action-button");
        shareButton.setOnAction(e -> handleShare(pubId));

        VBox commentsSection = new VBox();
        commentsSection.getStyleClass().add("comments-section");
        commentsSection.setManaged(false);
        commentsSection.setVisible(false);

        HBox commentInputBox = new HBox();
        commentInputBox.setSpacing(10);
        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");
        commentField.getStyleClass().add("comment-field");
        HBox.setHgrow(commentField, Priority.ALWAYS);

        Button postButton = new Button("Post");
        postButton.getStyleClass().add("post-comment-button");
        postButton.setOnAction(e -> {
            try {
                postComment(pubId, commentField.getText());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        commentInputBox.getChildren().addAll(commentField, postButton);

        VBox commentsContainer = new VBox();
        commentsContainer.getStyleClass().add("comments-container");

        commentsSection.getChildren().addAll(commentInputBox, commentsContainer);

        HBox header = createPublicationHeader(authorAvatar, authorLabel, dateLabel, visibilityBadge, pubId);
        HBox actionButtons = createActionButtons(likeButton, commentButton, shareButton);

        card.getChildren().addAll(header, titleLabel, contentFlow, categoryLabel, actionButtons, commentsSection);
        if (publicationImageView != null) {
            card.getChildren().add(2, publicationImageView);
        }

        return card;
    }

    private HBox createPublicationHeader(ImageView avatar, Label author, Label date, Label visibility, int publicationId) {
        HBox header = new HBox();
        header.getStyleClass().add("publication-header");
        header.setSpacing(10);

        VBox authorInfo = new VBox();
        authorInfo.getChildren().addAll(author, date);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MenuButton menuButton = new MenuButton();
        menuButton.getStyleClass().add("publication-menu");

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> editPublication(publicationId));

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deletePublication(publicationId));

        MenuItem shareItem = new MenuItem("Share");
        shareItem.setOnAction(e -> sharePublication(publicationId));

        menuButton.getItems().addAll(editItem, deleteItem, shareItem);

        header.getChildren().addAll(avatar, authorInfo, spacer, visibility, menuButton);

        return header;
    }

    private HBox createActionButtons(Button like, Button comment, Button share) {
        HBox actions = new HBox();
        actions.getStyleClass().add("publication-actions");
        actions.setSpacing(10);
        actions.getChildren().addAll(like, comment, share);
        return actions;
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadAllPublications();
        } else {
            try {
                List<Publication> allPublications = publicationService.showAll();
                List<Publication> searchResults = new ArrayList<>();
                for (Publication pub : allPublications) {
                    String authorName = pub.getAuthor() != null ? pub.getAuthor().getEmail() : "";
                    if (pub.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            pub.getContenu().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            authorName.toLowerCase().contains(searchTerm.toLowerCase())) {
                        searchResults.add(pub);
                    }
                }
                currentPublications = searchResults;
                displayPublications(searchResults);
            } catch (Exception e) {
                showAlert("Error searching publications: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCategorySelect() {
        String selected = categoriesListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("All Categories")) {
            loadAllPublications();
        } else {
            try {
                List<Publication> filteredPublications = publicationService.getPublicationsByCategory(selected);
                currentPublications = filteredPublications;
                displayPublications(filteredPublications);
            } catch (Exception e) {
                showAlert("Error filtering publications: " + e.getMessage());
            }
        }
    }

    @FXML
    private void filterAll() {
        loadAllPublications();
    }

    @FXML
    private void filterMine() {
        String currentUserEmail = UserSession.getEmail();
        if (currentUserEmail == null || currentUserEmail.trim().isEmpty()) {
            showAlert("User session not found. Please log in.");
            return;
        }
        try {
            List<Publication> userPublications = publicationService.getPublicationsByAuthor(currentUserEmail);
            currentPublications = userPublications;
            displayPublications(userPublications);
        } catch (Exception e) {
            showAlert("Error filtering your publications: " + e.getMessage());
        }
    }

    @FXML
    private void filterPublic() {
        try {
            List<Publication> publicPublications = publicationService.getPublicationsByVisibility("Public");
            currentPublications = publicPublications;
            displayPublications(publicPublications);
        } catch (Exception e) {
            showAlert("Error filtering public publications: " + e.getMessage());
        }
    }

    @FXML
    private void showCreatePublicationDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreatePublication.fxml"));
            Parent root = loader.load();
            CreatePublicationController controller = loader.getController();
            if (publicationService == null || categorieService == null) {
                showAlert("Error: Publication or category service is not initialized!");
                return;
            }
            controller.setPublicationService(publicationService);
            controller.setCategorieService(categorieService);
            Stage stage = new Stage();
            stage.setTitle("Create New Publication");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> loadAllPublications());
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening publication form: " + e.getMessage());
        }
    }

    @FXML
    private void handleLike(int publicationId) {
        System.out.println("Like publication: " + publicationId);
    }

    @FXML
    private void handleComment(int publicationId) {
        for (javafx.scene.Node node : publicationsContainer.getChildren()) {
            if (node instanceof VBox && node.getUserData() != null &&
                    node.getUserData().equals(publicationId)) {
                VBox card = (VBox) node;
                VBox commentsSection = (VBox) card.getChildren().get(card.getChildren().size() - 1);
                boolean isVisible = commentsSection.isVisible();
                commentsSection.setManaged(!isVisible);
                commentsSection.setVisible(!isVisible);
                if (!isVisible) {
                    loadComments(publicationId, commentsSection);
                }
                break;
            }
        }
    }

    private void loadComments(int publicationId, VBox commentsSection) {
        VBox commentsContainer = (VBox) commentsSection.getChildren().get(1);
        commentsContainer.getChildren().clear();
        try {
            CommentService commentService = new CommentService();
            List<Comment> comments = commentService.getCommentsByPublication(publicationId);
            for (Comment comment : comments) {
                VBox commentBubble = createCommentBubble(comment.getAuthor(), comment.getContent());
                commentsContainer.getChildren().add(commentBubble);
            }
        } catch (SQLException e) {
            showAlert("Error loading comments: " + e.getMessage());
        }
    }

    private VBox createCommentBubble(String author, String text) {
        VBox commentBubble = new VBox();
        commentBubble.getStyleClass().add("comment-bubble");
        Label authorLabel = new Label(author);
        authorLabel.getStyleClass().add("comment-author");
        Label commentText = new Label(text);
        commentText.getStyleClass().add("comment-text");
        commentText.setWrapText(true);
        commentBubble.getChildren().addAll(authorLabel, commentText);
        return commentBubble;
    }

    @FXML
    private void handleShare(int publicationId) {
        System.out.println("Share publication: " + publicationId);
    }

    @FXML
    private void editPublication(int publicationId) {
        try {
            Publication publicationToEdit = null;
            for (Publication pub : currentPublications) {
                if (pub.getId_publication() == publicationId) {
                    publicationToEdit = pub;
                    break;
                }
            }
            if (publicationToEdit == null) {
                showAlert("Publication not found");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditPublication.fxml"));
            Parent root = loader.load();
            EditPublicationController controller = loader.getController();
            controller.setPublication(publicationToEdit);
            controller.setPublicationService(publicationService);
            controller.setCategorieService(categorieService);
            Stage stage = new Stage();
            stage.setTitle("Edit Publication");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> loadAllPublications());
            stage.showAndWait();
        } catch (Exception e) {
            showAlert("Error opening edit form: " + e.getMessage());
        }
    }

    @FXML
    private void deletePublication(int publicationId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Publication");
        confirmAlert.setHeaderText("Are you sure?");
        confirmAlert.setContentText("This action cannot be undone.");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    publicationService.deletePublication(publicationId);
                    loadAllPublications();
                } catch (Exception e) {
                    showAlert("Error deleting publication: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void sharePublication(int publicationId) {
        handleShare(publicationId);
    }

    private void postComment(int publicationId, String commentText) throws SQLException {
        if (commentText == null || commentText.trim().isEmpty()) {
            return;
        }
        String currentUserEmail = UserSession.getEmail();
        if (currentUserEmail == null || currentUserEmail.trim().isEmpty()) {
            showError("User session not found. Please log in again.");
            return;
        }
        java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.now());
        CommentService commentService = new CommentService();
        Comment newComment = new Comment(currentUserEmail, commentText, sqlDate, publicationId);
        commentService.insert(newComment);
        Platform.runLater(() -> {
            for (javafx.scene.Node node : publicationsContainer.getChildren()) {
                if (node instanceof VBox && publicationId == (int) node.getUserData()) {
                    VBox card = (VBox) node;
                    VBox commentsSection = (VBox) card.getChildren().get(card.getChildren().size() - 1);
                    VBox commentsContainer = (VBox) commentsSection.getChildren().get(1);
                    VBox newCommentBubble = createCommentBubble(currentUserEmail, commentText);
                    commentsContainer.getChildren().add(0, newCommentBubble);
                    TextField commentField = (TextField) ((HBox) commentsSection.getChildren().get(0)).getChildren().get(0);
                    commentField.clear();
                    break;
                }
            }
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goToAddPublication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPub.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) buttonnavigateadd.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Publication");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Failed to load AjouterPub.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void goToAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCatFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) buttonAddCat.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Category");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Failed to load AjouterCatFront.fxml: " + e.getMessage());
        }
    }
}