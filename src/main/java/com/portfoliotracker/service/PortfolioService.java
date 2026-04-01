package com.portfoliotracker.service;

import com.portfoliotracker.model.Holding;
import com.portfoliotracker.model.Transaction;
import com.portfoliotracker.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortfolioService {

    private TransactionRepository transactionRepository;
    private MarketDataService marketDataService;
    private CalculationService calculationService;

    public PortfolioService(TransactionRepository transactionRepository,
                            MarketDataService marketDataService,
                            CalculationService calculationService) {
        this.transactionRepository = transactionRepository;
        this.marketDataService = marketDataService;
        this.calculationService = calculationService;
    }

    /**
     * Get all assets,grouped by symbol,
     * create a Holding for every asset and save it to a Map (key - value)
     * calculate quantity, average price and get current price
     * @param userId the id of the user
     * @return holdings
     */
    public List<Holding> getHoldings(String userId){
        // We get all transactions
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        // Group asset by symbol
        Map<String, List<Transaction>> grouped = new HashMap<>();
        for (Transaction t : transactions) {
            grouped.computeIfAbsent(t.getAssetSymbol(), k -> new ArrayList<>()).add(t);
        }

        // For every asset we create a Holding
        List<Holding> holdings = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {
            String symbol = entry.getKey();
            List<Transaction> assetTransaction = entry.getValue();

            // Calculate quantity and avgPrice
            Holding holding = calculationService.calculateHolding(symbol, assetTransaction);

            // Get current price
            try {
                BigDecimal currentPrice = marketDataService.getCurrentPrice(symbol);
                holding.setCurrentPrice(currentPrice);
            } catch (Exception e) {
                // Αν το API αποτύχει, βάλε τιμή 0
                holding.setCurrentPrice(BigDecimal.ZERO);
            }

            if (holding.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                holdings.add(holding);
            }
        }
        return holdings;
    }

    /**
     * Calculate portfolio value
     * @param userId the id of the user
     * @return portfolios value
     */
    public BigDecimal getPortfolioValue(String userId){
        return calculationService.calculatePortfolioValue(getHoldings(userId));
    }

    /**
     * Calculate total invested
     * @param userId the id of the user
     * @return the total invested
     */
    public BigDecimal getTotalInvested(String userId){
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return calculationService.calculateTotalInvested(transactions);
    }

    /**
     * Calculate the profit and loss of the portfolio
     * @param userId the id of the user
     * @return profit and loss
     */
    public BigDecimal getTotalProfitLoss(String userId){
       return calculationService.calculateTotalProfitLoss(getHoldings(userId));
    }
}