package com.portfoliotracker.repository;

import com.portfoliotracker.model.Portfolio;
import com.portfoliotracker.storage.file.PortfolioFileStorage;

import java.util.List;
import java.util.stream.Collectors;

public class PortfolioRepository {

    private PortfolioFileStorage portfolioFileStorage;

    public PortfolioRepository(PortfolioFileStorage portfolioFileStorage) {
        this.portfolioFileStorage = portfolioFileStorage;
    }

    /**
     * Save a portfolio
     * @param portfolio the portfolio to save
     * @return the saved portfolio
     */
    public Portfolio save(Portfolio portfolio) {
        List<Portfolio> portfolios = portfolioFileStorage.loadAll();

        portfolios.removeIf(existing -> existing.getId().equals(portfolio.getId()));

        portfolios.add(portfolio);

        portfolioFileStorage.saveAll(portfolios);
        return portfolio;
    }

    /**
     * Find all portfolios for a user
     * @param userId the id of the user
     * @return the list of portfolios
     */
    public List<Portfolio> findByUserId(String userId){
        List<Portfolio> portfolios = portfolioFileStorage.loadAll();

        return portfolios.stream()
                .filter(portfolio -> portfolio.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Find all portfolios
     * @return the list of portfolios
     */
    public List<Portfolio> findAll(){
        return portfolioFileStorage.loadAll();
    }

    /**
     * Delete a portfolio
     * @param id the id of the portfolio to delete
     */
    public void delete(String id) {
        List<Portfolio> portfolios = portfolioFileStorage.loadAll();
        portfolios.removeIf(portfolio -> portfolio.getId().equals(id));
        portfolioFileStorage.saveAll(portfolios);
    }
}