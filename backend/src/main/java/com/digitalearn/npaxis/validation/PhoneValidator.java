package com.digitalearn.npaxis.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for phone number format.
 * Accepts phone numbers with 10+ digits, optionally starting with +.
 * Examples: 1234567890, +11234567890, +1 (123) 456-7890
 */
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    // Pattern to validate phone number
    // ^[+]? - optional + at the beginning
    // [0-9\\s()\\-.]* - digits, spaces, parentheses, hyphens, dots
    // [0-9]{10,}$ - at least 10 digits
    private static final String PHONE_PATTERN = "^[+]?[0-9\\s()\\-\\.]*[0-9]{10,}$";

    private static final Pattern pattern = Pattern.compile(PHONE_PATTERN);

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Allow null or empty values - use @NotNull or @NotBlank for mandatory checks
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        return pattern.matcher(value).matches();
    }
}

