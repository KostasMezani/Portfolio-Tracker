package com.portfoliotracker.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceSnapshot {
    private String assetSymbol;
    private BigDecimal currentPrice;
    private BigDecimal change24h;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;

    //Constructor
    public PriceSnapshot(String assetSymbol, BigDecimal currentPrice, BigDecimal change24h, BigDecimal totalAmount, LocalDateTime timestamp) {
        this.assetSymbol = assetSymbol;
        this.currentPrice = currentPrice;
        this.change24h = change24h;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
    }

    // Empty Constructor
    public PriceSnapshot(){}

    //Setters & Getters
    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getChange24h() {
        return change24h;
    }

    public void setChange24h(BigDecimal change24h) {
        this.change24h = change24h;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PriceSnapshot{" +
                "assetSymbol='" + assetSymbol + '\'' +
                ", currentPrice=" + currentPrice +
                ", change24h=" + change24h +
                ", totalAmount=" + totalAmount +
                ", timestamp=" + timestamp +
                "}";
    }
}
