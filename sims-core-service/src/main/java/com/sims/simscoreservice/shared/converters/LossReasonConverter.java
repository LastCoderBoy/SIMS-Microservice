package com.sims.simscoreservice.shared.converters;

import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.inventory.enums.LossReason;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class LossReasonConverter implements Converter<String, LossReason> {
    @Override
    public LossReason convert(String source) {
        try {
            return LossReason.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(LossReason.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            throw new ValidationException(
                    String.format("Invalid category: '%s'. Valid values: %s", source, validValues)
            );
        }
    }
}
