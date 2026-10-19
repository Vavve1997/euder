package com.vavedin.euder.feature.item.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void constructor_withoutId_storesAllValuesAndGeneratesId() {
        Item item = new Item(
                "Coffee machine",
                "Makes excellent coffee",
                89.95,
                12
        );

        assertThat(item.getName()).isEqualTo("Coffee machine");
        assertThat(item.getDescription())
                .isEqualTo("Makes excellent coffee");
        assertThat(item.getPrice()).isEqualTo(89.95);
        assertThat(item.getAmount()).isEqualTo(12);
        assertThat(item.getId()).isNotBlank();

        assertThat(UUID.fromString(item.getId()).toString())
                .isEqualTo(item.getId());
    }

    @Test
    void constructor_withId_preservesAllProvidedValues() {
        Item item = new Item(
                "Coffee machine",
                "Makes excellent coffee",
                89.95,
                12,
                "existing-item-id"
        );

        assertThat(item.getName()).isEqualTo("Coffee machine");
        assertThat(item.getDescription())
                .isEqualTo("Makes excellent coffee");
        assertThat(item.getPrice()).isEqualTo(89.95);
        assertThat(item.getAmount()).isEqualTo(12);
        assertThat(item.getId()).isEqualTo("existing-item-id");
    }

}