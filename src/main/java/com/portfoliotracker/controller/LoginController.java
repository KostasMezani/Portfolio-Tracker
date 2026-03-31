package com.portfoliotracker.controller;

import com.portfoliotracker.model.User;
import com.portfoliotracker.service.AuthService;
import com.portfoliotracker.exception.AuthenticationException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginController {

    private final AuthService authService;
    private final Stage stage;

    // UI Elements
    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;

    public LoginController(Stage stage, AuthService authService) {
        this.stage = stage;
        this.authService = authService;
    }

    /**
     * Creates and returns the Login Scene
     * @return the login scene
     */
    public Scene createScene() {
        // Title
        Label titleLabel = new Label("Portfolio Tracker");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        // Username field
        Label usernameLabel = new Label("Username:");
        usernameLabel.setTextFill(Color.WHITE);
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(300);

        // Password field
        Label passwordLabel = new Label("Password:");
        passwordLabel.setTextFill(Color.WHITE);
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(300);

        // Error label
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Login button
        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(300);
        loginButton.setOnAction(e -> handleLogin());

        // Register link
        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here");
        registerLink.setOnAction(e -> navigateToRegister());

        // Layout
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(
                titleLabel,
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                errorLabel,
                loginButton,
                registerLink
        );
        layout.setStyle("-fx-background-color: #1a2942;");

        return new Scene(layout, 800, 600);
    }

    /**
     * Handles login button click
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate fields
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        try {
            User user = authService.login(username, password);
            navigateToDashboard(user);
        } catch (AuthenticationException e) {
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
     * Navigates to Register screen
     */
    private void navigateToRegister() {
        RegisterController registerController = new RegisterController(stage, authService);
        stage.setScene(registerController.createScene());
    }

    /**
     * Navigates to Dashboard
     */
    private void navigateToDashboard(User user) {
        // TODO: θα υλοποιηθεί όταν φτιάξουμε το DashboardController
    }
}