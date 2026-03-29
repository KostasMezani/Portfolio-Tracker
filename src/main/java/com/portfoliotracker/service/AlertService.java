package com.portfoliotracker.service;

import com.portfoliotracker.model.AlertCondition;
import com.portfoliotracker.model.PriceAlert;
import com.portfoliotracker.repository.AlertRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlertService {

    private AlertRepository alertRepository;
    private MarketDataService marketDataService;

    public AlertService(AlertRepository alertRepository, MarketDataService marketDataService){
        this.alertRepository = alertRepository;
        this.marketDataService = marketDataService;
    }

    /**
     * Create an alert
     * @param userId the id of the user
     * @param assetSymbol asset's symbol
     * @param condition triggered or not triggered
     * @param targetPrice the price the user sets
     * @return the created price alert
     */
    public PriceAlert createAlert(String userId, String assetSymbol, AlertCondition condition, BigDecimal targetPrice){
        PriceAlert priceAlert = new PriceAlert(
                UUID.randomUUID().toString(),
                userId,
                assetSymbol,
                condition,
                targetPrice,
                false
        );
        alertRepository.save(priceAlert);
        return priceAlert;
    }

    /**
     * Delete the alert
     * @param alertId the id of the alert
     */
    public void deleteAlert(String alertId){
        alertRepository.deleteByAlertId(alertId);
    }

    /**
     * Get all alerts
     * @param userId the id of the user
     * @return the alert list
     */
    public List<PriceAlert> getAlerts(String userId){
        return alertRepository.findByUserId(userId);
    }

    /**
     * Checks all the alerts
     * @param userId the id of the user
     * @return triggered alerts
     */
    public List<PriceAlert> checkAlerts(String userId){
        List<PriceAlert> alerts = alertRepository.findByUserId(userId);
        List<PriceAlert> triggered = new ArrayList<>();

        for (PriceAlert alert: alerts){
            if (alert.isTriggered()) continue;

            BigDecimal currentPrice = marketDataService.getCurrentPrice(alert.getAssetSymbol());

            boolean shouldTrigger =
                    (alert.getCondition() == AlertCondition.ABOVE && currentPrice.compareTo(alert.getTargetPrice()) >= 0) ||
                            (alert.getCondition() == AlertCondition.BELOW && currentPrice.compareTo(alert.getTargetPrice()) <= 0);

            if (shouldTrigger) {
                alert.setTriggered(true);
                alertRepository.save(alert);
                triggered.add(alert);
            }
        }
        return triggered;
    }
}
