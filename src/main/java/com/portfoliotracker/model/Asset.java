package com.portfoliotracker.model;

public class Asset {
    private String symbol;
    private String name;
    private AssetType type;

    public Asset(String symbol, String name, AssetType type) {
        this.symbol = symbol;
        this.name = name;
        this.type = type;
    }

    public Asset() {}

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public AssetType getType() {
        return type;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(AssetType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type + '}';
    }
}
