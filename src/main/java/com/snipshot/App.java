package com.snipshot;

import javafx.application.Application;
import javafx.stage.Stage;
import com.snipshot.controller.MainController;

public class App extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        MainController mainController = new MainController();
        mainController.showMainView();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 