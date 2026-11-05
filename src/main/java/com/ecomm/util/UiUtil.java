package com.ecomm.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public final class UiUtil {

    public static final String DB_DOWN_MESSAGE =
            "Cannot reach the database. Start it with 'docker compose up -d' (see README).";

    private UiUtil() {
    }

    /** Shows a message in a label, styled green for success and red for errors. */
    public static void showMessage(Label label, String text, boolean success) {
        if (label == null) return;
        label.setText(text);
        label.getStyleClass().removeAll("error-text", "success-text");
        label.getStyleClass().add(success ? "success-text" : "error-text");
    }

    public static void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().getStylesheets().add(UiUtil.class.getResource("/css/app.css").toExternalForm());
        alert.showAndWait();
    }
}
