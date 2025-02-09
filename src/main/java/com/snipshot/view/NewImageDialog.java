package com.snipshot.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class NewImageDialog extends Dialog<NewImageDialog.ImageProperties> {
    public static class ImageProperties {
        public final int width;
        public final int height;

        public ImageProperties(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public NewImageDialog(Stage owner) {
        setTitle("New Image");
        setHeaderText("Enter image dimensions");
        initOwner(owner);

        // Center dialog on owner window
        Window window = getDialogPane().getScene().getWindow();
        window.setOnShowing(e -> {
            window.centerOnScreen();
        });

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the width and height labels and fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField widthField = new TextField("800");
        TextField heightField = new TextField("600");
        
        grid.add(new Label("Width (px):"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("Height (px):"), 0, 1);
        grid.add(heightField, 1, 1);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    int width = Integer.parseInt(widthField.getText());
                    int height = Integer.parseInt(heightField.getText());
                    return new ImageProperties(width, height);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
    }
} 