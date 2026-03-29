package com.portfoliotracker.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.portfoliotracker.exception.ApiException;
import com.portfoliotracker.model.PriceSnapshot;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class MarketDataService{

    private HttpClient httpClient;
    private Map<String, PriceSnapshot> cache;

    public MarketDataService() {
        this.httpClient = HttpClient.newHttpClient();
        this.cache = new HashMap<>();
    }

    /**
     * Get the current price of assets
     * @param symbol the asset symbol e.g. "BTC"
     * @return price for each asset
     */
    public BigDecimal getCurrentPrice(String symbol){
        return getPriceSnapshot(symbol).getCurrentPrice();
    }

    /**
     * Calling API to get current price of Assets
     * @param symbol the asset symbol e.g. "BTC"
     * @return cached and snapshot
     */
    public PriceSnapshot getPriceSnapshot(String symbol){
        // Step 1: Checking cache
        if (cache.containsKey(symbol)){
            PriceSnapshot cached = cache.get(symbol);
            long age = System.currentTimeMillis() - cached.getTimestamp()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
            if (age < 30_000) {
                return cached;
            }
        }

        // Step 2: API call
        try {
            String coinId = symbol.toLowerCase();
            String url = "https://api.coingecko.com/api/v3/simple/price?ids="
                    + coinId + "&vs_currencies=eur&include_24hr_change=true";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Step 3: Parse JSON
            JsonObject json = JsonParser.parseString(response.body())
                    .getAsJsonObject()
                    .getAsJsonObject(coinId);

            BigDecimal price = json.get("eur").getAsBigDecimal();
            BigDecimal change24h = json.get("eur_24h_change").getAsBigDecimal();

            // Step 4: Create a snapshot and save a cache
            PriceSnapshot snapshot = new PriceSnapshot(
                    symbol, price, change24h, BigDecimal.ZERO, LocalDateTime.now()
            );
            cache.put(symbol, snapshot);
            return snapshot;
        } catch (Exception e) {
            throw new ApiException("Failed to fetch price for: " + symbol);
        }
    }

    /**
     * Refresh cache
     * @param symbol the asset symbol e.g. "BTC"
     */
    public void refreshCache(String symbol){
        cache.remove(symbol);
        getPriceSnapshot(symbol);
    }
}