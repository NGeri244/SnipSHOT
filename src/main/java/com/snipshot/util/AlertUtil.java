package com.snipshot.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AlertUtil {
    
    public static void showSaveCheckAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Check");
        alert.setHeaderText("Changes Made");
        alert.setContentText("Your changes have been saved to the current session.");
        alert.showAndWait();
    }
    
    public static Optional<ButtonType> showSaveConfirmationAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save Changes");
        alert.setHeaderText("You have unsaved changes");
        alert.setContentText("Do you want to save before quitting?");
        return alert.showAndWait();
    }
    
    public static void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public static void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 