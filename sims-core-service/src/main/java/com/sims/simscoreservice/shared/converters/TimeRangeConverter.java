package com.sims.simscoreservice.shared.converters;


import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.analytics.enums.TimeRange;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class TimeRangeConverter implements Converter<String, TimeRange> {

    @Override
    public TimeRange convert(@NonNull String source) {
        try{
            return TimeRange.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(TimeRange.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            throw new ValidationException(
                    String.format("Invalid Time Range status: '%s'. Valid values: %s", source, validValues)
            );
        }
    }
}
