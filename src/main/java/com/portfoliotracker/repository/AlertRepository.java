package com.portfoliotracker.repository;

import com.portfoliotracker.model.PriceAlert;
import com.portfoliotracker.storage.file.AlertFileStorage;

import java.util.List;
import java.util.stream.Collectors;

public class AlertRepository {

    private AlertFileStorage alertFileStorage;

    public AlertRepository(AlertFileStorage alertFileStorage) {
        this.alertFileStorage = alertFileStorage;
    }

    /**
     * Save a price alert
     * @param alert the price alert to save
     * @return the saved price alert
     */
    public PriceAlert save(PriceAlert alert) {
        List<PriceAlert> priceAlert = alertFileStorage.loadAll();

        priceAlert.removeIf(existing -> existing.getId().equals(alert.getId()));

        priceAlert.add(alert);

        alertFileStorage.saveAll(priceAlert);
        return alert;
    }

    /**
     * Find all price alerts for a user
     * @param userId the id of the user
     * @return the list of price alerts
     */
    public List<PriceAlert> findByUserId(String userId) {
        List<PriceAlert> priceAlerts = alertFileStorage.loadAll();

        return priceAlerts.stream()
                .filter(alert -> alert.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Delete a price alert by id
     * @param alertId the id of the alert
     */
    public void deleteByAlertId(String alertId) {
        List<PriceAlert> priceAlerts = alertFileStorage.loadAll();

        priceAlerts.removeIf(alerts -> alerts.getId().equals(alertId));

        alertFileStorage.saveAll(priceAlerts);
    }

    /**
     * Find all price alerts
     * @return the list of price alerts
     */
    public List<PriceAlert> findAll() {
        return alertFileStorage.loadAll();
    }
}
