package com.ecomm.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Switches the window between screens (login, register, dashboard, add listing). */
public final class Navigator {

    public static final String LOGIN = "/fxml/login.fxml";
    public static final String REGISTER = "/fxml/register.fxml";
    public static final String MAIN = "/fxml/main.fxml";
    public static final String ADD_LISTING = "/fxml/add-listing.fxml";

    private Navigator() {
    }

    /**
     * Loads the FXML onto the given stage and returns its controller so the caller can hand it data.
     */
    public static <T> T show(Stage stage, String fxml, String title, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxml));
        Parent root = loader.load();

        boolean sizeChanged = stage.getScene() == null
                || stage.getScene().getWidth() != width
                || stage.getScene().getHeight() != height;

        stage.setScene(new Scene(root, width, height));
        stage.setTitle(title);
        if (sizeChanged) {
            stage.centerOnScreen();
        }
        return loader.getController();
    }
}
