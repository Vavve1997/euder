package com.vavedin.euder.feature.item.service;

import com.vavedin.euder.feature.item.api.ItemMapper;
import com.vavedin.euder.feature.item.api.dto.CreateItemDto;
import com.vavedin.euder.feature.item.api.dto.ItemDto;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.item.domain.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    private ItemRepository itemRepository;
    private ItemService itemService;

    @BeforeEach
    void setup() {
        itemRepository = mock(ItemRepository.class);
        itemService = new ItemService(new ItemMapper(), itemRepository);
    }

    @Test
    void addItem_savesInputValuesAndReturnsRepositoryResultAsDto() {
        CreateItemDto request = new CreateItemDto(
                "Coffee machine",
                "Makes excellent coffee",
                new BigDecimal("89.95"),
                12
        );

        Item storedItem = new Item(
                "Coffee machine",
                "Makes excellent coffee",
                new BigDecimal("89.95"),
                12,
                "stored-item-id"
        );

        when(itemRepository.addItem(any(Item.class)))
                .thenReturn(storedItem);

        ItemDto result = itemService.addItem(request);

        ArgumentCaptor<Item> captor =
                ArgumentCaptor.forClass(Item.class);

        verify(itemRepository).addItem(captor.capture());

        Item submittedItem = captor.getValue();

        assertThat(submittedItem.getName())
                .isEqualTo("Coffee machine");
        assertThat(submittedItem.getDescription())
                .isEqualTo("Makes excellent coffee");
        assertThat(submittedItem.getPrice()).isEqualByComparingTo("89.95");
        assertThat(submittedItem.getAmount()).isEqualTo(12);
        assertThat(submittedItem.getId()).isNotBlank();

        ItemDto expected = new ItemDto(
                "Coffee machine",
                "Makes excellent coffee",
                new BigDecimal("89.95"),
                12,
                "stored-item-id"
        );

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

}