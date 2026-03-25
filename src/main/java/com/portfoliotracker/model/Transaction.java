package com.portfoliotracker.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private String id;
    private String userId;
    private String assetSymbol;
    private TransactionType type;
    private BigDecimal quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalAmount;
    private LocalDateTime date;

    public Transaction(String id, String userId, String assetSymbol, TransactionType type, BigDecimal quantity, BigDecimal pricePerUnit, BigDecimal totalAmount, LocalDateTime date) {
        this.id = id;
        this.userId = userId;
        this.assetSymbol = assetSymbol;
        this.type = type;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalAmount = totalAmount;
        this.date = date;
    }

    public Transaction() {}

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", assetSymbol='" + assetSymbol + '\''+
                ", type='" + type + '\'' +
                ", quantity=" + quantity +
                ", pricePerUnit=" + pricePerUnit +
                ", totalAmount=" + totalAmount +
                ", date=" + date + '}';

    }
}