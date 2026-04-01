package com.portfoliotracker.controller;

import com.portfoliotracker.model.User;
import com.portfoliotracker.service.*;
import com.portfoliotracker.exception.AuthenticationException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {

    private final AuthService authService;
    private final Stage stage;
    private final PortfolioService portfolioService;
    private final TransactionService transactionService;
    private final MarketDataService marketDataService;
    private final WatchlistService watchlistService;
    private final AlertService alertService;

    // UI Elements
    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;

    /**
     * Constructs a LoginController with all required services and the primary stage.
     *
     * @param stage              the primary JavaFX stage used to switch scenes
     * @param authService        service responsible for authentication logic
     * @param portfolioService   service for portfolio-related operations
     * @param transactionService service for transaction-related operations
     * @param marketDataService  service for fetching live market data
     * @param watchlistService   service for watchlist management
     * @param alertService       service for alert management
     */
    public LoginController(Stage stage, AuthService authService,
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

    private void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );
    }

    /**
     * Builds and returns the login {@link Scene} containing the username/password form.
     *
     * @return the JavaFX Scene for the login screen
     */
    public Scene createScene() {
        // Title
        Label titleLabel = new Label("Portfolio Tracker");
        titleLabel.getStyleClass().add("login-title");

        // Username field
        Label usernameLabel = new Label("Username:");
        usernameLabel.getStyleClass().add("subtitle-label");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(300);

        // Password field
        Label passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().add("subtitle-label");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(300);

        // Error label
        errorLabel = new Label();
        errorLabel.setVisible(false);
        errorLabel.getStyleClass().add("error-label");

        // Login button
        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(300);
        loginButton.setOnAction(e -> handleLogin());
        loginButton.getStyleClass().add("primary-button");

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
        layout.getStyleClass().add("login-container");

        Scene scene = new Scene(layout, 800, 600);
        applyStylesheet(scene);
        return scene;
    }

    /**
     * Handles the Login button click event. Validates the username and password fields,
     * delegates authentication to {@link com.portfoliotracker.service.AuthService},
     * and navigates to the Dashboard on success or shows an error on failure.
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
     * Displays an error message in the error label, making it visible to the user.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Navigates to the Register screen by replacing the current scene.
     */
    private void navigateToRegister() {
        RegisterController registerController = new RegisterController(
                stage, authService, portfolioService,
                transactionService, marketDataService,
                watchlistService, alertService
        );
        stage.setScene(registerController.createScene());
    }

    /**
     * Navigates to the Dashboard screen for the authenticated user.
     *
     * @param user the authenticated {@link User} whose data will be displayed
     */
    private void navigateToDashboard(User user) {
        DashboardController dashboardController = new DashboardController(
                stage, user, portfolioService, authService,
                transactionService, marketDataService,
                watchlistService, alertService
        );
        stage.setScene(dashboardController.createScene());
    }
}