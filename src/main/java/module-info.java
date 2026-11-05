module com.ecomm {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Java SQL for database
    requires java.sql;

    // Open packages to JavaFX (pentru reflection și FXML loading)
    opens com.ecomm to javafx.fxml;
    opens com.ecomm.controller to javafx.fxml;
    opens com.ecomm.model to javafx.base, javafx.fxml;

    // Export packages (pentru a fi accesibile din exterior)
    exports com.ecomm;
    exports com.ecomm.controller;
    exports com.ecomm.model;
    exports com.ecomm.repository;
    exports com.ecomm.util;
}