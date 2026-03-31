package com.portfoliotracker.controller;

import com.portfoliotracker.model.Holding;
import com.portfoliotracker.model.User;
import com.portfoliotracker.service.*;
import com.portfoliotracker.util.CurrencyUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
     *
     * @param stage              the primary JavaFX stage used to switch scenes
     * @param currentUser        the currently authenticated user
     * @param portfolioService   service for retrieving and calculating holdings data
     * @param authService        service responsible for authentication logic
     * @param transactionService service for transaction-related operations
     * @param marketDataService  service for fetching live market data
     * @param watchlistService   service for watchlist management
     * @param alertService       service for alert management
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

    /**
     * Builds and returns the holdings {@link Scene} composed of a sidebar and main content area.
     *
     * @return the JavaFX Scene for the holdings screen
     */
    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createContent());
        return new Scene(mainLayout, 1100, 700);
    }

    /**
     * Creates the main content area showing the page title, subtitle, and holdings table.
     *
     * @return a {@link VBox} containing the holdings content
     */
    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Holdings");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label subtitleLabel = new Label("Track your current assets and performance");
        subtitleLabel.setTextFill(Color.GRAY);

        TableView<Holding> holdingsTable = createHoldingsTable();

        content.getChildren().addAll(titleLabel, subtitleLabel, holdingsTable);
        return content;
    }

    /**
     * Creates and populates the holdings {@link TableView} with the current user's positions,
     * showing asset symbol, quantity, average buy price, current price, and profit/loss columns.
     *
     * @return a {@link TableView} populated with the user's holdings
     */
    private TableView<Holding> createHoldingsTable() {
        TableView<Holding> table = new TableView<>();

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
        addTransactionBtn.setOnAction(e -> navigateToAddTransaction());
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