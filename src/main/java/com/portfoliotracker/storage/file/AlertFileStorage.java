package com.portfoliotracker.storage.file;

import com.google.gson.reflect.TypeToken;
import com.portfoliotracker.model.PriceAlert;

import java.util.List;

public class AlertFileStorage extends JsonFileStorage<PriceAlert>{
    public AlertFileStorage() {
        super("data/alerts.json",
                new TypeToken<List<PriceAlert>>(){}.getType());
    }
}
