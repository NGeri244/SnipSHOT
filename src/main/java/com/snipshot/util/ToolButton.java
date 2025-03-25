package com.snipshot.util;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ToolButton {
    private static final String ACTIVE_STYLE = "-fx-background-color: #2675BF;";
    private static final String NORMAL_STYLE = "-fx-background-color: #4c5052;";
    
    public static Button create(String text) {
        Button button = new Button(text);
        button.setStyle(NORMAL_STYLE);
        return button;
    }

    public static Button createWithImage(String imagePath, double size) {
        try {
            Image icon = new Image(ToolButton.class.getResourceAsStream(imagePath));
            ImageView imageView = new ImageView(icon);
            imageView.setFitHeight(size);
            imageView.setFitWidth(size);
            Button button = new Button("", imageView);
            button.setStyle(NORMAL_STYLE);
            return button;
        } catch (Exception e) {
            // Fallback to text button if image loading fails
            return new Button("Tool");
        }
    }
    
    public static void setActive(Button button, boolean active) {
        button.setStyle(active ? ACTIVE_STYLE : NORMAL_STYLE);
    }
} 