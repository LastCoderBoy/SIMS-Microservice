package com.sims.simscoreservice.shared.converters;

import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.product.enums.ProductCategories;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class ProductCategoriesConverter implements Converter<String, ProductCategories> {

    @Override
    public ProductCategories convert(@NonNull String source) {
        try {
            return ProductCategories.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(ProductCategories.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            throw new ValidationException(
                    String.format("Invalid category: '%s'. Valid values: %s", source, validValues)
            );
        }
    }
}
