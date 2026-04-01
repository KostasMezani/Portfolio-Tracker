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

    private void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );
    }

    /**
     * Builds and returns the add-transaction {@link Scene}.
     */
    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createContent());

        Scene scene = new Scene(mainLayout, 1100, 700);
        applyStylesheet(scene);
        return scene;
    }

    /**
     * Creates the transaction form.
     */
    private VBox createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(30));
        content.setMaxWidth(600);
        content.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("Add Transaction");
        titleLabel.getStyleClass().add("title-label");

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
        assetComboBox.setMaxWidth(Double.MAX_VALUE);
        assetComboBox.setOnAction(e -> fetchLivePrice());

        // Live price label
        livePriceLabel = new Label("Select an asset to see live price");
        livePriceLabel.getStyleClass().add("subtitle-label");

        // Quantity field
        quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");
        quantityField.setMaxWidth(Double.MAX_VALUE);
        quantityField.setOnKeyTyped(e -> updateTotalAmount());

        // Price field
        priceField = new TextField();
        priceField.setPromptText("Price per unit");
        priceField.setEditable(false);
        priceField.setMaxWidth(Double.MAX_VALUE);

        // Total amount
        totalAmountLabel = new Label("Total Amount: €0.00");
        totalAmountLabel.getStyleClass().add("title-label");

        // Error label
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        // Buttons
        Button saveBtn = new Button("Save Transaction");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setOnAction(e -> handleSave());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.getStyleClass().add("secondary-button");
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
     * Fetches the live price for the selected asset.
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
     * Recalculates the total amount label.
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
     * Handles the Save Transaction button click.
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
     * Displays an error message.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Creates the navigation sidebar.
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        sidebar.setPadding(new Insets(20));
        sidebar.getStyleClass().add("sidebar");

        Label appTitle = new Label("Portfolio Tracker");
        appTitle.getStyleClass().add("sidebar-title");

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
        logoutBtn.getStyleClass().add("logout-button");
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
     * Creates a styled sidebar button.
     */
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        return btn;
    }

    private void navigateToDashboard() {
        DashboardController controller = new DashboardController(
                stage, currentUser, portfolioService, authService,
                transactionService, marketDataService, watchlistService, alertService
        );
        stage.setScene(controller.createScene());
    }

    private void navigateToTransactions() {
        TransactionsController controller = new TransactionsController(
                stage, currentUser, transactionService, authService,
                portfolioService, marketDataService, watchlistService, alertService
        );
        stage.setScene(controller.createScene());
    }

    private void navigateToHoldings() {
        HoldingsController controller = new HoldingsController(
                stage, currentUser, portfolioService, authService,
                transactionService, marketDataService, watchlistService, alertService
        );
        stage.setScene(controller.createScene());
    }

    private void navigateToWatchlist() {
        WatchlistController controller = new WatchlistController(
                stage, currentUser, watchlistService, marketDataService,
                authService, portfolioService, transactionService, alertService
        );
        stage.setScene(controller.createScene());
    }

    private void handleLogout() {
        LoginController loginController = new LoginController(
                stage, authService, portfolioService,
                transactionService, marketDataService,
                watchlistService, alertService
        );
        stage.setScene(loginController.createScene());
    }
}