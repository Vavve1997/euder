package com.vavedin.euder.feature.item.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;


class ItemRepositoryTest {

    private ItemDatabase itemDatabase;
    private ItemRepository itemRepository;

    @BeforeEach
    void setup() {
        itemDatabase = new ItemDatabase();
        itemRepository = new ItemRepository(itemDatabase);
    }

    @Test
    void addItem_storesItemUnderItsIdAndReturnsIt() {
        Item item = new Item(
                "Coffee machine",
                "Makes excellent coffee",
                new BigDecimal("89.95"),
                12
        );

        Item result = itemRepository.addItem(item);

        assertThat(itemDatabase.getItemDatabase())
                .hasSize(1)
                .containsEntry(item.getId(), item);

        assertThat(result).isSameAs(item);
    }

    @Test
    void addItem_preservesPreviouslyAddedItems() {
        Item coffeeMachine = new Item(
                "Coffee machine",
                "Makes excellent coffee",
                new BigDecimal("89.95"),
                12
        );

        Item toaster = new Item(
                "Toaster",
                "Toasts two slices",
                new BigDecimal("29.95"),
                5
        );

        itemRepository.addItem(coffeeMachine);
        itemRepository.addItem(toaster);

        assertThat(itemDatabase.getItemDatabase())
                .hasSize(2)
                .containsEntry(coffeeMachine.getId(), coffeeMachine)
                .containsEntry(toaster.getId(), toaster);
    }
}
