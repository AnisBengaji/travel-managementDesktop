package org.projeti.controllers.publication;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.projeti.entites.Publication;
import org.projeti.entites.Comment;

import java.util.List;
import java.util.function.Consumer;

public class PublicationCardController {

    @FXML private VBox publicationCard;
    @FXML private ImageView publicationImage;
    @FXML private Label publicationTitle;
    @FXML private Label publicationMetadata;
    @FXML private Label publicationContent;
    @FXML private Button toggleCommentsButton;
    @FXML private TitledPane commentsPane;
    @FXML private ListView<Comment> commentsListView;

    private Publication publication;
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private Consumer<Boolean> onSelectionChanged;

    @FXML
    public void initialize() {
        publicationCard.setOnMouseClicked(this::handleCardClick);
        toggleCommentsButton.setOnAction(event -> toggleComments());
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
        publicationTitle.setText(publication.getTitle());
        publicationMetadata.setText(String.format("By: %s, %s | Category: %s | Visibility: %s",
                publication.getAuthor() != null ? publication.getAuthor().getPrenom() : "Unknown",
                publication.getDate_publication(),
                publication.getCategory() != null ? publication.getCategory().getNomCategory() : "Uncategorized",
                publication.getVisibility()));
        publicationContent.setText(publication.getContenu());
        if (publication.getImage() != null) {
            publicationImage.setImage(new Image("file:" + publication.getImage()));
        }
        publicationCard.getProperties().put("controller", this);
    }

    public void setComments(List<Comment> comments) {
        this.commentsListView.getItems().setAll(comments);
        commentsPane.setVisible(!comments.isEmpty());
        toggleCommentsButton.setVisible(!comments.isEmpty());
    }

    public Publication getPublication() {
        return publication;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setOnSelectionChanged(Consumer<Boolean> callback) {
        this.onSelectionChanged = callback;
        selected.addListener((obs, oldVal, newVal) -> callback.accept(newVal));
    }

    private void handleCardClick(MouseEvent event) {
        selected.set(!selected.get());
        publicationCard.setStyle(selected.get() ? "-fx-border-color: #0288D1; -fx-border-width: 2;" : "-fx-border-color: #B0BEC5; -fx-border-width: 1;");
    }

    @FXML
    void toggleComments() {
        commentsPane.setExpanded(!commentsPane.isExpanded());
        toggleCommentsButton.setText(commentsPane.isExpanded() ? "Hide Comments" : "Show Comments");
    }
}