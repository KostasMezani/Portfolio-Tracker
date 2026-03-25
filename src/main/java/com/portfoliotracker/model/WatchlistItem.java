package com.portfoliotracker.model;

public class WatchlistItem {
    private String id;
    private String userId;
    private String assetSymbol;

    public WatchlistItem(String id, String userId, String assetSymbol) {
        this.id = id;
        this.userId = userId;
        this.assetSymbol = assetSymbol;
    }

    public WatchlistItem() {}

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

    @Override
    public String toString() {
        return "WatchlistItem{" +
                "id='" + id +'\'' +
                ", userId='" + userId + '\'' +
                ", assetSymbol='" + assetSymbol + '\'' +
                "}";
    }
}
