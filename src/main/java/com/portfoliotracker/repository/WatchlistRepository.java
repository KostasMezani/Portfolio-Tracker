package com.portfoliotracker.repository;

import com.portfoliotracker.model.WatchlistItem;
import com.portfoliotracker.storage.file.WatchlistFileStorage;

import java.util.List;
import java.util.stream.Collectors;

public class WatchlistRepository {

    private WatchlistFileStorage watchlistFileStorage;

    public WatchlistRepository(WatchlistFileStorage watchlistFileStorage) {
        this.watchlistFileStorage = watchlistFileStorage;
    }

    /**
     * Saves a watchlist item
     * @param watchlistItem the watchlist item to save
     * @return the saved watchlist item
     */
    public WatchlistItem save(WatchlistItem watchlistItem) {
        List<WatchlistItem> items = watchlistFileStorage.loadAll();

        items.removeIf(existing -> existing.getId().equals(watchlistItem.getId()));

        items.add(watchlistItem);

        watchlistFileStorage.saveAll(items);
        return watchlistItem;
    }

    /**
     * Finds all watchlist items for a user
     * @param userId the id of the user
     * @return the list of watchlist items
     */
    public List<WatchlistItem> findByUserId(String userId) {
        List<WatchlistItem> items = watchlistFileStorage.loadAll();
        return items.stream()
                .filter(item -> item.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Deletes a watchlist item by user id and symbol
     * @param userId the id of the user
     * @param symbol the symbol of the asset
     */
    public void deleteByUserIdAndSymbol(String userId, String symbol) {
        List<WatchlistItem> items = watchlistFileStorage.loadAll();
        items.removeIf(item -> item.getUserId().equals(userId)
                                    && item.getAssetSymbol().equals(symbol));
        watchlistFileStorage.saveAll(items);
    }

    /**
     * Finds all watchlist items
     * @return the list of watchlist items
     */
    public List<WatchlistItem> findAll() {
        return watchlistFileStorage.loadAll();
    }
}
