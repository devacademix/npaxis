package com.digitalearn.npaxis.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for password strength.
 * Checks that password meets all requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter (A-Z)
 * - At least one lowercase letter (a-z)
 * - At least one digit (0-9)
 * - At least one special character (!@#$%^&*...)
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // Pattern to validate password strength
    // (?=.*[A-Z]) - at least one uppercase
    // (?=.*[a-z]) - at least one lowercase
    // (?=.*\d) - at least one digit
    // (?=.*[@$!%*?&^#()\\-_=+\\[\\]{};:'\",.<>?/\\\\|`~]) - at least one special character
    // .{8,} - at least 8 characters
    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&^#()\\-_=+\\[\\]{};:'\\\",.<>?/\\\\|`~]).{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Allow null values - use @NotNull or @NotBlank for null checks
        if (value == null) {
            return true;
        }

        return pattern.matcher(value).matches();
    }
}

