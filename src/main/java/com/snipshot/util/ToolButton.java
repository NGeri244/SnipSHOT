package com.snipshot.util;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ToolButton {
    public static Button create(String text) {
        return new Button(text);
    }

    public static Button createWithImage(String imagePath, double size) {
        try {
            Image icon = new Image(ToolButton.class.getResourceAsStream(imagePath));
            ImageView imageView = new ImageView(icon);
            imageView.setFitHeight(size);
            imageView.setFitWidth(size);
            return new Button("", imageView);
        } catch (Exception e) {
            // Fallback to text button if image loading fails
            return new Button("Tool");
        }
    }
} 