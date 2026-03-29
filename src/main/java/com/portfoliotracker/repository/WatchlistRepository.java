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

    public WatchlistItem save(WatchlistItem watchlistItem) {
        List<WatchlistItem> items = watchlistFileStorage.loadAll();

        items.removeIf(existing -> existing.getId().equals(watchlistItem.getId()));

        items.add(watchlistItem);

        watchlistFileStorage.saveAll(items);
        return watchlistItem;
    }

    public List<WatchlistItem> findByUserId(String userId) {
        List<WatchlistItem> items = watchlistFileStorage.loadAll();
        return items.stream()
                .filter(item -> item.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public void deleteByUserIdAndSymbol(String userId, String symbol) {
        List<WatchlistItem> items = watchlistFileStorage.loadAll();
        items.removeIf(item -> item.getUserId().equals(userId)
                                    && item.getAssetSymbol().equals(symbol));
        watchlistFileStorage.saveAll(items);
    }

    public List<WatchlistItem> findAll() {
        return watchlistFileStorage.loadAll();
    }
}
