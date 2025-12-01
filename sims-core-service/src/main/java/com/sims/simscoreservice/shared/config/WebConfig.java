package com.sims.simscoreservice.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web Configuration
 * Configures CORS and custom enum converters
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed.origins}")
    private String[] allowedOrigins;

    @Value("${cors.allowed.methods}")
    private String[] allowedMethods;

    @Value("${cors.allow.credentials}")
    private boolean allowCredentials;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(allowCredentials); // Required for cookies
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Product Enums
        registry.addConverter(new ProductCategoriesConverter());
        registry.addConverter(new ProductStatusConverter());

        // Inventory Enums
        registry.addConverter(new InventoryStatusConverter());
        registry.addConverter(new LossReasonConverter());

        // TODO: Add SO/PO converters when implementing
        // registry.addConverter(new SalesOrderStatusConverter());
        // registry.addConverter(new PurchaseOrderStatusConverter());
    }
}
