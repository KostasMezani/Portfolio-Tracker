package com.portfoliotracker.controller;

import com.portfoliotracker.model.Transaction;
import com.portfoliotracker.model.User;
import com.portfoliotracker.service.*;
import com.portfoliotracker.util.CurrencyUtils;
import com.portfoliotracker.util.DateUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class TransactionsController {

    private final Stage stage;
    private final User currentUser;
    private final TransactionService transactionService;
    private final AuthService authService;
    private final PortfolioService portfolioService;
    private final MarketDataService marketDataService;
    private final WatchlistService watchlistService;
    private final AlertService alertService;

    private TextField searchField;
    private ComboBox<String> filterComboBox;
    private TableView<Transaction> transactionsTable;

    /**
     * Constructs a TransactionsController with the authenticated user and all required services.
     *
     * @param stage              the primary JavaFX stage used to switch scenes
     * @param currentUser        the currently authenticated user
     * @param transactionService service for loading, filtering, and deleting transactions
     * @param authService        service responsible for authentication logic
     * @param portfolioService   service for portfolio-related operations
     * @param marketDataService  service for fetching live market data
     * @param watchlistService   service for watchlist management
     * @param alertService       service for alert management
     */
    public TransactionsController(Stage stage, User currentUser,
                                  TransactionService transactionService,
                                  AuthService authService,
                                  PortfolioService portfolioService,
                                  MarketDataService marketDataService,
                                  WatchlistService watchlistService,
                                  AlertService alertService) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.transactionService = transactionService;
        this.authService = authService;
        this.portfolioService = portfolioService;
        this.marketDataService = marketDataService;
        this.watchlistService = watchlistService;
        this.alertService = alertService;
    }

    /**
     * Builds and returns the transactions {@link Scene} composed of a sidebar and main content area.
     *
     * @return the JavaFX Scene for the transactions screen
     */
    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createContent());
        return new Scene(mainLayout, 1100, 700);
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
        addTransactionBtn.setOnAction(e -> navigateToAddTransaction());
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
     * Creates the main content area including the page title, search/filter toolbar,
     * and the transactions table.
     *
     * @return a {@link VBox} containing the transactions content
     */
    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Transactions");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        HBox toolbar = new HBox(10);
        searchField = new TextField();
        searchField.setPromptText("Search transactions...");
        searchField.setPrefWidth(300);
        searchField.setOnKeyTyped(e -> filterTransactions());

        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("All Types", "BUY", "SELL");
        filterComboBox.setValue("All Types");
        filterComboBox.setOnAction(e -> filterTransactions());

        Button addBtn = new Button("+ Add Transaction");
        addBtn.setOnAction(e -> navigateToAddTransaction());

        toolbar.getChildren().addAll(searchField, filterComboBox, addBtn);

        transactionsTable = createTransactionsTable();
        loadTransactions();

        content.getChildren().addAll(titleLabel, toolbar, transactionsTable);
        return content;
    }

    /**
     * Creates the transactions {@link TableView} with columns for date, asset, type,
     * quantity, price, total value, and a delete action button per row.
     *
     * @return a configured {@link TableView} for displaying transactions
     */
    private TableView<Transaction> createTransactionsTable() {
        TableView<Transaction> table = new TableView<>();

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        DateUtils.formatDateTime(data.getValue().getDate())));

        TableColumn<Transaction, String> assetCol = new TableColumn<>("Asset");
        assetCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getAssetSymbol()));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getType().toString()));

        TableColumn<Transaction, String> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getQuantity().toString()));

        TableColumn<Transaction, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtils.formatCurrency(data.getValue().getPricePerUnit())));

        TableColumn<Transaction, String> totalCol = new TableColumn<>("Total Value");
        totalCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtils.formatCurrency(data.getValue().getTotalAmount())));

        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setOnAction(e -> {
                    Transaction t = getTableView().getItems().get(getIndex());
                    handleDelete(t);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().addAll(
                dateCol, assetCol, typeCol,
                quantityCol, priceCol, totalCol, actionCol
        );
        return table;
    }

    /**
     * Clears and reloads the transactions table with all transactions belonging
     * to the current user.
     */
    private void loadTransactions() {
        transactionsTable.getItems().clear();
        List<Transaction> transactions = transactionService
                .getTransaction(currentUser.getId());
        transactionsTable.getItems().addAll(transactions);
    }

    /**
     * Filters the transactions table based on the current search text and the selected
     * transaction type filter (All Types, BUY, or SELL).
     */
    private void filterTransactions() {
        String search = searchField.getText().toLowerCase();
        String filter = filterComboBox.getValue();

        transactionsTable.getItems().clear();
        List<Transaction> transactions = transactionService
                .getTransaction(currentUser.getId());

        transactions.stream()
                .filter(t -> t.getAssetSymbol().toLowerCase().contains(search))
                .filter(t -> filter.equals("All Types") ||
                        t.getType().toString().equals(filter))
                .forEach(t -> transactionsTable.getItems().add(t));
    }

    /**
     * Shows a confirmation dialog before deleting the given transaction. If the user
     * confirms, the transaction is deleted and the table is refreshed.
     *
     * @param transaction the {@link Transaction} to delete
     */
    private void handleDelete(Transaction transaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Transaction");
        alert.setContentText("Are you sure you want to delete this transaction?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                transactionService.deleteTransaction(transaction.getId());
                loadTransactions();
            }
        });
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