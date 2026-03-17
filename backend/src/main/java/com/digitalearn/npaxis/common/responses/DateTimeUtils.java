package com.digitalearn.npaxis.common.responses;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for handling date and time formatting.
 * <p>
 * This class provides methods to format LocalDateTime objects into specific string formats
 * used in API responses and HL7 messages.
 * </p>
 */
public final class DateTimeUtils {

    /**
     * Date format used for API responses.
     */
    private static final String API_RESPONSE_FORMAT = "yyyy-MM-dd HH:mm";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DateTimeUtils() {
        // Prevent instantiation
    }

    /**
     * Date format used for HL7 messages with timezone.
     *
     * @param localDateTime the LocalDateTime object to convert
     * @return the formatted date string, or null if the input is null
     */
    public static String localDateTimeToString(LocalDateTime localDateTime) {
        return getFormattedDateTime(localDateTime, API_RESPONSE_FORMAT);
    }

    /**
     * Converts a {@link LocalDate} to an HL7-compliant date string in the format yyyyMMdd.
     *
     * @param date the LocalDate to convert
     * @return a formatted date string or an empty string if the input is null
     * @author Molu Tyagi
     */
    public static String yyyyMMdd(LocalDate date) {
        return (date != null) ? date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) : "";
    }

    public static String getFormattedDateTime(LocalDateTime localDateTime, String format) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return dateTimeFormatter.format(localDateTime);
    }
}
