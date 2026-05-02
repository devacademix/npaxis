package com.digitalearn.npaxis.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Utility class for string operations and validation.
 * Provides safe string manipulation and common validations.
 *
 * @author NPAxis Team
 * @version 1.0.0
 */
@Slf4j
public class StringUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9]{10,}$");

    private static final Pattern ALPHANUMERIC_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_-]+$");

    /**
     * Checks if a string is null or empty.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Checks if a string is not null and not empty.
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Checks if a string is null, empty, or contains only whitespace.
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Checks if a string is not null, not empty, and contains non-whitespace.
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Safely trims a string.
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * Checks if a string is a valid email address.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Checks if a string is a valid phone number.
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Checks if a string contains only alphanumeric characters, underscores, and hyphens.
     */
    public static boolean isAlphanumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Safely converts a string to uppercase.
     */
    public static String toUpperCase(String str) {
        return str == null ? null : str.toUpperCase();
    }

    /**
     * Safely converts a string to lowercase.
     */
    public static String toLowerCase(String str) {
        return str == null ? null : str.toLowerCase();
    }

    /**
     * Checks if a string starts with a prefix (case-insensitive).
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        return str.toLowerCase().startsWith(prefix.toLowerCase());
    }

    /**
     * Checks if a string ends with a suffix (case-insensitive).
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        return str.toLowerCase().endsWith(suffix.toLowerCase());
    }

    /**
     * Safely joins strings with a delimiter.
     */
    public static String join(String delimiter, Object... items) {
        if (items == null || items.length == 0) {
            return "";
        }
        return String.join(delimiter,
                java.util.Arrays.stream(items)
                        .map(String::valueOf)
                        .toArray(String[]::new));
    }

    /**
     * Masks a portion of a string for sensitive data.
     */
    public static String mask(String str, int showPrefix, int showSuffix) {
        if (isEmpty(str) || str.length() <= (showPrefix + showSuffix)) {
            return str;
        }
        String prefix = str.substring(0, showPrefix);
        String suffix = str.substring(str.length() - showSuffix);
        int maskLength = str.length() - showPrefix - showSuffix;
        String mask = "*".repeat(Math.max(0, maskLength));
        return prefix + mask + suffix;
    }

    /**
     * Converts a full name to initials.
     * If only one name is provided, returns the first letter.
     * If multiple names are provided, returns the first letter of each name.
     */
    public static String getInitials(String fullName) {
        if (isEmpty(fullName)) {
            return fullName;
        }

        String trimmed = trim(fullName);
        String[] parts = trimmed.split("\\s+");

        if (parts.length == 1) {
            // Only one name, return first letter
            return parts[0].substring(0, 1).toUpperCase();
        }

        // Multiple names, return first letter of each
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.substring(0, 1).toUpperCase());
            }
        }
        return initials.toString();
    }
}

