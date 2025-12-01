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
    EDUCATION("Educational Toy", "Learning and development toys"),
    ELECTRONIC("Electronic Toy", "Battery-operated and electronic gadgets"),
    ACTION_FIGURES("Action Figure", "Superhero and character figures"),
    DOLLS("Dolls", "Dolls and doll accessories"),
    MUSICAL_TOY("Musical Toy", "Instruments and music-making toys"),
    OUTDOOR_TOY("Outdoor Toy", "Sports and outdoor play equipment");

    private final String displayName;
    private final String description;

    ProductCategories(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}