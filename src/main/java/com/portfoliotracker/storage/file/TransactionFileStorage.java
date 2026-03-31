package com.portfoliotracker.storage.file;

import com.google.gson.reflect.TypeToken;
import com.portfoliotracker.model.Transaction;

import java.util.List;

public class TransactionFileStorage extends JsonFileStorage<Transaction> {
    public TransactionFileStorage() {
        super("data/transactions.json",
                new TypeToken<List<Transaction>>(){}.getType());
    }
}
