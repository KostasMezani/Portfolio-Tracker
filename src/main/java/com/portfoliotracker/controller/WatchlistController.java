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

    private void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );
    }

    /**
     * Builds and returns the watchlist {@link Scene}.
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

        Label titleLabel = new Label("Watchlist");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("Track assets you are interested in");
        subtitleLabel.getStyleClass().add("subtitle-label");

        HBox addBar = new HBox(10);
        TextField symbolField = new TextField();
        symbolField.setPromptText("Enter asset symbol e.g. bitcoin");
        symbolField.setPrefWidth(300);

        Button addBtn = new Button("Add to Watchlist");
        addBtn.getStyleClass().add("primary-button");
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
     * Creates the watchlist {@link TableView}.
     */
    private TableView<WatchlistItem> createWatchlistTable() {
        TableView<WatchlistItem> table = new TableView<>();
        table.getStyleClass().add("table-view");

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
                removeBtn.getStyleClass().add("danger-button");
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
     * Clears and reloads the watchlist table.
     */
    private void loadWatchlist() {
        watchlistTable.getItems().clear();
        List<WatchlistItem> items = watchlistService.getWatchlist(currentUser.getId());
        watchlistTable.getItems().addAll(items);
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
        holdingsBtn.setOnAction(e -> navigateToHoldings());

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

    private void navigateToHoldings() {
        HoldingsController controller = new HoldingsController(
                stage, currentUser, portfolioService, authService,
                transactionService, marketDataService, watchlistService, alertService
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