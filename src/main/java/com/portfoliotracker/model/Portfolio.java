package com.portfoliotracker.model;

public class Portfolio {
    private String id;
    private String userId;
    private String name;
    private String baseCurrency;

    //Constructor

    public Portfolio(String id, String userId, String name, String baseCurrency) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.baseCurrency = baseCurrency;
    }

    public Portfolio() {}

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", baseCurrency='" + baseCurrency + "}";
    }
}
