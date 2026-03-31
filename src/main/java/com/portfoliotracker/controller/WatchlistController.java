package com.portfoliotracker.controller;

import com.portfoliotracker.model.PriceSnapshot;
import com.portfoliotracker.model.User;
import com.portfoliotracker.model.WatchlistItem;
import com.portfoliotracker.service.AuthService;
import com.portfoliotracker.service.MarketDataService;
import com.portfoliotracker.service.WatchlistService;
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

    private TableView<WatchlistItem> watchlistTable;

    public WatchlistController(Stage stage, User currentUser,
                               WatchlistService watchlistService,
                               MarketDataService marketDataService,
                               AuthService authService) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.watchlistService = watchlistService;
        this.marketDataService = marketDataService;
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

        Label titleLabel = new Label("Watchlist");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label subtitleLabel = new Label("Track assets you are interested in");
        subtitleLabel.setTextFill(Color.GRAY);

        // Add to watchlist
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

        // Remove button
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

    private void loadWatchlist() {
        watchlistTable.getItems().clear();
        List<WatchlistItem> items = watchlistService.getWatchlist(currentUser.getId());
        watchlistTable.getItems().addAll(items);
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