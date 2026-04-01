package com.portfoliotracker.controller;

import com.portfoliotracker.model.Holding;
import com.portfoliotracker.model.User;
import com.portfoliotracker.service.*;
import com.portfoliotracker.util.CurrencyUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class DashboardController {

    private final Stage stage;
    private final User currentUser;
    private final PortfolioService portfolioService;
    private final AuthService authService;
    private final TransactionService transactionService;
    private final MarketDataService marketDataService;
    private final WatchlistService watchlistService;
    private final AlertService alertService;

    /**
     * Constructs a DashboardController with the authenticated user and all required services.
     *
     * @param stage              the primary JavaFX stage used to switch scenes
     * @param currentUser        the currently authenticated user
     * @param portfolioService   service for portfolio value and holdings calculations
     * @param authService        service responsible for authentication logic
     * @param transactionService service for transaction-related operations
     * @param marketDataService  service for fetching live market data
     * @param watchlistService   service for watchlist management
     * @param alertService       service for alert management
     */
    public DashboardController(Stage stage, User currentUser,
                               PortfolioService portfolioService,
                               AuthService authService,
                               TransactionService transactionService,
                               MarketDataService marketDataService,
                               WatchlistService watchlistService,
                               AlertService alertService) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.portfolioService = portfolioService;
        this.authService = authService;
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
     * Builds and returns the dashboard {@link Scene} composed of a sidebar and main content area.
     *
     * @return the JavaFX Scene for the dashboard screen
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
     * Creates the navigation sidebar containing buttons for all main application views
     * and a logout button anchored at the bottom.
     *
     * @return a {@link VBox} representing the sidebar
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

        transactionsBtn.setOnAction(e -> navigateToTransactions());
        addTransactionBtn.setOnAction(e -> navigateToAddTransaction());
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
     * Creates a styled sidebar navigation button that spans the full width of the sidebar.
     *
     * @param text the label text to display on the button
     * @return a styled {@link Button} for use in the sidebar
     */
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        return btn;
    }

    /**
     * Creates the main content area of the dashboard, including the welcome label,
     * portfolio summary cards, and the assets table.
     *
     * @return a {@link VBox} containing the dashboard content
     */
    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername());
        welcomeLabel.getStyleClass().add("title-label");

        HBox summaryCards = createSummaryCards();

        Label assetsLabel = new Label("Your Assets");
        assetsLabel.getStyleClass().add("title-label");

        TableView<Holding> assetsTable = createAssetsTable();

        content.getChildren().addAll(
                welcomeLabel, summaryCards, assetsLabel, assetsTable
        );
        return content;
    }

    /**
     * Creates the row of summary cards showing total portfolio value, total profit/loss,
     * and total amount invested.
     *
     * @return an {@link HBox} containing the three summary cards
     */
    private HBox createSummaryCards() {
        BigDecimal totalValue = portfolioService.getPortfolioValue(currentUser.getId());
        BigDecimal totalProfitLoss = portfolioService.getTotalProfitLoss(currentUser.getId());
        BigDecimal totalInvested = portfolioService.getTotalInvested(currentUser.getId());

        VBox valueCard = createCard("Total Portfolio Value",
                CurrencyUtils.formatCurrency(totalValue), null);
        VBox plCard = createCard("Total Profit / Loss",
                CurrencyUtils.formatCurrency(totalProfitLoss), totalProfitLoss);
        VBox investedCard = createCard("Total Invested",
                CurrencyUtils.formatCurrency(totalInvested), null);

        HBox cards = new HBox(20);
        cards.getChildren().addAll(valueCard, plCard, investedCard);
        return cards;
    }

    /**
     * Creates a single summary card displaying a title and a coloured value.
     *
     * @param title      the card's heading label (e.g. "Total Portfolio Value")
     * @param value      the formatted value to display (e.g. "€12,345.00")
     * @param rawValue   the raw value (e.g. 12345.00)
     * @return a styled {@link VBox} card
     */
    private VBox createCard(String title, String value, BigDecimal rawValue) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);
        card.getStyleClass().add("card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label valueLabel = new Label(value);

        if (rawValue == null || rawValue.compareTo(BigDecimal.ZERO) == 0) {
            valueLabel.getStyleClass().add("card-value");
        } else if (rawValue.compareTo(BigDecimal.ZERO) > 0) {
            valueLabel.getStyleClass().add("card-value-positive");
        } else {
            valueLabel.getStyleClass().add("card-value-negative");
        }

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    /**
     * Creates and populates the assets {@link TableView} with the current user's holdings,
     * showing symbol, quantity, average buy price, current price, and profit/loss columns.
     *
     * @return a {@link TableView} populated with the user's holdings
     */
    private TableView<Holding> createAssetsTable() {
        TableView<Holding> table = new TableView<>();
        table.getStyleClass().add("table-view");

        TableColumn<Holding, String> symbolCol = new TableColumn<>("Asset");
        symbolCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getAsset().getSymbol()));

        TableColumn<Holding, String> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getQuantity().toString()));

        TableColumn<Holding, String> avgPriceCol = new TableColumn<>("Avg Buy Price");
        avgPriceCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtils.formatCurrency(data.getValue().getAvgPrice())));

        TableColumn<Holding, String> currentPriceCol = new TableColumn<>("Current Price");
        currentPriceCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtils.formatCurrency(data.getValue().getCurrentPrice())));

        TableColumn<Holding, String> plCol = new TableColumn<>("Profit / Loss");
        plCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtils.formatCurrency(data.getValue().getProfitLoss())));

        table.getColumns().addAll(
                symbolCol, quantityCol, avgPriceCol, currentPriceCol, plCol
        );

        List<Holding> holdings = portfolioService.getHoldings(currentUser.getId());
        table.getItems().addAll(holdings);

        return table;
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
     * Navigates to the Add Transaction screen by replacing the current scene.
     */
    private void navigateToAddTransaction() {
        AddTransactionController controller = new AddTransactionController(
                stage, currentUser, transactionService, marketDataService,
                authService, portfolioService, watchlistService, alertService
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