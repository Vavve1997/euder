package com.vavedin.euder.feature.eurder.domain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;
class ItemGroupTest {
    @Test void storesSnapshotAndCalculatesLineTotal() {
        var date = LocalDate.of(2026, 10, 21);
        var group = new ItemGroup("item-1", "Coffee", new BigDecimal("89.95"), 3, date);
        assertThat(group.getItemId()).isEqualTo("item-1");
        assertThat(group.getItemName()).isEqualTo("Coffee");
        assertThat(group.getUnitPrice()).isEqualByComparingTo("89.95");
        assertThat(group.getAmount()).isEqualTo(3);
        assertThat(group.getShippingDate()).isEqualTo(date);
        assertThat(group.calculateTotalPrice()).isEqualByComparingTo("269.85");
    }
    @ParameterizedTest @ValueSource(ints = {0, -1})
    void rejectsNonPositiveAmount(int amount) {
        assertThatThrownBy(() -> new ItemGroup("a", "Coffee", new BigDecimal("89.95"),
                amount, LocalDate.of(2026, 10, 21))).isInstanceOf(IllegalArgumentException.class);
    }
}
