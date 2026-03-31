package com.portfoliotracker.app;

import com.portfoliotracker.controller.LoginController;
import com.portfoliotracker.repository.*;
import com.portfoliotracker.service.*;
import com.portfoliotracker.storage.file.*;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {

        // Storage layer
        UserFileStorage userFileStorage = new UserFileStorage();
        TransactionFileStorage transactionFileStorage = new TransactionFileStorage();
        PortfolioFileStorage portfolioFileStorage = new PortfolioFileStorage();
        WatchlistFileStorage watchlistFileStorage = new WatchlistFileStorage();
        AlertFileStorage alertFileStorage = new AlertFileStorage();

        // Repository layer
        UserRepository userRepository = new UserRepository(userFileStorage);
        TransactionRepository transactionRepository = new TransactionRepository(transactionFileStorage);
        PortfolioRepository portfolioRepository = new PortfolioRepository(portfolioFileStorage);
        WatchlistRepository watchlistRepository = new WatchlistRepository(watchlistFileStorage);
        AlertRepository alertRepository = new AlertRepository(alertFileStorage);

        // Service layer
        AuthService authService = new AuthService(userRepository);
        UserService userService = new UserService(userRepository);
        TransactionService transactionService = new TransactionService(transactionRepository);
        MarketDataService marketDataService = new MarketDataService();
        CalculationService calculationService = new CalculationService();
        PortfolioService portfolioService = new PortfolioService(
                transactionRepository, marketDataService, calculationService);
        WatchlistService watchlistService = new WatchlistService(watchlistRepository);
        AlertService alertService = new AlertService(alertRepository, marketDataService);

        // Start with Login screen
        LoginController loginController = new LoginController(
                primaryStage, authService, portfolioService,
                transactionService, marketDataService,
                watchlistService, alertService
        );
        primaryStage.setTitle("Portfolio Tracker");
        primaryStage.setScene(loginController.createScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}