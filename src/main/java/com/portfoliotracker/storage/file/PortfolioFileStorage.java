package com.portfoliotracker.storage.file;

import com.google.gson.reflect.TypeToken;
import com.portfoliotracker.model.Portfolio;

import java.util.List;

public class PortfolioFileStorage extends JsonFileStorage<Portfolio>{
    public PortfolioFileStorage() {
        super("data/portfolios.json",
                new TypeToken<List<Portfolio>>(){}.getType());
    }
}
