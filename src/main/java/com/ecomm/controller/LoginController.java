package com.ecomm.controller;

import com.ecomm.model.User;
import com.ecomm.repository.UserRepository;
import com.ecomm.util.DatabaseUtil;
import com.ecomm.util.Navigator;
import com.ecomm.util.UiUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label messageLabel;

    private final UserRepository userRepository = new UserRepository();

    @FXML
    public void initialize() {
        messageLabel.setText("");
        warnIfDatabaseIsDown();
    }

    /** Checks the connection off the UI thread so a missing database is obvious before the user types anything. */
    private void warnIfDatabaseIsDown() {
        Thread checker = new Thread(() -> {
            boolean up = DatabaseUtil.testConnection();
            if (!up) {
                Platform.runLater(() -> UiUtil.showMessage(messageLabel, UiUtil.DB_DOWN_MESSAGE, false));
            }
        }, "db-check");
        checker.setDaemon(true);
        checker.start();
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            UiUtil.showMessage(messageLabel, "Please enter both email and password.", false);
            return;
        }

        Optional<User> user = userRepository.authenticate(email, password);
        if (user.isPresent()) {
            openDashboard(user.get());
        } else if (!DatabaseUtil.testConnection()) {
            UiUtil.showMessage(messageLabel, UiUtil.DB_DOWN_MESSAGE, false);
        } else {
            UiUtil.showMessage(messageLabel, "Invalid email or password.", false);
            passwordField.clear();
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            Navigator.show(stage(), Navigator.REGISTER, "Register - E-Commerce Auto", 520, 780);
        } catch (IOException e) {
            UiUtil.showMessage(messageLabel, "Could not open the registration screen: " + e.getMessage(), false);
        }
    }

    private void openDashboard(User user) {
        try {
            MainController main = Navigator.show(stage(), Navigator.MAIN, "E-Commerce Auto", 1280, 800);
            main.setCurrentUser(user);
            main.loadInitialData();
        } catch (IOException e) {
            UiUtil.showMessage(messageLabel, "Could not open the dashboard: " + e.getMessage(), false);
        }
    }

    private Stage stage() {
        return (Stage) loginButton.getScene().getWindow();
    }
}
