package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.testsupport.StoryApiTest;
import com.vavedin.euder.feature.eurder.api.dto.*;
import com.vavedin.euder.feature.eurder.domain.*;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.security.enums.Role;
import com.vavedin.euder.feature.user.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

class EurderStoriesTest extends StoryApiTest {
    private CreateEurderDto request(Item item, int amount) {
        return new CreateEurderDto(List.of(new CreateItemGroupDto(item.getId(), amount)));
    }

    @Test
    @DisplayName("Story 3 - Multiple groups get their own shipping dates and correct totals")
    void givenMixedStock_whenOrdering_thenDatesAndTotalsCalculated() {
        Item bread = saveItem("Hot Pie's bread", "2.50", 4);
        Item wine = saveItem("Arbor Gold", "18.00", 1);
        LocalDate before = LocalDate.now();
        EurderDto result = member.assertPost("/eurders", new CreateEurderDto(List.of(
                new CreateItemGroupDto(bread.getId(), 4), new CreateItemGroupDto(wine.getId(), 2))))
                .returns(EurderDto.class);
        LocalDate after = LocalDate.now();
        assertThat(result.getCustomerId()).isEqualTo(stannis.getId());
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getItemGroups()).hasSize(2);
        ItemGroupDto breadGroup = result.getItemGroups().stream()
                .filter(g -> g.getItemId().equals(bread.getId())).findFirst().orElseThrow();
        ItemGroupDto wineGroup = result.getItemGroups().stream()
                .filter(g -> g.getItemId().equals(wine.getId())).findFirst().orElseThrow();
        assertThat(breadGroup.getShippingDate()).isBetween(before.plusDays(1), after.plusDays(1));
        assertThat(wineGroup.getShippingDate()).isBetween(before.plusDays(7), after.plusDays(7));
        assertThat(breadGroup.getAmount()).isEqualTo(4);
        assertThat(breadGroup.getUnitPrice()).isEqualByComparingTo("2.50");
        assertThat(breadGroup.getTotalPrice()).isEqualByComparingTo("10.00");
        assertThat(wineGroup.getAmount()).isEqualTo(2);
        assertThat(wineGroup.getTotalPrice()).isEqualByComparingTo("36.00");
        assertThat(result.getTotalPrice()).isEqualByComparingTo("46.00");
        assertThat(eurders.existsById(result.getId())).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM eurder.item_groups WHERE eurder_id = ?",
                Integer.class, result.getId())).isEqualTo(2);
        assertThat(items.findById(bread.getId()).orElseThrow().getAmount()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void givenInvalidQuantity_whenOrdering_thenNoOrderOrStockChange(int amount) {
        Item bread = saveItem("Bread", "2.50", 10);
        member.assertPost("/eurders", request(bread, amount)).failsWith(BAD_REQUEST);
        assertThat(eurders.count()).isZero();
        assertThat(items.findById(bread.getId()).orElseThrow().getAmount()).isEqualTo(10);
    }

    @Test
    void givenEmptyOrder_whenOrdering_thenBadRequest() {
        member.assertPost("/eurders", new CreateEurderDto(List.of())).failsWith(BAD_REQUEST);
        assertThat(eurders.count()).isZero();
    }

    @Test
    @DisplayName("Story 3 - A later invalid group rolls back the complete order")
    void givenValidThenUnknownItem_whenOrdering_thenNoPartialChanges() {
        Item bread = saveItem("Bread", "2.50", 10);
        member.assertPost("/eurders", new CreateEurderDto(List.of(
                new CreateItemGroupDto(bread.getId(), 2), new CreateItemGroupDto("unknown", 1))))
                .failsWith(BAD_REQUEST);
        assertThat(eurders.count()).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM eurder.item_groups", Integer.class)).isZero();
        assertThat(items.findById(bread.getId()).orElseThrow().getAmount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Story 5 - Report contains only own orders, group totals and grand total")
    void givenOrdersForDifferentCustomers_whenReport_thenOnlyOwnOrders() {
        Item bread = saveItem("Bread", "2.50", 20);
        Item wine = saveItem("Wine", "18.00", 20);
        Eurder first = saveEurder(stannis, group(bread, 4, LocalDate.now()), group(wine, 2, LocalDate.now()));
        Eurder second = saveEurder(stannis, group(bread, 2, LocalDate.now()));
        User arya = saveUser("arya", Role.MEMBER);
        saveEurder(arya, group(wine, 10, LocalDate.now()));
        EurderReportDto report = member.assertGet("/eurders/report").returns(EurderReportDto.class);
        assertThat(report.getEurders()).extracting(EurderDto::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(report.getTotalPrice()).isEqualByComparingTo("51.00");
        EurderDto firstResult = report.getEurders().stream().filter(e -> e.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(firstResult.getTotalPrice()).isEqualByComparingTo("46.00");
        assertThat(firstResult.getItemGroups()).extracting(ItemGroupDto::getItemName)
                .containsExactlyInAnyOrder("Bread", "Wine");
        assertThat(firstResult.getItemGroups()).allSatisfy(g -> {
            if (g.getItemId().equals(bread.getId())) {
                assertThat(g.getAmount()).isEqualTo(4);
                assertThat(g.getTotalPrice()).isEqualByComparingTo("10.00");
            } else {
                assertThat(g.getAmount()).isEqualTo(2);
                assertThat(g.getTotalPrice()).isEqualByComparingTo("36.00");
            }
        });
    }

    @Test
    void givenNoOrders_whenReport_thenEmptyReportWithZeroTotal() {
        EurderReportDto report = member.assertGet("/eurders/report").returns(EurderReportDto.class);
        assertThat(report.getEurders()).isEmpty();
        assertThat(report.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Stories 3 and 5 - Later catalogue changes do not change historical orders")
    void givenChangedCatalogue_whenReport_thenHistoricalNameAndPricePreserved() {
        Item bread = saveItem("Bread", "2.50", 10);
        EurderDto original = member.assertPost("/eurders", request(bread, 2)).returns(EurderDto.class);
        // Direct fixture change isolates historical reporting from the update endpoint.
        jdbc.update("UPDATE eurder.items SET name = ?, price = ?, version = version + 1 WHERE id = ?",
                "Royal bread", new BigDecimal("9.00"), bread.getId());
        EurderReportDto report = member.assertGet("/eurders/report").returns(EurderReportDto.class);
        assertThat(report.getEurders()).hasSize(1);
        assertThat(report.getEurders().getFirst().getId()).isEqualTo(original.getId());
        ItemGroupDto oldGroup = report.getEurders().getFirst().getItemGroups().getFirst();
        assertThat(oldGroup.getItemName()).isEqualTo("Bread");
        assertThat(oldGroup.getUnitPrice()).isEqualByComparingTo("2.50");
        assertThat(report.getTotalPrice()).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("Story 6 - Reorder copies quantities but recalculates prices and shipping")
    void givenOwnOrderAndNewPrices_whenReorder_thenNewOrderAtCurrentPrices() {
        Item bread = saveItem("Bread", "2.50", 10);
        Item wine = saveItem("Wine", "18.00", 0);
        Eurder original = saveEurder(stannis, group(bread, 2, LocalDate.now().minusDays(4)),
                group(wine, 1, LocalDate.now().minusDays(4)));
        jdbc.update("UPDATE eurder.items SET price = ?, version = version + 1 WHERE id = ?",
                new BigDecimal("4.00"), bread.getId());
        LocalDate before = LocalDate.now();
        EurderDto result = member.assertPostEmptyBody("/eurders/reorder/{id}", original.getId())
                .returns(EurderDto.class);
        LocalDate after = LocalDate.now();
        assertThat(result.getId()).isNotEqualTo(original.getId());
        assertThat(result.getCustomerId()).isEqualTo(stannis.getId());
        assertThat(result.getTotalPrice()).isEqualByComparingTo("26.00");
        assertThat(result.getItemGroups()).hasSize(2);
        assertThat(result.getItemGroups()).extracting(ItemGroupDto::getItemId)
                .containsExactlyInAnyOrder(bread.getId(), wine.getId());
        assertThat(result.getItemGroups()).allSatisfy(g -> {
            if (g.getItemId().equals(bread.getId())) {
                assertThat(g.getAmount()).isEqualTo(2);
                assertThat(g.getUnitPrice()).isEqualByComparingTo("4.00");
                assertThat(g.getShippingDate()).isBetween(before.plusDays(1), after.plusDays(1));
            } else {
                assertThat(g.getAmount()).isEqualTo(1);
                assertThat(g.getUnitPrice()).isEqualByComparingTo("18.00");
                assertThat(g.getShippingDate()).isBetween(before.plusDays(7), after.plusDays(7));
            }
        });
        assertThat(eurders.count()).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT unit_price FROM eurder.item_groups WHERE eurder_id = ? AND item_id = ?",
                BigDecimal.class, original.getId(), bread.getId())).isEqualByComparingTo("2.50");
    }

    @Test
    void givenSomeoneElsesOrder_whenReordering_thenForbiddenAndUnchanged() {
        Item bread = saveItem("Bread", "2.50", 10);
        User arya = saveUser("arya", Role.MEMBER);
        Eurder original = saveEurder(arya, group(bread, 2, LocalDate.now()));
        member.assertPostEmptyBody("/eurders/reorder/{id}", original.getId()).failsWith(FORBIDDEN);
        assertThat(eurders.count()).isEqualTo(1);
        assertThat(items.findById(bread.getId()).orElseThrow().getAmount()).isEqualTo(10);
    }

    @Test
    void givenUnknownOrder_whenReordering_thenNotFound() {
        member.assertPostEmptyBody("/eurders/reorder/unknown").failsWith(NOT_FOUND);
        assertThat(eurders.count()).isZero();
    }

    @Test
    void givenGuestOrAdmin_whenUsingCustomerEndpoints_thenRejected() {
        Item bread = saveItem("Bread", "2.50", 10);
        guest.assertPost("/eurders", request(bread, 1)).failsWith(UNAUTHORIZED);
        admin.assertPost("/eurders", request(bread, 1)).failsWith(FORBIDDEN);
        guest.assertGet("/eurders/report").failsWith(UNAUTHORIZED);
        admin.assertGet("/eurders/report").failsWith(FORBIDDEN);
        guest.assertPostEmptyBody("/eurders/reorder/unknown").failsWith(UNAUTHORIZED);
        admin.assertPostEmptyBody("/eurders/reorder/unknown").failsWith(FORBIDDEN);
        assertThat(eurders.count()).isZero();
        assertThat(items.findById(bread.getId()).orElseThrow().getAmount()).isEqualTo(10);
    }
}
