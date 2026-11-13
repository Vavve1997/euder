package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.testsupport.StoryApiTest;
import com.vavedin.euder.feature.eurder.api.dto.ItemGroupDto;
import com.vavedin.euder.feature.eurder.domain.*;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.security.enums.Role;
import com.vavedin.euder.feature.user.api.dto.AddressDto;
import com.vavedin.euder.feature.user.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

class ShipmentStoryTest extends StoryApiTest {
    // Test-side response type: no dependency on constructors of the production DTO.
    public record ShipmentResponse(String eurderId, AddressDto shippingAddress, List<ItemGroupDto> itemGroups) {}

    @Test
    @DisplayName("Story 9 - All matching orders, only today's groups, original shipping addresses")
    void givenDifferentShippingDates_whenAdminListsShipments_thenOnlyToday() {
        LocalDate today = LocalDate.now();
        Item bread = saveItem("Bread", "2.50", 20);
        Item wine = saveItem("Wine", "18.00", 20);
        User arya = saveUser("arya", Role.MEMBER);
        Address destination = new Address("Great Pyramid", "2A", "0800", "Meereen");
        Eurder first = eurders.saveAndFlush(new Eurder(stannis.getId(), List.of(
                group(bread, 4, today), group(wine, 2, today.plusDays(1)),
                group(bread, 1, today.minusDays(1))), destination));
        Eurder second = saveEurder(arya, group(wine, 3, today));
        saveEurder(stannis, group(bread, 1, today.minusDays(1)));
        saveEurder(arya, group(wine, 1, today.plusDays(1)));
        // The current user address differs from the address captured in the order.
        jdbc.update("UPDATE eurder.users SET street = ? WHERE id = ?", "New castle", stannis.getId());

        ShipmentResponse[] result = admin.assertGet("/eurders/shipping_today").returns(ShipmentResponse[].class);
        assertThat(result).extracting(ShipmentResponse::eurderId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(result).allSatisfy(shipment -> {
            assertThat(shipment.itemGroups()).hasSize(1);
            assertThat(shipment.itemGroups().getFirst().getShippingDate()).isEqualTo(today);
            if (shipment.eurderId().equals(first.getId())) {
                assertThat(shipment.itemGroups().getFirst().getItemId()).isEqualTo(bread.getId());
                assertThat(shipment.itemGroups().getFirst().getAmount()).isEqualTo(4);
                assertThat(shipment.shippingAddress()).usingRecursiveComparison()
                        .isEqualTo(new AddressDto("Great Pyramid", "2A", "0800", "Meereen"));
            } else {
                assertThat(shipment.itemGroups().getFirst().getItemId()).isEqualTo(wine.getId());
                assertThat(shipment.itemGroups().getFirst().getAmount()).isEqualTo(3);
                assertThat(shipment.shippingAddress()).usingRecursiveComparison()
                        .isEqualTo(new AddressDto("Dragonstone Keep", "1", "1000", "Dragonstone"));
            }
        });
    }

    @Test
    void givenOnlyFutureShipments_whenAdminLists_thenEmptyArray() {
        Item bread = saveItem("Bread", "2.50", 20);
        saveEurder(stannis, group(bread, 2, LocalDate.now().plusDays(1)));
        assertThat(admin.assertGet("/eurders/shipping_today").returns(ShipmentResponse[].class)).isEmpty();
    }

    @Test
    void givenMemberOrGuest_whenListingShipments_thenRejected() {
        member.assertGet("/eurders/shipping_today").failsWith(FORBIDDEN);
        guest.assertGet("/eurders/shipping_today").failsWith(UNAUTHORIZED);
    }
}
