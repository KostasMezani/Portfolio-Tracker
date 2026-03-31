package com.portfoliotracker.repository;

import com.portfoliotracker.model.Transaction;
import com.portfoliotracker.model.TransactionType;
import com.portfoliotracker.storage.file.TransactionFileStorage;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionRepository {

    private TransactionFileStorage transactionFileStorage;

    public TransactionRepository(TransactionFileStorage transactionFileStorage) {
        this.transactionFileStorage = transactionFileStorage;
    }

    public Transaction save(Transaction t) {
        List<Transaction> transactions = transactionFileStorage.loadAll();

        transactions.removeIf(existing -> existing.getId().equals(t.getId()));

        transactions.add(t);

        transactionFileStorage.saveAll(transactions);
        return t;
    }

    public Transaction findById(String id){
        List<Transaction> transaction = transactionFileStorage.loadAll();
        for (Transaction t : transaction) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        return null;
    }

    public List<Transaction> findByUserId(String userId){
        List<Transaction> transaction = transactionFileStorage.loadAll();
        return transaction.stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Transaction> findByUserAndType(String userId, TransactionType type){
        List<Transaction> transaction = transactionFileStorage.loadAll();
        return transaction.stream()
                .filter(t -> t.getUserId().equals(userId) && t.getType().equals(type))
                .collect(Collectors.toList());
    }

    public BigDecimal getOwnedQuantity(String userId, String symbol) {
        List<Transaction> transactions = transactionFileStorage.loadAll();
        BigDecimal quantity = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getUserId().equals(userId) && t.getAssetSymbol().equals(symbol)) {
                if (t.getType() == TransactionType.BUY) {
                    quantity = quantity.add(t.getQuantity());
                } else {
                    quantity = quantity.subtract(t.getQuantity());
                }
            }
        }
        return quantity;
    }

    public List<Transaction> findAll() {
        return transactionFileStorage.loadAll();
    }

    public void delete(String id) {
        List<Transaction> transaction = transactionFileStorage.loadAll();
        transaction.removeIf(t -> t.getId().equals(id));
        transactionFileStorage.saveAll(transaction);
    }
}