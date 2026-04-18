package com.portfoliotracker.controller;

import com.portfoliotracker.model.Holding;
import com.portfoliotracker.model.PriceAlert;
import com.portfoliotracker.model.User;
import com.portfoliotracker.service.*;
import com.portfoliotracker.util.CurrencyUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardController {

    private final Stage stage;
    private final User currentUser;
    private final PortfolioService portfolioService;
    private final AuthService authService;
    private final TransactionService transactionService;
    private final MarketDataService marketDataService;
    private final WatchlistService watchlistService;
    private final AlertService alertService;

    private ScheduledExecutorService alertScheduler;

    /**
     * Constructs a DashboardController with the authenticated user and all required services.
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
     * Builds and returns the dashboard {@link Scene}.
     */
    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createContent());

        Scene scene = new Scene(mainLayout, 1100, 700);
        applyStylesheet(scene);

        // Ξεκίνα τον alert checker
        startAlertChecker();

        // Σταμάτα τον scheduler όταν αλλάξει η σκηνή
        stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != scene && alertScheduler != null && !alertScheduler.isShutdown()) {
                alertScheduler.shutdown();
            }
        });

        return scene;
    }

    /**
     * Starts the background alert checker that runs every 60 seconds.
     */
    private void startAlertChecker() {
        alertScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // κλείνει αυτόματα με την εφαρμογή
            return t;
        });

        alertScheduler.scheduleAtFixedRate(() -> {
            try {
                List<PriceAlert> triggered = alertService.checkAlerts(currentUser.getId());
                if (!triggered.isEmpty()) {
                    Platform.runLater(() -> showAlertNotifications(triggered));
                }
            } catch (Exception e) {
                System.out.println("Alert check failed: " + e.getMessage());
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    /**
     * Shows a notification dialog for each triggered alert.
     * @param triggered the list of triggered alerts
     */
    private void showAlertNotifications(List<PriceAlert> triggered) {
        for (PriceAlert alert : triggered) {
            Alert notification = new Alert(Alert.AlertType.INFORMATION);
            notification.setTitle("🔔 Price Alert Triggered!");
            notification.setHeaderText("Alert for " + alert.getAssetSymbol().toUpperCase());
            notification.setContentText(
                    alert.getAssetSymbol().toUpperCase() + " has gone " +
                            alert.getCondition() + " " +
                            CurrencyUtils.formatCurrency(alert.getTargetPrice()) + "!"
            );
            notification.showAndWait();
        }
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
     * Creates a styled sidebar button.
     */
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        return btn;
    }

    /**
     * Creates the main content area.
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
     * Creates the summary cards.
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
     * Creates a summary card.
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
     * Creates the assets table.
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

    private void navigateToTransactions() {
        if (alertScheduler != null) alertScheduler.shutdown();
        TransactionsController controller = new TransactionsController(
                stage, currentUser, transactionService, authService,
                portfolioService, marketDataService, watchlistService, alertService
        );
        stage.setScene(controller.createScene());
    }

    private void navigateToAddTransaction() {
        if (alertScheduler != null) alertScheduler.shutdown();
        AddTransactionController controller = new AddTransactionController(
                stage, currentUser, transactionService, marketDataService,
                authService, portfolioService, watchlistService, alertService
        );
        stage.setScene(controller.createScene());
    }

    private void navigateToHoldings() {
        if (alertScheduler != null) alertScheduler.shutdown();
        HoldingsController controller = new HoldingsController(
                stage, currentUser, portfolioService, authService,
                transactionService, marketDataService, watchlistService, alertService
        );
        stage.setScene(controller.createScene());
    }

    private void navigateToWatchlist() {
        if (alertScheduler != null) alertScheduler.shutdown();
        WatchlistController controller = new WatchlistController(
                stage, currentUser, watchlistService, marketDataService,
                authService, portfolioService, transactionService, alertService
        );
        stage.setScene(controller.createScene());
    }

    private void handleLogout() {
        if (alertScheduler != null) alertScheduler.shutdown();
        LoginController loginController = new LoginController(
                stage, authService, portfolioService,
                transactionService, marketDataService,
                watchlistService, alertService
        );
        stage.setScene(loginController.createScene());
    }
}