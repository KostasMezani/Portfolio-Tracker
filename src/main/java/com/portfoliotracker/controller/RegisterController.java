package com.portfoliotracker.controller;

import com.portfoliotracker.exception.ValidationException;
import com.portfoliotracker.service.AuthService;
import com.portfoliotracker.util.ValidationUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class RegisterController {

    private final AuthService authService;
    private final Stage stage;

    // UI Elements
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField emailField;
    private Label errorLabel;

    public RegisterController(Stage stage, AuthService authService) {
        this.stage = stage;
        this.authService = authService;
    }

    /**
     * Creates and returns the Register Scene
     * @return the register scene
     */
    public Scene createScene() {
        // Title
        Label titleLabel = new Label("Create an Account");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Fields
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(300);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(300);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setMaxWidth(300);

        emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setMaxWidth(300);

        // Error label
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Register button
        Button registerButton = new Button("Register");
        registerButton.setMaxWidth(300);
        registerButton.setOnAction(e -> handleRegister());

        // Back to login link
        Hyperlink loginLink = new Hyperlink("Back to Login");
        loginLink.setOnAction(e -> navigateToLogin());

        // Layout
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(
                titleLabel,
                usernameField,
                passwordField,
                confirmPasswordField,
                emailField,
                errorLabel,
                registerButton,
                loginLink
        );

        return new Scene(layout, 800, 600);
    }

    /**
     * Handles register button click
     */
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String email = emailField.getText().trim();

        // Validate fields
        if (!ValidationUtils.isNotEmpty(username) ||
                !ValidationUtils.isNotEmpty(password) ||
                !ValidationUtils.isNotEmpty(email)) {
            showError("Please fill in all fields");
            return;
        }

        // Check passwords match
        if (!ValidationUtils.passwordsMatch(password, confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Check email format
        if (!ValidationUtils.isValidEmail(email)) {
            showError("Invalid email format");
            return;
        }

        try {
            authService.register(username, password, email);
            navigateToLogin();
        } catch (ValidationException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Shows error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Navigates back to Login screen
     */
    private void navigateToLogin() {
        LoginController loginController = new LoginController(stage, authService);
        stage.setScene(loginController.createScene());
    }
}