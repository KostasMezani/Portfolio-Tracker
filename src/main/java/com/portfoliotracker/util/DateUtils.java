package com.portfoliotracker.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    /**
     * Formats a LocalDateTime to a readable date and time string
     * @param dateTime the date and time to format
     * @return formatted string e.g. "15 Jul 2025, 14:30"
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Formats a LocalDateTime to a readable date and time string
     * @param dateTime the date and time to format
     * @return formatted string e.g. "15 Jul 2025"
     */
    public static String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return dateTime.format(formatter);
    }
}
