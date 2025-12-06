package com.sims.simscoreservice.shared.converters;


import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class PurchaseOrderStatusConverter implements Converter<String, PurchaseOrderStatus> {

    @Override
    public PurchaseOrderStatus convert(String source) {
        try{
            return PurchaseOrderStatus.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(PurchaseOrderStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            throw new ValidationException(
                    String.format("Invalid Purchase Order status: '%s'. Valid values: %s", source, validValues)
            );
        }
    }
}