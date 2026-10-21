package com.vavedin.euder.feature.eurder.domain;

import com.vavedin.euder.feature.user.domain.Address;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EurderRepositoryTest {
    @Test
    void savesCompleteEurdersUnderTheirIdsWithoutReplacingPreviousEurder() {
        var database = new EurderDatabase();
        var repository = new EurderRepository(database);
        var group = new ItemGroup("a", "Coffee", new BigDecimal("10.00"), 2, LocalDate.of(2026, 10, 21));
        var first = new Eurder("customer-1", List.of(group), new Address("Kingsroad", 1, 1000, "Winterfell"));
        var second = new Eurder("customer-2", List.of(group), new Address("Kingsroad", 1, 1000, "Winterfell"));
        assertThat(repository.addEurder(first)).isSameAs(first);
        assertThat(repository.addEurder(second)).isSameAs(second);
        assertThat(database.getEurderDatabase()).hasSize(2)
                .containsEntry(first.getId(), first).containsEntry(second.getId(), second);
        assertThat(database.getEurderDatabase().get(first.getId()).getItemGroups()).containsExactly(group);
    }
}
