package com.vavedin.euder.feature.eurder.service;

import com.vavedin.euder.feature.eurder.api.EurderMapper;
import com.vavedin.euder.feature.eurder.api.ItemGroupMapper;
import com.vavedin.euder.feature.eurder.api.dto.*;
import com.vavedin.euder.feature.eurder.domain.*;
import com.vavedin.euder.feature.item.domain.*;
import com.vavedin.euder.feature.user.domain.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EurderServiceTest {
    private ItemRepository items;
    private EurderRepository eurderRepository;
    private EurderService service;

    private final User customer = new User("Test", "User", "test@example.com",
            new Address("Street", 1, 1000, "City"), "012345", "customer-id", "test-hash");

    @BeforeEach
    void setup() {
        items = mock(ItemRepository.class);
        eurderRepository = mock(EurderRepository.class);
        service = new EurderService(eurderRepository, new EurderMapper(new ItemGroupMapper(items)), items);
    }

    private CreateEurderDto request(int amount) {
        return new CreateEurderDto(List.of(new CreateItemGroupDto("item-1", amount)));
    }

    private Item item(int stock, String price) {
        return new Item("Coffee", "Good coffee", new BigDecimal(price), stock, "item-1");
    }

    @ParameterizedTest
    @MethodSource("provideShippingDateTestData")
    void shippingDateDependsOnSufficientStock(int stock, int amount, LocalDate expectedDate) {

        when(items.findById("item-1")).thenReturn(Optional.of(item(stock, "89.95")));
        when(eurderRepository.addEurder(any(Eurder.class)))
                .thenAnswer(call -> call.getArgument(0));

        var dto = service.addEurder(request(amount), customer);

        var captor = ArgumentCaptor.forClass(Eurder.class);
        verify(eurderRepository).addEurder(captor.capture());
        var saved = captor.getValue();

        assertThat(saved.getCustomerId()).isEqualTo(customer.getId());
        assertThat(saved.getItemGroups()).hasSize(1);

        var group = saved.getItemGroups().getFirst();
        assertThat(group.getItemId()).isEqualTo("item-1");
        assertThat(group.getAmount()).isEqualTo(amount);
        assertThat(group.getUnitPrice()).isEqualByComparingTo("89.95");
        assertThat(group.getShippingDate()).isEqualTo(expectedDate);
        assertThat(dto.getId()).isEqualTo(saved.getId());
        assertThat(dto.getTotalPrice()).isEqualByComparingTo("269.85");
    }

    private static Stream<Arguments> provideShippingDateTestData() {
        LocalDate today = LocalDate.now();
        return Stream.of(
                // Arguments.of(stock, amount, expectedDate, expectedTotalPrice)
                Arguments.of(5, 3, today.plusDays(1), "269.85"),
                Arguments.of(3, 3, today.plusDays(1), "269.85"),
                Arguments.of(2, 3, today.plusWeeks(1), "269.85"),
                Arguments.of(0, 3, today.plusWeeks(1), "269.85")
        );
    }

    @Test
    void multipleGroupsHaveIndependentDatesAndCombinedTotal() {
        when(items.findById("item-1")).thenReturn(Optional.of(item(3, "89.95")));
        when(items.findById("item-2")).thenReturn(Optional.of(
                new Item("Toaster", "Toasts bread", new BigDecimal("29.95"), 0, "item-2")));
        when(eurderRepository.addEurder(any(Eurder.class))).thenAnswer(call -> call.getArgument(0));
        var dto = service.addEurder(new CreateEurderDto(List.of(
                new CreateItemGroupDto("item-1", 3), new CreateItemGroupDto("item-2", 2))), customer);
        assertThat(dto.getTotalPrice()).isEqualByComparingTo("329.75");
        assertThat(dto.getItemGroups()).hasSize(2);
        assertThat(dto.getItemGroups().get(0).getShippingDate()).isEqualTo(LocalDate.of(2026, 10, 21));
        assertThat(dto.getItemGroups().get(1).getShippingDate()).isEqualTo(LocalDate.of(2026, 10, 27));
        verify(eurderRepository).addEurder(any(Eurder.class));
    }

    @Test
    void unknownItemDoesNotSavePartialEurder() {
        when(items.findById("item-1")).thenReturn(Optional.of(item(10, "89.95")));
        when(items.findById("missing")).thenReturn(Optional.empty());
        var request = new CreateEurderDto(List.of(new CreateItemGroupDto("item-1", 1), new CreateItemGroupDto("missing", 1)));
        assertThatThrownBy(() -> service.addEurder(request, customer)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(eurderRepository);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void invalidAmountDoesNotSave(int amount) {
        // lenient: implementation may validate before or after looking up the item.
        lenient().when(items.findById("item-1")).thenReturn(Optional.of(item(10, "89.95")));
        assertThatThrownBy(() -> service.addEurder(request(amount), customer)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(eurderRepository);
    }

    @Test
    void emptyRequestDoesNotSave() {
        assertThatThrownBy(() -> service.addEurder(new CreateEurderDto(List.of()), customer))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(eurderRepository);
    }

    @Test
    void laterCatalogPriceDoesNotChangeEarlierEurder() {
        when(items.findById("item-1")).thenReturn(Optional.of(item(10, "10.00")), Optional.of(item(10, "20.00")));
        when(eurderRepository.addEurder(any(Eurder.class))).thenAnswer(call -> call.getArgument(0));
        var first = service.addEurder(request(2), customer);
        var second = service.addEurder(request(2), customer);
        var captor = ArgumentCaptor.forClass(Eurder.class);
        verify(eurderRepository, times(2)).addEurder(captor.capture());
        assertThat(captor.getAllValues().get(0).calculateTotalPrice()).isEqualByComparingTo("20.00");
        assertThat(first.getTotalPrice()).isEqualByComparingTo("20.00");
        assertThat(second.getTotalPrice()).isEqualByComparingTo("40.00");
    }
}
