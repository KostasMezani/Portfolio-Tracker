package com.portfoliotracker.controller;

import com.portfoliotracker.exception.InsufficientHoldingsException;
import com.portfoliotracker.exception.ValidationException;
import com.portfoliotracker.model.User;
import com.portfoliotracker.service.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class AddTransactionController {

    private final Stage stage;
    private final User currentUser;
    private final TransactionService transactionService;
    private final MarketDataService marketDataService;
    private final AuthService authService;
    private final PortfolioService portfolioService;
    private final WatchlistService watchlistService;
    private final AlertService alertService;

    private ToggleGroup typeToggle;
    private ComboBox<String> assetComboBox;
    private TextField quantityField;
    private TextField priceField;
    private Label totalAmountLabel;
    private Label errorLabel;
    private Label livePriceLabel;

    /**
     * Constructs an AddTransactionController with the authenticated user and all required services.
     *
     * @param stage              the primary JavaFX stage used to switch scenes
     * @param currentUser        the currently authenticated user
     * @param transactionService service for persisting buy and sell transactions
     * @param marketDataService  service for fetching live asset prices
     * @param authService        service responsible for authentication logic
     * @param portfolioService   service for portfolio-related operations
     * @param watchlistService   service for watchlist management
     * @param alertService       service for alert management
     */
    public AddTransactionController(Stage stage, User currentUser,
                                    TransactionService transactionService,
                                    MarketDataService marketDataService,
                                    AuthService authService,
                                    PortfolioService portfolioService,
                                    WatchlistService watchlistService,
                                    AlertService alertService) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.transactionService = transactionService;
        this.marketDataService = marketDataService;
        this.authService = authService;
        this.portfolioService = portfolioService;
        this.watchlistService = watchlistService;
        this.alertService = alertService;
    }

    /**
     * Builds and returns the add-transaction {@link Scene} composed of a sidebar
     * and the transaction form.
     *
     * @return the JavaFX Scene for the add transaction screen
     */
    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createContent());
        return new Scene(mainLayout, 1100, 700);
    }

    /**
     * Creates the transaction form with fields for transaction type, asset selection,
     * quantity, live price, total amount, and save/cancel buttons.
     *
     * @return a {@link VBox} containing the form content
     */
    private VBox createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(30));
        content.setMaxWidth(600);
        content.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("Add Transaction");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // BUY / SELL toggle
        RadioButton buyBtn = new RadioButton("BUY");
        RadioButton sellBtn = new RadioButton("SELL");
        typeToggle = new ToggleGroup();
        buyBtn.setToggleGroup(typeToggle);
        sellBtn.setToggleGroup(typeToggle);
        buyBtn.setSelected(true);
        HBox typeBox = new HBox(10, buyBtn, sellBtn);

        // Asset selector
        assetComboBox = new ComboBox<>();
        assetComboBox.getItems().addAll("bitcoin", "ethereum", "solana");
        assetComboBox.setPromptText("Select asset");
        assetComboBox.setOnAction(e -> fetchLivePrice());

        // Live price label
        livePriceLabel = new Label("Select an asset to see live price");
        livePriceLabel.setTextFill(Color.GRAY);

        // Quantity field
        quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");
        quantityField.setOnKeyTyped(e -> updateTotalAmount());

        // Price field
        priceField = new TextField();
        priceField.setPromptText("Price per unit");
        priceField.setEditable(false);

        // Total amount
        totalAmountLabel = new Label("Total Amount: €0.00");
        totalAmountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Error label
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Buttons
        Button saveBtn = new Button("Save Transaction");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> handleSave());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> navigateToTransactions());

        content.getChildren().addAll(
                titleLabel,
                new Label("Transaction Type:"), typeBox,
                new Label("Asset:"), assetComboBox,
                livePriceLabel,
                new Label("Quantity:"), quantityField,
                new Label("Price per Unit:"), priceField,
                totalAmountLabel,
                errorLabel,
                saveBtn, cancelBtn
        );

        return content;
    }

    /**
     * Fetches the live price for the currently selected asset and populates the price field.
     * Updates the live price label with the fetched value or an error message on failure.
     */
    private void fetchLivePrice() {
        String symbol = assetComboBox.getValue();
        if (symbol == null) return;

        try {
            BigDecimal price = marketDataService.getCurrentPrice(symbol);
            priceField.setText(price.toString());
            livePriceLabel.setText("Live price: €" + price + " ✓");
            livePriceLabel.setTextFill(Color.GREEN);
            updateTotalAmount();
        } catch (Exception e) {
            livePriceLabel.setText("Could not fetch live price");
            livePriceLabel.setTextFill(Color.RED);
        }
    }

    /**
     * Recalculates and updates the total amount label based on the current values of
     * the quantity and price fields. Resets to €0.00 if either field is invalid.
     */
    private void updateTotalAmount() {
        try {
            BigDecimal quantity = new BigDecimal(quantityField.getText());
            BigDecimal price = new BigDecimal(priceField.getText());
            BigDecimal total = quantity.multiply(price);
            totalAmountLabel.setText("Total Amount: €" + total);
        } catch (Exception e) {
            totalAmountLabel.setText("Total Amount: €0.00");
        }
    }

    /**
     * Handles the Save Transaction button click event. Validates inputs, determines whether
     * the transaction is a BUY or SELL, and delegates to the appropriate service method.
     * Navigates to the Transactions screen on success, or shows an error message on failure.
     */
    private void handleSave() {
        String symbol = assetComboBox.getValue();
        String quantityText = quantityField.getText().trim();
        String priceText = priceField.getText().trim();

        if (symbol == null || quantityText.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        try {
            BigDecimal quantity = new BigDecimal(quantityText);
            BigDecimal price = new BigDecimal(priceText);

            RadioButton selectedType = (RadioButton) typeToggle.getSelectedToggle();
            boolean isBuy = selectedType.getText().equals("BUY");

            if (isBuy) {
                transactionService.addBuyTransaction(
                        currentUser.getId(), symbol, quantity, price);
            } else {
                transactionService.addSellTransaction(
                        currentUser.getId(), symbol, quantity, price);
            }

            navigateToTransactions();

        } catch (InsufficientHoldingsException e) {
            showError("Not enough assets to sell!");
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers");
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
     * Creates the navigation sidebar containing buttons for all main application views
     * and a logout button anchored at the bottom.
     *
     * @return a {@link VBox} representing the sidebar
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #1a2942;");

        Label appTitle = new Label("Portfolio Tracker");
        appTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        appTitle.setTextFill(Color.WHITE);

        Button dashboardBtn = createSidebarButton("Dashboard");
        Button transactionsBtn = createSidebarButton("Transactions");
        Button addTransactionBtn = createSidebarButton("Add Transaction");
        Button holdingsBtn = createSidebarButton("Holdings");
        Button watchlistBtn = createSidebarButton("Watchlist");

        dashboardBtn.setOnAction(e -> navigateToDashboard());
        transactionsBtn.setOnAction(e -> navigateToTransactions());
        holdingsBtn.setOnAction(e -> navigateToHoldings());
        watchlistBtn.setOnAction(e -> navigateToWatchlist());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> handleLogout());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                appTitle, dashboardBtn, transactionsBtn,
                addTransactionBtn, holdingsBtn, watchlistBtn,
                spacer, logoutBtn
        );
        return sidebar;
    }

    /**
     * Creates a styled sidebar navigation button that spans the full width of the sidebar.
     *
     * @param text the label text to display on the button
     * @return a styled {@link Button} for use in the sidebar
     */
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        return btn;
    }

    /**
     * Navigates to the Dashboard screen by replacing the current scene.
     */
    private void navigateToDashboard() {
        DashboardController controller = new DashboardController(
                stage, currentUser, portfolioService, authService,
                transactionService, marketDataService, watchlistService, alertService
        );
        stage.setScene(controller.createScene());
    }

    /**
     * Navigates to the Transactions screen by replacing the current scene.
     */
    private void navigateToTransactions() {
        TransactionsController controller = new TransactionsController(
                stage, currentUser, transactionService, authService,
                portfolioService, marketDataService, watchlistService, alertService
        );
        stage.setScene(controller.createScene());
    }

    /**
     * Navigates to the Holdings screen by replacing the current scene.
     */
    private void navigateToHoldings() {
        HoldingsController controller = new HoldingsController(
                stage, currentUser, portfolioService, authService,
                transactionService, marketDataService, watchlistService, alertService
        );
        stage.setScene(controller.createScene());
    }

    /**
     * Navigates to the Watchlist screen by replacing the current scene.
     */
    private void navigateToWatchlist() {
        WatchlistController controller = new WatchlistController(
                stage, currentUser, watchlistService, marketDataService,
                authService, portfolioService, transactionService, alertService
        );
        stage.setScene(controller.createScene());
    }

    /**
     * Handles the Logout button click event. Clears the current session and navigates
     * back to the Login screen.
     */
    private void handleLogout() {
        LoginController loginController = new LoginController(
                stage, authService, portfolioService,
                transactionService, marketDataService,
                watchlistService, alertService
        );
        stage.setScene(loginController.createScene());
    }
}