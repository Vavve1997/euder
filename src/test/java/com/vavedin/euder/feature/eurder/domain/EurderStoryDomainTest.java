package com.vavedin.euder.feature.eurder.domain;

import com.vavedin.euder.feature.user.domain.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class EurderStoryDomainTest {
    @Test
    @DisplayName("Story 3 - Monetary totals use exact decimal arithmetic")
    void givenMultipleGroups_whenCalculatingTotal_thenExactSum() {
        ItemGroup bread = new ItemGroup("bread", "Bread", new BigDecimal("0.10"), 3, LocalDate.of(2026, 1, 2));
        ItemGroup wine = new ItemGroup("wine", "Wine", new BigDecimal("2.25"), 2, LocalDate.of(2026, 1, 8));
        Eurder eurder = new Eurder("stannis", List.of(bread, wine), new Address("Keep", "1", "1000", "Dragonstone"));
        assertThat(bread.calculateTotalPrice()).isEqualByComparingTo("0.30");
        assertThat(eurder.calculateTotalPrice()).isEqualByComparingTo("4.80");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void givenNonPositiveQuantity_whenCreatingGroup_thenRejected(int quantity) {
        assertThatThrownBy(() -> new ItemGroup("bread", "Bread", BigDecimal.TEN, quantity, LocalDate.of(2026, 1, 2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenNoGroups_whenCreatingEurder_thenRejected() {
        assertThatThrownBy(() -> new Eurder("stannis", List.of(), new Address("Keep", "1", "1000", "Dragonstone")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
