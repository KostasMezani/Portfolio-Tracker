package com.portfoliotracker.controller;

import com.portfoliotracker.model.Transaction;
import com.portfoliotracker.model.TransactionType;
import com.portfoliotracker.model.User;
import com.portfoliotracker.service.AuthService;
import com.portfoliotracker.service.TransactionService;
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

    private TextField searchField;
    private ComboBox<String> filterComboBox;
    private TableView<Transaction> transactionsTable;

    public TransactionsController(Stage stage, User currentUser,
                                  TransactionService transactionService,
                                  AuthService authService) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.transactionService = transactionService;
        this.authService = authService;
    }

    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createContent());
        return new Scene(mainLayout, 1100, 700);
    }

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

    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Transactions");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Search and filter bar
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

        // Table
        transactionsTable = createTransactionsTable();
        loadTransactions();

        content.getChildren().addAll(titleLabel, toolbar, transactionsTable);
        return content;
    }

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

        // Delete button column
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

    private void loadTransactions() {
        transactionsTable.getItems().clear();
        List<Transaction> transactions = transactionService
                .getTransaction(currentUser.getId());
        transactionsTable.getItems().addAll(transactions);
    }

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

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        return btn;
    }

    private void navigateToDashboard() { /* TODO */ }
    private void navigateToAddTransaction() { /* TODO */ }
    private void navigateToHoldings() { /* TODO */ }
    private void navigateToWatchlist() { /* TODO */ }
    private void handleLogout() {
        LoginController loginController = new LoginController(stage, authService);
        stage.setScene(loginController.createScene());
    }
}