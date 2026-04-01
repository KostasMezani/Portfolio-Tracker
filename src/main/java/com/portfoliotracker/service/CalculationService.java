package com.portfoliotracker.service;

import com.portfoliotracker.model.Asset;
import com.portfoliotracker.model.Holding;
import com.portfoliotracker.model.Transaction;
import com.portfoliotracker.model.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalculationService {

    public CalculationService() {}

    /**
     * Calculate the holding of all holdings
     * @param symbol of the asset
     * @param transactions the list of transactions
     * @return asset, total quantity, average price
     */
    public Holding calculateHolding(String symbol, List<Transaction> transactions) {
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalBuyAmount = BigDecimal.ZERO;
        BigDecimal totalBuyQuantity = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.BUY) {
                totalQuantity = totalQuantity.add(t.getQuantity());
                totalBuyAmount = totalBuyAmount.add(t.getTotalAmount());
                totalBuyQuantity = totalBuyQuantity.add(t.getQuantity());
            } else {
                totalQuantity = totalQuantity.subtract(t.getQuantity());
            }
        }

        BigDecimal avgPrice = totalBuyQuantity.compareTo(BigDecimal.ZERO) >0
                ? totalBuyAmount.divide(totalBuyQuantity, 8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Asset asset = new Asset(symbol, symbol, null);
        return new Holding(asset, totalQuantity, avgPrice, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Calculate the total value of the portfolio
     * @param holdings the list of holdings
     * @return total
     */
    public BigDecimal calculatePortfolioValue(List<Holding> holdings){
        BigDecimal total = BigDecimal.ZERO;

        for (Holding h : holdings) {
            total = total.add(h.getQuantity().multiply(h.getCurrentPrice()));
        }
        return total;
    }

    /**
     * Calculate the total amount
     * spent on Investments
     * @param transactions the list of transactions
     * @return total
     */
    public BigDecimal calculateTotalInvested(List<Transaction> transactions){
        BigDecimal total = BigDecimal.ZERO;

        for (Transaction t : transactions){
            if (t.getType() == TransactionType.BUY){
                total = total.add(t.getTotalAmount());
            }
        }
        return total;
    }

    /**
     * Calculate the profit loss
     * of the user
     * @param holdings the list of holdings
     * @return totalProfitLoss
     */
    public BigDecimal calculateTotalProfitLoss(List<Holding> holdings){
        BigDecimal totalProfitLoss = BigDecimal.ZERO;

        for (Holding h : holdings) {
            totalProfitLoss = totalProfitLoss.add(h.getProfitLoss());
        }
        return totalProfitLoss;
    }
}
