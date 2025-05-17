package org.projeti.controllers.publication;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.projeti.entites.Publication;
import java.text.SimpleDateFormat;

public class PublicationListCell extends ListCell<Publication> {

    @Override
    protected void updateItem(Publication publication, boolean empty) {
        super.updateItem(publication, empty);

        if (empty || publication == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Create a VBox to hold the post content
            VBox postBox = new VBox();
            postBox.setSpacing(10);
            postBox.getStyleClass().add("post-box");

            // Add hover effect
            postBox.setOnMouseEntered(e -> postBox.setStyle("-fx-background-color: #F5F5F5;"));
            postBox.setOnMouseExited(e -> postBox.setStyle("-fx-background-color: #FFFFFF;"));

            // Title
            Text titleText = new Text(publication.getTitle() != null ? publication.getTitle() : "No Title");
            titleText.setFont(Font.font("System", FontWeight.BOLD, 16));
            titleText.getStyleClass().add("title-text");

            // Author (Using Prenom and Nom)
            String authorName = "Unknown Author";
            if (publication.getAuthor() != null) {
                String prenom = publication.getAuthor().getPrenom();
                String nom = publication.getAuthor().getNom();
                if (prenom != null && nom != null) {
                    authorName = prenom + " " + nom;
                } else if (prenom != null) {
                    authorName = prenom;
                } else if (nom != null) {
                    authorName = nom;
                }
            }
            Text authorText = new Text("By: " + authorName);
            authorText.setFont(Font.font("System", FontWeight.NORMAL, 12));
            authorText.getStyleClass().add("author-text");

            // Date (Formatted)
            String dateString = "Date not available";
            if (publication.getDate_publication() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
                dateString = "Published on: " + sdf.format(publication.getDate_publication());
            }
            Text dateText = new Text(dateString);
            dateText.setFont(Font.font("System", FontWeight.NORMAL, 12));
            dateText.getStyleClass().add("date-text");

            // Category
            String categoryName = publication.getCategory() != null && publication.getCategory().getNomCategory() != null
                    ? publication.getCategory().getNomCategory()
                    : "Uncategorized";
            Text categoryText = new Text("Category: " + categoryName);
            categoryText.setFont(Font.font("System", FontWeight.NORMAL, 12));
            categoryText.getStyleClass().add("category-text");

            // Visibility
            Text visibilityText = new Text("Visibility: " + (publication.getVisibility() != null ? publication.getVisibility() : "Unknown"));
            visibilityText.setFont(Font.font("System", FontWeight.NORMAL, 12));
            visibilityText.getStyleClass().add("visibility-text");

            // Content preview (limit to a reasonable length)
            String contentPreview = publication.getContenu() != null ? publication.getContenu() : "";
            if (contentPreview.length() > 200) {
                contentPreview = contentPreview.substring(0, 197) + "...";
            }
            Text contentText = new Text(contentPreview);
            contentText.setFont(Font.font("System", FontWeight.NORMAL, 14));
            contentText.getStyleClass().add("content-text");
            contentText.setWrappingWidth(700);

            // Comments
            int commentCount = publication.getComments() != null ? publication.getComments().size() : 0;
            Text commentsText = new Text("Comments: " + commentCount);
            commentsText.setFont(Font.font("System", FontWeight.NORMAL, 12));
            commentsText.getStyleClass().add("comments-text");

            // Image (if available)
            ImageView imageView = null;
            if (publication.getImage() != null && !publication.getImage().isEmpty()) {
                try {
                    Image image = new Image(publication.getImage(), 200, 150, true, true);
                    imageView = new ImageView(image);
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(150);
                    imageView.setPreserveRatio(true);
                } catch (Exception e) {
                    System.err.println("Failed to load image: " + e.getMessage());
                    // No need to set imageView - it will remain null
                }
            }

            // Add elements to the VBox
            postBox.getChildren().addAll(titleText, authorText, dateText, categoryText, visibilityText);

            // Add image next to the content if available
            if (imageView != null) {
                javafx.scene.layout.HBox contentBox = new javafx.scene.layout.HBox(20);
                contentBox.getChildren().addAll(contentText, imageView);
                postBox.getChildren().add(contentBox);
            } else {
                postBox.getChildren().add(contentText);
            }

            postBox.getChildren().add(commentsText);

            // Set the VBox as the graphic for the cell
            setGraphic(postBox);
        }
    }
}