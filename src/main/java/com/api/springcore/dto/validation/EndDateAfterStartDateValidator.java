package com.api.springcore.dto.validation;

import com.api.springcore.dto.EventRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndDateAfterStartDateValidator
        implements ConstraintValidator<EndDateAfterStartDate, EventRequest.Create> {

    @Override
    public boolean isValid(EventRequest.Create dto, ConstraintValidatorContext ctx) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            return true;
        }
        return !dto.getEndDate().isBefore(dto.getStartDate());
    }
}