package com.portfoliotracker.service;

import com.portfoliotracker.model.WatchlistItem;
import com.portfoliotracker.repository.WatchlistRepository;

import java.util.List;
import java.util.UUID;

public class WatchlistService {

    private WatchlistRepository watchlistRepository;

    public WatchlistService(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    /**
     * Adds an asset to the user's watchlist
     * @param userId the id of the user
     * @param assetSymbol the symbol of the asset e.g. "BTC"
     * @return the created WatchlistItem
     */
    public WatchlistItem addToWatchlist(String userId, String assetSymbol){

        WatchlistItem watchlistItem = new WatchlistItem(
                UUID.randomUUID().toString(),
                userId,
                assetSymbol
        );
        watchlistRepository.save(watchlistItem);
        return watchlistItem;
    }

    /**
     * Removes an asset from the user's watchlist
     * @param userId the id of the user
     * @param assetSymbol the symbol of the asset e.g. "BTC"
     */
    public void removeFromWatchlist(String userId, String assetSymbol){
        watchlistRepository.deleteByUserIdAndSymbol(userId, assetSymbol);
    }

    /**
     * Brings the list with the assets
     * that the user has on watch list
     * @param userId the id of the user
     * @return the watch list
     */
    public List<WatchlistItem> getWatchlist(String userId){
        return watchlistRepository.findByUserId(userId);
    }
}
