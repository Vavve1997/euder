package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.feature.eurder.domain.*;
import com.vavedin.euder.feature.item.domain.ItemDatabase;
import com.vavedin.euder.feature.item.domain.ItemRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EurderMapperTest {
    @Test
    void toDtoIncludesCustomerGroupsDatesPricesAndTotal() {
        var date = LocalDate.of(2026, 10, 21);
        var eurder = new Eurder("customer-id", List.of(
                new ItemGroup("a", "Coffee", new BigDecimal("89.95"), 3, date),
                new ItemGroup("b", "Toaster", new BigDecimal("29.95"), 2, date.plusDays(6))));
        var dto = new EurderMapper(new ItemGroupMapper()).toDto(eurder);
        assertThat(dto.getId()).isEqualTo(eurder.getId());
        assertThat(dto.getCustomerId()).isEqualTo("customer-id");
        assertThat(dto.getTotalPrice()).isEqualByComparingTo("329.75");
        assertThat(dto.getItemGroups()).hasSize(2);
        var first = dto.getItemGroups().get(0);
        assertThat(first.getItemId()).isEqualTo("a");
        assertThat(first.getItemName()).isEqualTo("Coffee");
        assertThat(first.getUnitPrice()).isEqualByComparingTo("89.95");
        assertThat(first.getAmount()).isEqualTo(3);
        assertThat(first.getShippingDate()).isEqualTo(date);
        assertThat(first.getTotalPrice()).isEqualByComparingTo("269.85");
        var second = dto.getItemGroups().get(1);
        assertThat(second.getItemId()).isEqualTo("b");
        assertThat(second.getItemName()).isEqualTo("Toaster");
        assertThat(second.getUnitPrice()).isEqualByComparingTo("29.95");
        assertThat(second.getAmount()).isEqualTo(2);
        assertThat(second.getShippingDate()).isEqualTo(date.plusDays(6));
        assertThat(second.getTotalPrice()).isEqualByComparingTo("59.90");
    }
}
