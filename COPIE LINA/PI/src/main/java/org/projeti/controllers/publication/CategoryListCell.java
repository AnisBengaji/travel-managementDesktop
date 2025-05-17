package org.projeti.controllers.publication;

import javafx.scene.control.ListCell;
import org.projeti.entites.Category;

public class CategoryListCell extends ListCell<Category> {
    @Override
    protected void updateItem(Category category, boolean empty) {
        super.updateItem(category, empty);

        if (empty || category == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(category.getNomCategory() + " - " + category.getDescription());
        }
    }
}