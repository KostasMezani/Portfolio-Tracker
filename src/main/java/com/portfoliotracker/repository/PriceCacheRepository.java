package com.portfoliotracker.repository;

import com.portfoliotracker.model.PriceSnapshot;
import com.portfoliotracker.storage.file.JsonFileStorage;

import java.util.List;

public class PriceCacheRepository {

    private JsonFileStorage jsonFileStorage;

    public PriceCacheRepository(JsonFileStorage jsonFileStorage) {
        this.jsonFileStorage = jsonFileStorage;
    }

    /**
     * Save a price snapshot
     * @param snapshot the price snapshot to save
     * @return the saved price snapshot
     */
    public PriceSnapshot save(PriceSnapshot snapshot) {
        List<PriceSnapshot> snapshots = jsonFileStorage.loadAll();
        snapshots.removeIf(existing -> existing.getAssetSymbol().equals(snapshot.getAssetSymbol()));
        snapshots.add(snapshot);
        jsonFileStorage.saveAll(snapshots);
        return snapshot;
    }

    /**
     * Find a price snapshot by symbol
     * @param symbol the symbol of the asset
     * @return the price snapshot
     */
    public PriceSnapshot findBySymbol(String symbol) {
        List<PriceSnapshot> snapshots = jsonFileStorage.loadAll();
        return snapshots.stream()
                .filter(s -> s.getAssetSymbol().equals(symbol))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find all price snapshots
     * @return the list of price snapshots
     */
    public List<PriceSnapshot> findAll() {
        return jsonFileStorage.loadAll();
    }

    /**
     * Delete a price snapshot by symbol
     * @param symbol the symbol of the asset
     */
    public void delete(String symbol) {
        List<PriceSnapshot> snapshots = jsonFileStorage.loadAll();
        snapshots.removeIf(s -> s.getAssetSymbol().equals(symbol));
        jsonFileStorage.saveAll(snapshots);
    }
}