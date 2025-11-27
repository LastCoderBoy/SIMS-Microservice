package com.sims.simscoreservice.shared.config;

import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class InventoryStatusConverter implements Converter<String, InventoryStatus> {

    @Override
    public InventoryStatus convert(@NonNull String source) {
        try {
            return InventoryStatus.valueOf(source.trim(). toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(InventoryStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            throw new ValidationException(
                    String.format("Invalid inventory status: '%s'.  Valid values: %s", source, validValues)
            );
        }
    }
}
