package com.sims.simscoreservice.product.enums;

import lombok.Getter;

/**
 * Product Categories
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Getter
public enum ProductCategories {
    EDUCATION("Educational Toys", "Learning and development toys"),
    ELECTRONIC("Electronic Toys", "Battery-operated and electronic gadgets"),
    ACTION_FIGURES("Action Figures", "Superhero and character figures"),
    DOLLS("Dolls", "Dolls and doll accessories"),
    MUSICAL_TOYS("Musical Toys", "Instruments and music-making toys"),
    OUTDOOR_TOYS("Outdoor Toys", "Sports and outdoor play equipment");

    private final String displayName;
    private final String description;

    ProductCategories(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}