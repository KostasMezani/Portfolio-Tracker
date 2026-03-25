package com.portfoliotracker.model;

import java.math.BigDecimal;

public class PriceAlert {
    private String id;
    private String userId;
    private String assetSymbol;
    private AlertCondition condition;
    private BigDecimal targetPrice;
    private boolean isTriggered;

    public PriceAlert(String id, String userId, String assetSymbol, AlertCondition condition, BigDecimal targetPrice, boolean isTriggered) {
        this.id = id;
        this.userId = userId;
        this.assetSymbol = assetSymbol;
        this.condition = condition;
        this.targetPrice = targetPrice;
        this.isTriggered = isTriggered;
    }

    public PriceAlert() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public AlertCondition getCondition() {
        return condition;
    }

    public void setCondition(AlertCondition condition) {
        this.condition = condition;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    public boolean isTriggered() {
        return isTriggered;
    }

    public void setTriggered(boolean triggered) {
        isTriggered = triggered;
    }

    @Override
    public String toString() {
        return "PriceAlert{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", assetSymbol='" + assetSymbol + '\'' +
                ", condition=" + condition +
                ", targetPrice=" + targetPrice +
                ", isTriggered=" + isTriggered +
                "}";
    }
}
