package com.digitalearn.npaxis.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for phone number format.
 * Phone must be a valid format (10+ digits, optionally starting with +).
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
@Documented
public @interface ValidPhone {
    String message() default "Phone number must be valid (10+ digits, optionally starting with +)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

