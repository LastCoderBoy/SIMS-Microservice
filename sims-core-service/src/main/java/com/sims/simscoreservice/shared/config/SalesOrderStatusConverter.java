package com.sims.simscoreservice.shared.config;


import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.product.enums.ProductStatus;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class SalesOrderStatusConverter implements Converter<String, SalesOrderStatus> {

    @Override
    public SalesOrderStatus convert(@NonNull String source) {
        try{
            return SalesOrderStatus.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(SalesOrderStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            throw new ValidationException(
                    String.format("Invalid Sales Order status: '%s'. Valid values: %s", source, validValues)
            );
        }
    }
}
