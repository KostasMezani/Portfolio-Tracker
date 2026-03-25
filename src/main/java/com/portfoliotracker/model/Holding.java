package com.portfoliotracker.model;

import java.math.BigDecimal;

public class Holding {
    private Asset asset;
    private BigDecimal quantity;
    private BigDecimal avgPrice;
    private BigDecimal currentPrice;
    private BigDecimal profitLoss;

    public Holding(Asset asset, BigDecimal quantity, BigDecimal avgPrice, BigDecimal currentPrice, BigDecimal profitLoss) {
        this.asset = asset;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.currentPrice = currentPrice;
        this.profitLoss = profitLoss;
    }

    public Holding() {}

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }

    @Override
    public String toString() {
        return "Holding{" +
                "asset=" + asset +
                ", quantity=" + quantity +
                ", avgPrice=" + avgPrice +
                ", currentPrice=" + currentPrice +
                ", profitLoss=" + profitLoss +
                "}";
    }
}
