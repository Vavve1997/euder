package com.vavedin.euder.feature.eurder.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EurderReportRepositoryTest {

    private EurderRepository eurderRepository;

    @BeforeEach
    void setup() {
        eurderRepository = new EurderRepository(new EurderDatabase());
    }

    @Test
    void findByCustomerId_returnsOnlyEurdersOfThatCustomer() {
        ItemGroup itemGroup = new ItemGroup(
                "item-1",
                "Coffee",
                new BigDecimal("10.00"),
                2,
                LocalDate.of(2026, 10, 21)
        );

        Eurder firstEurder = new Eurder(
                "customer-1", List.of(itemGroup)
        );
        Eurder secondEurder = new Eurder(
                "customer-1", List.of(itemGroup)
        );
        Eurder otherCustomersEurder = new Eurder(
                "customer-2", List.of(itemGroup)
        );

        eurderRepository.addEurder(firstEurder);
        eurderRepository.addEurder(secondEurder);
        eurderRepository.addEurder(otherCustomersEurder);

        List<Eurder> result =
                eurderRepository.findByCustomerId("customer-1");

        assertThat(result)
                .containsExactlyInAnyOrder(firstEurder, secondEurder);
    }

    @Test
    void findByCustomerId_withoutMatchingEurders_returnsAnEmptyList() {
        ItemGroup itemGroup = new ItemGroup(
                "item-1",
                "Coffee",
                new BigDecimal("10.00"),
                1,
                LocalDate.of(2026, 10, 21)
        );

        eurderRepository.addEurder(
                new Eurder("another-customer", List.of(itemGroup))
        );

        List<Eurder> result =
                eurderRepository.findByCustomerId("customer-without-eurders");

        assertThat(result).isEmpty();
    }
}