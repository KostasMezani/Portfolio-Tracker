package com.portfoliotracker.controller;

import com.portfoliotracker.model.PriceSnapshot;
import com.portfoliotracker.model.User;
import com.portfoliotracker.model.WatchlistItem;
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

public class WatchlistController {

    private final Stage stage;
    private final User currentUser;
    private final WatchlistService watchlistService;
    private final MarketDataService marketDataService;
    private final AuthService authService;
    private final PortfolioService portfolioService;
    private final TransactionService transactionService;
    private final AlertService alertService;

    private TableView<WatchlistItem> watchlistTable;

    /**
     * Constructs a WatchlistController with the authenticated user and all required services.
     *
     * @param stage              the primary JavaFX stage used to switch scenes
     * @param currentUser        the currently authenticated user
     * @param watchlistService   service for adding, removing, and retrieving watchlist items
     * @param marketDataService  service for fetching live asset prices and snapshots
     * @param authService        service responsible for authentication logic
     * @param portfolioService   service for portfolio-related operations
     * @param transactionService service for transaction-related operations
     * @param alertService       service for alert management
     */
    public WatchlistController(Stage stage, User currentUser,
                               WatchlistService watchlistService,
                               MarketDataService marketDataService,
                               AuthService authService,
                               PortfolioService portfolioService,
                               TransactionService transactionService,
                               AlertService alertService) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.watchlistService = watchlistService;
        this.marketDataService = marketDataService;
        this.authService = authService;
        this.portfolioService = portfolioService;
        this.transactionService = transactionService;
        this.alertService = alertService;
    }

    /**
     * Builds and returns the watchlist {@link Scene} composed of a sidebar and main content area.
     *
     * @return the JavaFX Scene for the watchlist screen
     */
    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createContent());
        return new Scene(mainLayout, 1100, 700);
    }

    /**
     * Creates the main content area including the page title, subtitle, add-asset input bar,
     * and the watchlist table.
     *
     * @return a {@link VBox} containing the watchlist content
     */
    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Watchlist");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label subtitleLabel = new Label("Track assets you are interested in");
        subtitleLabel.setTextFill(Color.GRAY);

        HBox addBar = new HBox(10);
        TextField symbolField = new TextField();
        symbolField.setPromptText("Enter asset symbol e.g. bitcoin");
        Button addBtn = new Button("Add to Watchlist");
        addBtn.setOnAction(e -> {
            String symbol = symbolField.getText().trim();
            if (!symbol.isEmpty()) {
                watchlistService.addToWatchlist(currentUser.getId(), symbol);
                symbolField.clear();
                loadWatchlist();
            }
        });
        addBar.getChildren().addAll(symbolField, addBtn);

        watchlistTable = createWatchlistTable();
        loadWatchlist();

        content.getChildren().addAll(
                titleLabel, subtitleLabel, addBar, watchlistTable
        );
        return content;
    }

    /**
     * Creates the watchlist {@link TableView} with columns for asset symbol, current price,
     * 24-hour price change, and a remove action button per row.
     *
     * @return a configured {@link TableView} for displaying watchlist items
     */
    private TableView<WatchlistItem> createWatchlistTable() {
        TableView<WatchlistItem> table = new TableView<>();

        TableColumn<WatchlistItem, String> assetCol = new TableColumn<>("Asset");
        assetCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getAssetSymbol()));

        TableColumn<WatchlistItem, String> priceCol = new TableColumn<>("Current Price");
        priceCol.setCellValueFactory(data -> {
            try {
                PriceSnapshot snapshot = marketDataService
                        .getPriceSnapshot(data.getValue().getAssetSymbol());
                return new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtils.formatCurrency(snapshot.getCurrentPrice()));
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        TableColumn<WatchlistItem, String> changeCol = new TableColumn<>("24h Change");
        changeCol.setCellValueFactory(data -> {
            try {
                PriceSnapshot snapshot = marketDataService
                        .getPriceSnapshot(data.getValue().getAssetSymbol());
                return new javafx.beans.property.SimpleStringProperty(
                        snapshot.getChange24h() + "%");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        TableColumn<WatchlistItem, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.setOnAction(e -> {
                    WatchlistItem item = getTableView().getItems().get(getIndex());
                    watchlistService.removeFromWatchlist(
                            currentUser.getId(), item.getAssetSymbol());
                    loadWatchlist();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        table.getColumns().addAll(assetCol, priceCol, changeCol, actionCol);
        return table;
    }

    /**
     * Clears and reloads the watchlist table with all items tracked by the current user.
     */
    private void loadWatchlist() {
        watchlistTable.getItems().clear();
        List<WatchlistItem> items = watchlistService.getWatchlist(currentUser.getId());
        watchlistTable.getItems().addAll(items);
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
        holdingsBtn.setOnAction(e -> navigateToHoldings());

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