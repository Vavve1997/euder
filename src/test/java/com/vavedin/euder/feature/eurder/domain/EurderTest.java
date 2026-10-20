package com.vavedin.euder.feature.eurder.domain;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
class EurderTest {
    private ItemGroup group(String itemName, String price, int amount) {
        return new ItemGroup("item-id", itemName, new BigDecimal(price), amount, LocalDate.of(2026, 10, 21));
    }
    @Test void generatesIdAndSumsAllGroups() {
        var groups = List.of(group("a", "89.95", 3), group("b", "29.95", 2));
        var eurder = new Eurder("customer-id", groups);
        assertThat(eurder.getId()).isNotBlank();
        assertThat(eurder.getCustomerId()).isEqualTo("customer-id");
        assertThat(eurder.getItemGroups()).containsExactlyElementsOf(groups);
        assertThat(eurder.calculateTotalPrice()).isEqualByComparingTo("329.75");
    }
    @Test void requiresAtLeastOneGroup() {
        assertThatThrownBy(() -> new Eurder("customer-id", List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Eurder("customer-id", null)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void protectsGroupsFromExternalChanges() {
        var first = group("a", "10.00", 1);
        var input = new ArrayList<>(List.of(first));
        var eurder = new Eurder("customer-id", input);
        input.clear();
        assertThat(eurder.getItemGroups()).containsExactly(first);
        // Contract: getItemGroups returns an unmodifiable list.
        assertThatThrownBy(() -> eurder.getItemGroups().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
