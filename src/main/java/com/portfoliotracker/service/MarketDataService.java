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

public class MarketDataService {

    private HttpClient httpClient;
    private Map<String, PriceSnapshot> cache;

    private static final long CACHE_DURATION_MS = 60_000; // 60 seconds
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds between retries

    public MarketDataService() {
        this.httpClient = HttpClient.newHttpClient();
        this.cache = new HashMap<>();
    }

    /**
     * Get the current price of assets
     * @param symbol the asset symbol e.g. "bitcoin"
     * @return price for each asset
     */
    public BigDecimal getCurrentPrice(String symbol) {
        return getPriceSnapshot(symbol).getCurrentPrice();
    }

    /**
     * Calling API to get current price of Assets with retry mechanism
     * @param symbol the asset symbol e.g. "bitcoin"
     * @return cached snapshot or fresh from API
     */
    public PriceSnapshot getPriceSnapshot(String symbol) {
        // Step 1: Check cache
        if (cache.containsKey(symbol)) {
            PriceSnapshot cached = cache.get(symbol);
            long age = System.currentTimeMillis() - cached.getTimestamp()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
            if (age < CACHE_DURATION_MS) {
                return cached;
            }
        }

        // Step 2: API call with retries
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                PriceSnapshot snapshot = fetchFromApi(symbol);
                cache.put(symbol, snapshot);
                return snapshot;
            } catch (Exception e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    try {
                        // Wait before retrying
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // Step 3: If all retries failed, return cached value if available
        if (cache.containsKey(symbol)) {
            return cache.get(symbol);
        }

        throw new ApiException("Failed to fetch price for: " + symbol +
                " after " + MAX_RETRIES + " attempts");
    }

    /**
     * Makes the actual HTTP request to CoinGecko API
     * @param symbol the asset symbol
     * @return PriceSnapshot with current price data
     */
    private PriceSnapshot fetchFromApi(String symbol) throws Exception {
        String coinId = symbol.toLowerCase();
        String url = "https://api.coingecko.com/api/v3/simple/price?ids="
                + coinId + "&vs_currencies=eur&include_24hr_change=true";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        // Check HTTP status
        if (response.statusCode() == 429) {
            throw new Exception("Rate limit exceeded - will retry");
        }
        if (response.statusCode() != 200) {
            throw new Exception("API returned status: " + response.statusCode());
        }

        // Check if response body is empty or invalid
        String body = response.body();
        if (body == null || body.isEmpty() || body.equals("{}")) {
            throw new Exception("Empty response from API");
        }

        // Parse JSON
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!root.has(coinId)) {
            throw new Exception("Symbol not found in API response: " + coinId);
        }

        JsonObject json = root.getAsJsonObject(coinId);
        BigDecimal price = json.get("eur").getAsBigDecimal();
        BigDecimal change24h = json.has("eur_24h_change")
                ? json.get("eur_24h_change").getAsBigDecimal()
                : BigDecimal.ZERO;

        return new PriceSnapshot(symbol, price, change24h, BigDecimal.ZERO, LocalDateTime.now());
    }

    /**
     * Refresh cache for a specific symbol
     * @param symbol the asset symbol e.g. "bitcoin"
     */
    public void refreshCache(String symbol) {
        cache.remove(symbol);
        getPriceSnapshot(symbol);
    }
}