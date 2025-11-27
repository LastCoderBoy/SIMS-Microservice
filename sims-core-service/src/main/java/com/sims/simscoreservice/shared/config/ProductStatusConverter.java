package com.sims.simscoreservice.shared.config;

import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.product.enums.ProductStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class ProductStatusConverter implements Converter<String, ProductStatus> {

    @Override
    public ProductStatus convert(@NonNull String source) {
        try {
            return ProductStatus.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(ProductStatus.values())
                    .map(Enum::name)
                    . collect(Collectors.joining(", "));

            throw new ValidationException(
                    String. format("Invalid product status: '%s'. Valid values: %s", source, validValues)
            );
        }
    }
}
