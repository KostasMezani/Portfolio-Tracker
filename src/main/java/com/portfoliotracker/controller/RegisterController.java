package com.portfoliotracker.controller;

import com.portfoliotracker.exception.ValidationException;
import com.portfoliotracker.service.*;
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

    private final Stage stage;
    private final AuthService authService;
    private final PortfolioService portfolioService;
    private final TransactionService transactionService;
    private final MarketDataService marketDataService;
    private final WatchlistService watchlistService;
    private final AlertService alertService;

    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField emailField;
    private Label errorLabel;

    /**
     * Constructs a RegisterController with all required services and the primary stage.
     *
     * @param stage              the primary JavaFX stage used to switch scenes
     * @param authService        service responsible for authentication and registration logic
     * @param portfolioService   service for portfolio-related operations
     * @param transactionService service for transaction-related operations
     * @param marketDataService  service for fetching live market data
     * @param watchlistService   service for watchlist management
     * @param alertService       service for alert management
     */
    public RegisterController(Stage stage, AuthService authService,
                              PortfolioService portfolioService,
                              TransactionService transactionService,
                              MarketDataService marketDataService,
                              WatchlistService watchlistService,
                              AlertService alertService) {
        this.stage = stage;
        this.authService = authService;
        this.portfolioService = portfolioService;
        this.transactionService = transactionService;
        this.marketDataService = marketDataService;
        this.watchlistService = watchlistService;
        this.alertService = alertService;
    }

    /**
     * Builds and returns the registration {@link Scene} containing the sign-up form.
     *
     * @return the JavaFX Scene for the registration screen
     */
    public Scene createScene() {
        Label titleLabel = new Label("Create an Account");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

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

        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        Button registerButton = new Button("Register");
        registerButton.setMaxWidth(300);
        registerButton.setOnAction(e -> handleRegister());

        Hyperlink loginLink = new Hyperlink("Back to Login");
        loginLink.setOnAction(e -> navigateToLogin());

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
     * Handles the Register button click event. Validates all form fields and,
     * if valid, delegates registration to {@link com.portfoliotracker.service.AuthService}.
     * Navigates to the login screen on success, or displays an error message on failure.
     */
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String email = emailField.getText().trim();

        if (!ValidationUtils.isNotEmpty(username) ||
                !ValidationUtils.isNotEmpty(password) ||
                !ValidationUtils.isNotEmpty(email)) {
            showError("Please fill in all fields");
            return;
        }

        if (!ValidationUtils.passwordsMatch(password, confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

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
     * Displays an error message in the error label, making it visible to the user.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Navigates back to the Login screen by replacing the current scene.
     */
    private void navigateToLogin() {
        LoginController loginController = new LoginController(
                stage, authService, portfolioService,
                transactionService, marketDataService,
                watchlistService, alertService
        );
        stage.setScene(loginController.createScene());
    }
}