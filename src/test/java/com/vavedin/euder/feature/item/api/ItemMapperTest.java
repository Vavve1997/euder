package com.vavedin.euder.feature.item.api;

import com.vavedin.euder.feature.item.api.dto.CreateItemDto;
import com.vavedin.euder.feature.item.api.dto.ItemDto;
import com.vavedin.euder.feature.item.domain.Item;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class ItemMapperTest {

    private final ItemMapper itemMapper = new ItemMapper();

    @Test
    void toEntity_preservesAllInputValuesAndGeneratesId() {
        CreateItemDto request = new CreateItemDto(
                "Coffee machine",
                "Makes excellent coffee",
                new BigDecimal("89.95"),
                12
        );

        Item result = itemMapper.toEntity(request);

        assertThat(result.getName()).isEqualTo("Coffee machine");
        assertThat(result.getDescription())
                .isEqualTo("Makes excellent coffee");
        assertThat(result.getPrice()).isEqualByComparingTo("89.95");
        assertThat(result.getAmount()).isEqualTo(12);
        assertThat(result.getId()).isNotBlank();
    }

    @Test
    void toDto_preservesAllValuesIncludingId() {
        Item item = new Item(
                "Coffee machine",
                "Makes excellent coffee",
                new BigDecimal("89.95"),
                12,
                "item-123"
        );

        ItemDto result = itemMapper.toDto(item);

        assertThat(result.getName()).isEqualTo("Coffee machine");
        assertThat(result.getDescription())
                .isEqualTo("Makes excellent coffee");
        assertThat(result.getPrice()).isEqualByComparingTo("89.95");
        assertThat(result.getAmount()).isEqualTo(12);
        assertThat(result.getId()).isEqualTo("item-123");
    }

}