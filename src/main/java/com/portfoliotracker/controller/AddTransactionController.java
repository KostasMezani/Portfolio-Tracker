package com.portfoliotracker.controller;

import com.portfoliotracker.exception.InsufficientHoldingsException;
import com.portfoliotracker.exception.ValidationException;
import com.portfoliotracker.model.User;
import com.portfoliotracker.service.AuthService;
import com.portfoliotracker.service.MarketDataService;
import com.portfoliotracker.service.TransactionService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class AddTransactionController {

    private final Stage stage;
    private final User currentUser;
    private final TransactionService transactionService;
    private final MarketDataService marketDataService;
    private final AuthService authService;

    private ToggleGroup typeToggle;
    private ToggleGroup assetTypeToggle;
    private ComboBox<String> assetComboBox;
    private TextField quantityField;
    private TextField priceField;
    private Label totalAmountLabel;
    private Label errorLabel;
    private Label livePriceLabel;

    public AddTransactionController(Stage stage, User currentUser,
                                    TransactionService transactionService,
                                    MarketDataService marketDataService,
                                    AuthService authService) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.transactionService = transactionService;
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
        content.setPadding(new Insets(30));
        content.setMaxWidth(600);
        content.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("Add Transaction");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // BUY / SELL toggle
        RadioButton buyBtn = new RadioButton("BUY");
        RadioButton sellBtn = new RadioButton("SELL");
        typeToggle = new ToggleGroup();
        buyBtn.setToggleGroup(typeToggle);
        sellBtn.setToggleGroup(typeToggle);
        buyBtn.setSelected(true);
        HBox typeBox = new HBox(10, buyBtn, sellBtn);

        // Asset type toggle
        RadioButton cryptoBtn = new RadioButton("Crypto");
        RadioButton stockBtn = new RadioButton("Stock");
        assetTypeToggle = new ToggleGroup();
        cryptoBtn.setToggleGroup(assetTypeToggle);
        stockBtn.setToggleGroup(assetTypeToggle);
        cryptoBtn.setSelected(true);
        HBox assetTypeBox = new HBox(10, cryptoBtn, stockBtn);

        // Asset selector
        assetComboBox = new ComboBox<>();
        assetComboBox.getItems().addAll("bitcoin", "ethereum", "solana");
        assetComboBox.setPromptText("Select asset");
        assetComboBox.setOnAction(e -> fetchLivePrice());

        // Live price label
        livePriceLabel = new Label("Select an asset to see live price");
        livePriceLabel.setTextFill(Color.GRAY);

        // Quantity field
        quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");
        quantityField.setOnKeyTyped(e -> updateTotalAmount());

        // Price field
        priceField = new TextField();
        priceField.setPromptText("Price per unit");
        priceField.setEditable(false);

        // Total amount
        totalAmountLabel = new Label("Total Amount: €0.00");
        totalAmountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Error label
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Buttons
        Button saveBtn = new Button("Save Transaction");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> handleSave());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> navigateToTransactions());

        content.getChildren().addAll(
                titleLabel,
                new Label("Transaction Type:"), typeBox,
                new Label("Asset Type:"), assetTypeBox,
                new Label("Asset:"), assetComboBox,
                livePriceLabel,
                new Label("Quantity:"), quantityField,
                new Label("Price per Unit:"), priceField,
                totalAmountLabel,
                errorLabel,
                saveBtn, cancelBtn
        );

        return content;
    }

    private void fetchLivePrice() {
        String symbol = assetComboBox.getValue();
        if (symbol == null) return;

        try {
            BigDecimal price = marketDataService.getCurrentPrice(symbol);
            priceField.setText(price.toString());
            livePriceLabel.setText("Live price: €" + price + " ✓");
            livePriceLabel.setTextFill(Color.GREEN);
            updateTotalAmount();
        } catch (Exception e) {
            livePriceLabel.setText("Could not fetch live price");
            livePriceLabel.setTextFill(Color.RED);
        }
    }

    private void updateTotalAmount() {
        try {
            BigDecimal quantity = new BigDecimal(quantityField.getText());
            BigDecimal price = new BigDecimal(priceField.getText());
            BigDecimal total = quantity.multiply(price);
            totalAmountLabel.setText("Total Amount: €" + total);
        } catch (Exception e) {
            totalAmountLabel.setText("Total Amount: €0.00");
        }
    }

    private void handleSave() {
        String symbol = assetComboBox.getValue();
        String quantityText = quantityField.getText().trim();
        String priceText = priceField.getText().trim();

        if (symbol == null || quantityText.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        try {
            BigDecimal quantity = new BigDecimal(quantityText);
            BigDecimal price = new BigDecimal(priceText);

            RadioButton selectedType = (RadioButton) typeToggle.getSelectedToggle();
            boolean isBuy = selectedType.getText().equals("BUY");

            if (isBuy) {
                transactionService.addBuyTransaction(
                        currentUser.getId(), symbol, quantity, price);
            } else {
                transactionService.addSellTransaction(
                        currentUser.getId(), symbol, quantity, price);
            }

            navigateToTransactions();

        } catch (InsufficientHoldingsException e) {
            showError("Not enough assets to sell!");
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void navigateToTransactions() { /* TODO */ }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #1a2942;");

        Label appTitle = new Label("Portfolio Tracker");
        appTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        appTitle.setTextFill(Color.WHITE);

        sidebar.getChildren().add(appTitle);
        return sidebar;
    }
}