package com.portfoliotracker.controller;

import com.portfoliotracker.model.Holding;
import com.portfoliotracker.model.User;
import com.portfoliotracker.service.AuthService;
import com.portfoliotracker.service.PortfolioService;
import com.portfoliotracker.util.CurrencyUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class DashboardController {

    private final Stage stage;
    private final User currentUser;
    private final PortfolioService portfolioService;
    private final AuthService authService;

    public DashboardController(Stage stage, User currentUser,
                               PortfolioService portfolioService,
                               AuthService authService) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.portfolioService = portfolioService;
        this.authService = authService;
    }

    /**
     * Creates and returns the Dashboard Scene
     * @return the dashboard scene
     */
    public Scene createScene() {
        // Main layout: Sidebar + Content
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createContent());
        return new Scene(mainLayout, 1100, 700);
    }

    /**
     * Creates the sidebar navigation
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

        // Navigation actions
        transactionsBtn.setOnAction(e -> navigateToTransactions());
        addTransactionBtn.setOnAction(e -> navigateToAddTransaction());
        holdingsBtn.setOnAction(e -> navigateToHoldings());
        watchlistBtn.setOnAction(e -> navigateToWatchlist());

        // Logout button at bottom
        Button logoutBtn = new Button("Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> handleLogout());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                appTitle,
                dashboardBtn,
                transactionsBtn,
                addTransactionBtn,
                holdingsBtn,
                watchlistBtn,
                spacer,
                logoutBtn
        );

        return sidebar;
    }

    /**
     * Creates a styled sidebar button
     */
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        return btn;
    }

    /**
     * Creates the main content area
     */
    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Welcome label
        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername());
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Summary cards
        HBox summaryCards = createSummaryCards();

        // Assets table
        Label assetsLabel = new Label("Your Assets");
        assetsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        TableView<Holding> assetsTable = createAssetsTable();

        content.getChildren().addAll(
                welcomeLabel,
                summaryCards,
                assetsLabel,
                assetsTable
        );

        return content;
    }

    /**
     * Creates the summary cards
     */
    private HBox createSummaryCards() {
        BigDecimal totalValue = portfolioService.getPortfolioValue(currentUser.getId());
        BigDecimal totalProfitLoss = portfolioService.getTotalProfitLoss(currentUser.getId());
        BigDecimal totalInvested = portfolioService.getTotalInvested(currentUser.getId());

        VBox valueCard = createCard("Total Portfolio Value",
                CurrencyUtils.formatCurrency(totalValue), "#ffffff");
        VBox plCard = createCard("Total Profit / Loss",
                CurrencyUtils.formatCurrency(totalProfitLoss),
                totalProfitLoss.compareTo(BigDecimal.ZERO) >= 0 ? "#00aa00" : "#cc0000");
        VBox investedCard = createCard("Total Invested",
                CurrencyUtils.formatCurrency(totalInvested), "#ffffff");

        HBox cards = new HBox(20);
        cards.getChildren().addAll(valueCard, plCard, investedCard);
        return cards;
    }

    /**
     * Creates a summary card
     */
    private VBox createCard(String title, String value, String valueColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 14));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        valueLabel.setStyle("-fx-text-fill: " + valueColor + ";");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    /**
     * Creates the assets TableView
     */
    private TableView<Holding> createAssetsTable() {
        TableView<Holding> table = new TableView<>();

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

        table.getColumns().addAll(symbolCol, quantityCol, avgPriceCol, currentPriceCol, plCol);

        // Load data
        List<Holding> holdings = portfolioService.getHoldings(currentUser.getId());
        table.getItems().addAll(holdings);

        return table;
    }

    private void navigateToTransactions() {
        // TODO
    }

    private void navigateToAddTransaction() {
        // TODO
    }

    private void navigateToHoldings() {
        // TODO
    }

    private void navigateToWatchlist() {
        // TODO
    }

    private void handleLogout() {
        LoginController loginController = new LoginController(stage, authService);
        stage.setScene(loginController.createScene());
    }
}