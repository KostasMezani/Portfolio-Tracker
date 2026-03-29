package com.portfoliotracker.service;

import com.portfoliotracker.exception.InsufficientHoldingsException;
import com.portfoliotracker.model.Transaction;
import com.portfoliotracker.model.TransactionType;
import com.portfoliotracker.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionService {

    private TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Adds a buy transaction for a user
     * @param userId the id of the user
     * @param assetSymbol the symbol of the asset e.g. "BTC"
     * @param quantity the quantity to buy
     * @param priceUnit the price per unit
     * @return the created Transaction
     */
    public Transaction addBuyTransaction(String userId, String assetSymbol, BigDecimal quantity, BigDecimal priceUnit) {
        BigDecimal totalAmount = quantity.multiply(priceUnit);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                userId,
                assetSymbol,
                TransactionType.BUY,
                quantity,
                priceUnit,
                totalAmount,
                LocalDateTime.now()
        );
        transactionRepository.save(transaction);
        return transaction;
    }

    /**
     * Adds a sell transaction for a user
     * @param userId the id of the user
     * @param assetSymbol the symbol of the asset e.g. "BTC"
     * @param quantity the quantity to sell
     * @param priceUnit the price per unit
     * @return the created Transaction
     */
    public Transaction addSellTransaction(String userId, String assetSymbol, BigDecimal quantity, BigDecimal priceUnit) {
        BigDecimal totalAmount = quantity.multiply(priceUnit);
        BigDecimal ownedQuantity = transactionRepository.getOwnedQuantity(userId, assetSymbol);

        if (ownedQuantity.compareTo(quantity) < 0) {
            throw new InsufficientHoldingsException("Not enough quantity to sell");
        }

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                userId,
                assetSymbol,
                TransactionType.SELL,
                quantity,
                priceUnit,
                totalAmount,
                LocalDateTime.now()
        );

        transactionRepository.save(transaction);
        return transaction;
    }

    /**
     * Returns all transactions for a specific user
     * @param userId the id of the user
     * @return the list of Transactions
     * filtered by Users id
     */
    public List<Transaction> getTransaction(String userId) {
        return transactionRepository.findByUserId(userId);
    }

    /**
     * Returns all transactions for a specific user and type
     * @param userId the id of the user
     * @param type the type of the asset
     * @return the list of Transactions
     * filtered by Users id and type
     */
    public List<Transaction> filterTransactions(String userId, TransactionType type) {
        return transactionRepository.findByUserAndType(userId, type);
    }
}