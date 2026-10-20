package com.vavedin.euder.feature.item.api;

import com.vavedin.euder.feature.item.api.dto.CreateItemDto;
import com.vavedin.euder.feature.item.api.dto.ItemDto;
import com.vavedin.euder.feature.item.service.ItemService;
import com.vavedin.euder.feature.security.SecurityService;
import com.vavedin.euder.feature.security.exceptions.FeatureAccessDeniedException;
import com.vavedin.euder.feature.security.exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.vavedin.euder.feature.security.enums.Feature.ADD_ITEM;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemControllerTest {

    private ItemService itemService;
    private SecurityService securityService;
    private ItemController itemController;
    private CreateItemDto request;
    private ItemDto expected;

    @BeforeEach
    void setup() {
        securityService = mock(SecurityService.class);
        itemService = mock(ItemService.class);
        itemController = new ItemController(itemService, securityService);

        request = new CreateItemDto(
                "Coffee machine",
                "Makes excellent coffee",
                new BigDecimal("89.95"),
                12
        );

        expected = new ItemDto(
                "Coffee",
                "Good coffee",
                new BigDecimal("12.50"),
                4,
                "item-id");

    }

    @Test
    void addItem_checksAccessBeforeCallingService_andReturnsResponse() {
        when(itemService.addItem(request)).thenReturn(expected);
        assertThat(itemController.addItem("Basic credentials", request)).isSameAs(expected);
        var order = inOrder(securityService, itemService);
        order.verify(securityService).validateAuthorization("Basic credentials", ADD_ITEM);
        order.verify(itemService).addItem(request);
    }
    @Test
    void addItem_unauthenticated_doesNotCallService() {
        when(securityService.validateAuthorization(null, ADD_ITEM)).thenThrow(new UnauthorizedException());
        assertThatThrownBy(() -> itemController.addItem(null, request)).isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(itemService);
    }
    @Test
    void addItem_forbidden_doesNotCallService() {
        when(securityService.validateAuthorization("Basic member", ADD_ITEM))
                .thenThrow(new FeatureAccessDeniedException("member@example.com", ADD_ITEM));
        assertThatThrownBy(() -> itemController.addItem("Basic member", request))
                .isInstanceOf(FeatureAccessDeniedException.class);
        verifyNoInteractions(itemService);
    }


}