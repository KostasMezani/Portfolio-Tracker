package com.portfoliotracker.storage.file;

import com.google.gson.reflect.TypeToken;
import com.portfoliotracker.model.WatchlistItem;

import java.util.List;

public class WatchlistFileStorage extends JsonFileStorage<WatchlistItem> {
    public WatchlistFileStorage() {
        super("data/watchlist.json",
                new TypeToken<List<WatchlistItem>>(){}.getType());
    }
}
