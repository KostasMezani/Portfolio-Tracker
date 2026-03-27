package com.portfoliotracker.util;

import java.math.BigDecimal;

public class CurrencyUtils {

    /**
     * Format a BigDecimal amount to a currency string
     * @param amount the amount to format
     * @return formatted string e.g. "€15,200.50"
     */
    public static String formatCurrency(BigDecimal amount){
        return String.format("€%,.2f", amount);
    }
}