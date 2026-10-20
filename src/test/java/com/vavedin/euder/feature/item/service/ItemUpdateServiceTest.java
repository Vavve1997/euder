package com.vavedin.euder.feature.item.service;

import com.vavedin.euder.feature.item.api.ItemMapper;
import com.vavedin.euder.feature.item.api.dto.ItemDto;
import com.vavedin.euder.feature.item.api.dto.UpdateItemDto;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.item.domain.ItemDatabase;
import com.vavedin.euder.feature.item.domain.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemUpdateServiceTest {

    private ItemDatabase itemDatabase;
    private ItemService itemService;
    private Item originalItem;

    @BeforeEach
    void setup() {
        itemDatabase = new ItemDatabase();
        ItemRepository itemRepository = new ItemRepository(itemDatabase);

        itemService = new ItemService(
                new ItemMapper(),
                itemRepository
        );

        originalItem = new Item(
                "Coffee",
                "Original description",
                new BigDecimal("10.00"),
                5
        );

        itemRepository.addItem(originalItem);
    }

    @Test
    void updateItem_updatesAllFieldsAndKeepsTheSameId() {
        UpdateItemDto update = new UpdateItemDto(
                "Premium coffee",
                "Updated description",
                new BigDecimal("12.50"),
                8
        );

        ItemDto result = itemService.updateItem(
                originalItem.getId(), update
        );

        assertThat(result.getId()).isEqualTo(originalItem.getId());
        assertThat(result.getName()).isEqualTo("Premium coffee");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getPrice()).isEqualByComparingTo("12.50");
        assertThat(result.getAmount()).isEqualTo(8);

        Item savedItem = itemDatabase.getItemDatabase()
                .get(originalItem.getId());

        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getId()).isEqualTo(originalItem.getId());
        assertThat(savedItem.getName()).isEqualTo("Premium coffee");
        assertThat(savedItem.getDescription()).isEqualTo("Updated description");
        assertThat(savedItem.getPrice()).isEqualByComparingTo("12.50");
        assertThat(savedItem.getAmount()).isEqualTo(8);

        // Een update mag geen tweede item aanmaken.
        assertThat(itemDatabase.getItemDatabase()).hasSize(1);
    }

    @Test
    void updateItem_withZeroStock_isAllowed() {
        UpdateItemDto update = new UpdateItemDto(
                "Coffee",
                "Sold out",
                new BigDecimal("10.00"),
                0
        );

        ItemDto result = itemService.updateItem(
                originalItem.getId(), update
        );

        assertThat(result.getAmount()).isZero();
        assertThat(itemDatabase.getItemDatabase()
                .get(originalItem.getId())
                .getAmount()).isZero();
    }

    @Test
    void updateItem_withInvalidPrice_doesNotChangeTheStoredItem() {
        UpdateItemDto update = new UpdateItemDto(
                "Changed name",
                "Changed description",
                BigDecimal.ZERO,
                8
        );

        assertThatThrownBy(() ->
                itemService.updateItem(originalItem.getId(), update)
        ).isInstanceOf(IllegalArgumentException.class);

        Item savedItem = itemDatabase.getItemDatabase()
                .get(originalItem.getId());

        // Ook de geldige velden mogen niet gedeeltelijk gewijzigd zijn.
        assertThat(savedItem.getName()).isEqualTo("Coffee");
        assertThat(savedItem.getDescription()).isEqualTo("Original description");
        assertThat(savedItem.getPrice()).isEqualByComparingTo("10.00");
        assertThat(savedItem.getAmount()).isEqualTo(5);
        assertThat(itemDatabase.getItemDatabase()).hasSize(1);
    }

    @Test
    void updateItem_withNegativeStock_doesNotChangeTheStoredItem() {
        UpdateItemDto update = new UpdateItemDto(
                "Changed name",
                "Changed description",
                new BigDecimal("12.50"),
                -1
        );

        assertThatThrownBy(() ->
                itemService.updateItem(originalItem.getId(), update)
        ).isInstanceOf(IllegalArgumentException.class);

        Item savedItem = itemDatabase.getItemDatabase()
                .get(originalItem.getId());

        assertThat(savedItem.getName()).isEqualTo("Coffee");
        assertThat(savedItem.getDescription()).isEqualTo("Original description");
        assertThat(savedItem.getPrice()).isEqualByComparingTo("10.00");
        assertThat(savedItem.getAmount()).isEqualTo(5);
    }
}