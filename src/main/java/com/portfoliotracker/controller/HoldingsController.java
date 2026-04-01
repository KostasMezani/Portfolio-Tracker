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

import java.util.List;

public class HoldingsController {

    private final Stage stage;
    private final User currentUser;
    private final PortfolioService portfolioService;
    private final AuthService authService;
    private final TransactionService transactionService;
    private final MarketDataService marketDataService;
    private final WatchlistService watchlistService;
    private final AlertService alertService;

    /**
     * Constructs a HoldingsController with the authenticated user and all required services.
     */
    public HoldingsController(Stage stage, User currentUser,
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
     * Builds and returns the holdings {@link Scene}.
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
     * Creates the main content area.
     */
    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Holdings");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("Track your current assets and performance");
        subtitleLabel.getStyleClass().add("subtitle-label");

        TableView<Holding> holdingsTable = createHoldingsTable();

        content.getChildren().addAll(titleLabel, subtitleLabel, holdingsTable);
        return content;
    }

    /**
     * Creates and populates the holdings {@link TableView}.
     */
    private TableView<Holding> createHoldingsTable() {
        TableView<Holding> table = new TableView<>();
        table.getStyleClass().add("table-view");

        TableColumn<Holding, String> assetCol = new TableColumn<>("Asset");
        assetCol.setCellValueFactory(data ->
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
                assetCol, quantityCol, avgPriceCol, currentPriceCol, plCol
        );

        List<Holding> holdings = portfolioService.getHoldings(currentUser.getId());
        table.getItems().addAll(holdings);

        return table;
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
        addTransactionBtn.setOnAction(e -> navigateToAddTransaction());
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

    private void navigateToAddTransaction() {
        AddTransactionController controller = new AddTransactionController(
                stage, currentUser, transactionService, marketDataService,
                authService, portfolioService, watchlistService, alertService
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