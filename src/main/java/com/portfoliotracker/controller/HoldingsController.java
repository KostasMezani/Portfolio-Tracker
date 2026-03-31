package com.portfoliotracker.controller;

import com.portfoliotracker.model.Holding;
import com.portfoliotracker.model.User;
import com.portfoliotracker.service.AuthService;
import com.portfoliotracker.service.PortfolioService;
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

    public HoldingsController(Stage stage, User currentUser,
                              PortfolioService portfolioService,
                              AuthService authService) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.portfolioService = portfolioService;
        this.authService = authService;
    }

    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createContent());
        return new Scene(mainLayout, 1100, 700);
    }

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
        plCol.setCellValueFactory(data -> {
            String value = CurrencyUtils.formatCurrency(data.getValue().getProfitLoss());
            return new javafx.beans.property.SimpleStringProperty(value);
        });

        table.getColumns().addAll(
                assetCol, quantityCol, avgPriceCol, currentPriceCol, plCol
        );

        List<Holding> holdings = portfolioService.getHoldings(currentUser.getId());
        table.getItems().addAll(holdings);

        return table;
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

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        return btn;
    }

    private void handleLogout() {
        LoginController loginController = new LoginController(stage, authService);
        stage.setScene(loginController.createScene());
    }
}