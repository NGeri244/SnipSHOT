package com.snipshot.model;

import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

public class TextBox extends TextArea {
    public TextBox(double x, double y, double width, double height) {
        super();
        setLayoutX(x);
        setLayoutY(y);
        setPrefSize(width, height);
        setWrapText(true);
        
        // Default styling
        setFont(Font.font("Arial", 14));
        setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
    }

    public void setTextFont(String family, double size) {
        setFont(Font.font(family, size));
    }

    public void setTextColor(Color color) {
        setStyle("-fx-background-color: transparent; -fx-text-fill: " + toRGBCode(color) + ";");
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
} 