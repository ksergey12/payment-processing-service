package com.saas.paymentservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    String message() default "Invalid currency code. Must be a valid ISO 4217 code (e.g. EUR, USD, GBP)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}