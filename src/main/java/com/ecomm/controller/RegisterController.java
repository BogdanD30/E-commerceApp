package com.ecomm.controller;

import com.ecomm.model.User;
import com.ecomm.repository.UserRepository;
import com.ecomm.util.DatabaseUtil;
import com.ecomm.util.Navigator;
import com.ecomm.util.PasswordUtil;
import com.ecomm.util.UiUtil;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private Label messageLabel;

    @FXML
    private ProgressIndicator passwordStrengthIndicator;

    private UserRepository userRepository;
    public RegisterController() {
        this.userRepository = new UserRepository();
    }

    @FXML
    public void initialize() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }

        if (passwordStrengthIndicator != null) {
            passwordStrengthIndicator.setVisible(false);
        }

        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                updatePasswordStrength(newVal);
            });
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!validateInput(email, password, confirmPassword)) {
            return;
        }

        try {
            if (userRepository.existsByEmail(email)) {
                showMessage("An account with this email already exists.", MessageType.ERROR);
                emailField.requestFocus();
                return;
            }

            User newUser = new User();
            newUser.setEmail(email.toLowerCase());
            newUser.setPasswordHash(PasswordUtil.createPasswordHash(password));
            newUser.setFirstName(firstName.isEmpty() ? null : firstName);
            newUser.setLastName(lastName.isEmpty() ? null : lastName);
            newUser.setPhone(phone.isEmpty() ? null : phone);

            if (userRepository.create(newUser)) {
                showMessage("Account created! Taking you to the login screen...", MessageType.SUCCESS);

                passwordField.clear();
                confirmPasswordField.clear();
                registerButton.setDisable(true);

                PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
                pause.setOnFinished(e -> handleBackToLogin(event));
                pause.play();

            } else if (!DatabaseUtil.testConnection()) {
                showMessage(UiUtil.DB_DOWN_MESSAGE, MessageType.ERROR);
            } else {
                showMessage("Registration failed. Please try again.", MessageType.ERROR);
            }

        } catch (Exception e) {
            showMessage("Error during registration: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validateInput(String email, String password, String confirmPassword) {
        if (email.isEmpty()) {
            showMessage("Email is required.", MessageType.ERROR);
            emailField.requestFocus();
            return false;
        }

        if (!isValidEmail(email)) {
            showMessage("Please enter a valid email address.", MessageType.ERROR);
            emailField.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showMessage("Password is required.", MessageType.ERROR);
            passwordField.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters long.", MessageType.ERROR);
            passwordField.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.", MessageType.ERROR);
            confirmPasswordField.requestFocus();
            confirmPasswordField.clear();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private void updatePasswordStrength(String password) {
        if (passwordStrengthIndicator == null) return;

        if (password.isEmpty()) {
            passwordStrengthIndicator.setVisible(false);
            return;
        }

        passwordStrengthIndicator.setVisible(true);

        double strength = calculatePasswordStrength(password);
        passwordStrengthIndicator.setProgress(strength);

        if (strength < 0.3) {
            passwordStrengthIndicator.setStyle("-fx-accent: red;");
        } else if (strength < 0.7) {
            passwordStrengthIndicator.setStyle("-fx-accent: orange;");
        } else {
            passwordStrengthIndicator.setStyle("-fx-accent: green;");
        }
    }

    private double calculatePasswordStrength(String password) {
        double strength = 0.0;

        if (password.length() >= 6) strength += 0.2;
        if (password.length() >= 8) strength += 0.2;
        if (password.length() >= 12) strength += 0.1;
        if (password.matches(".*[a-z].*")) strength += 0.15;
        if (password.matches(".*[A-Z].*")) strength += 0.15;
        if (password.matches(".*\\d.*")) strength += 0.1;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength += 0.1;

        return Math.min(strength, 1.0);
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        try {
            Navigator.show((Stage) backToLoginButton.getScene().getWindow(),
                    Navigator.LOGIN, "Login - E-Commerce Auto", 460, 560);
        } catch (IOException e) {
            showMessage("Error loading login screen: " + e.getMessage(), MessageType.ERROR);
            e.printStackTrace();
        }
    }

    private void showMessage(String message, MessageType type) {
        UiUtil.showMessage(messageLabel, message, type == MessageType.SUCCESS);
    }

    private enum MessageType {
        ERROR, SUCCESS
    }
}