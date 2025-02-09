module com.snipshot {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;

    exports com.snipshot;
    exports com.snipshot.controller;
    exports com.snipshot.model;
    exports com.snipshot.view;
    
    opens com.snipshot to javafx.fxml;
    opens com.snipshot.view to javafx.fxml;
} 