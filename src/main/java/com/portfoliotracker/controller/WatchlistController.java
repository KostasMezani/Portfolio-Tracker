package com.portfoliotracker.controller;

import com.portfoliotracker.model.AlertCondition;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

        // Dropdown αντί για TextField
        HBox addBar = new HBox(10);
        ComboBox<String> symbolComboBox = new ComboBox<>();
        symbolComboBox.getItems().addAll("bitcoin", "ethereum", "solana");
        symbolComboBox.setPromptText("Select asset");
        symbolComboBox.setPrefWidth(300);

        Button addBtn = new Button("Add to Watchlist");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> {
            String symbol = symbolComboBox.getValue();
            if (symbol != null) {
                watchlistService.addToWatchlist(currentUser.getId(), symbol);
                symbolComboBox.setValue(null);
                loadWatchlist();
            }
        });
        addBar.getChildren().addAll(symbolComboBox, addBtn);

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

        // Alert column
        TableColumn<WatchlistItem, Void> alertCol = new TableColumn<>("Alert");
        alertCol.setCellFactory(col -> new TableCell<>() {
            private final Button alertBtn = new Button("Set Alert");
            {
                alertBtn.getStyleClass().add("primary-button");
                alertBtn.setOnAction(e -> {
                    WatchlistItem item = getTableView().getItems().get(getIndex());
                    handleSetAlert(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : alertBtn);
            }
        });

        // Remove column
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

        table.getColumns().addAll(assetCol, priceCol, changeCol, alertCol, actionCol);
        return table;
    }

    /**
     * Opens a dialog for the user to set a price alert for a watchlist item.
     */
    private void handleSetAlert(WatchlistItem item) {
        // Dialog για να εισάγει ο χρήστης την τιμή στόχο
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Set Price Alert");
        dialog.setHeaderText("Set alert for: " + item.getAssetSymbol());

        // Condition dropdown
        Label conditionLabel = new Label("Condition:");
        ComboBox<String> conditionBox = new ComboBox<>();
        conditionBox.getItems().addAll("ABOVE", "BELOW");
        conditionBox.setValue("ABOVE");

        // Target price field
        Label priceLabel = new Label("Target Price (€):");
        TextField targetPriceField = new TextField();
        targetPriceField.setPromptText("e.g. 35000");

        // Show current price as reference
        Label currentPriceLabel = new Label();
        try {
            BigDecimal currentPrice = marketDataService.getCurrentPrice(item.getAssetSymbol());
            currentPriceLabel.setText("Current price: " + CurrencyUtils.formatCurrency(currentPrice));
        } catch (Exception e) {
            currentPriceLabel.setText("Current price: N/A");
        }

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(20));
        dialogContent.getChildren().addAll(
                currentPriceLabel,
                conditionLabel, conditionBox,
                priceLabel, targetPriceField
        );

        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                BigDecimal targetPrice = new BigDecimal(targetPriceField.getText().trim());
                AlertCondition condition = conditionBox.getValue().equals("ABOVE")
                        ? AlertCondition.ABOVE
                        : AlertCondition.BELOW;

                alertService.createAlert(
                        currentUser.getId(),
                        item.getAssetSymbol(),
                        condition,
                        targetPrice
                );

                // Confirmation message
                Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                confirmation.setTitle("Alert Created");
                confirmation.setContentText("Alert set for " + item.getAssetSymbol() +
                        " " + condition + " " + CurrencyUtils.formatCurrency(targetPrice));
                confirmation.showAndWait();

            } catch (NumberFormatException e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Invalid Input");
                error.setContentText("Please enter a valid price");
                error.showAndWait();
            }
        }
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