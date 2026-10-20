package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.feature.eurder.api.dto.*;
import com.vavedin.euder.feature.eurder.service.EurderService;
import com.vavedin.euder.feature.security.SecurityService;
import com.vavedin.euder.feature.security.exceptions.*;
import com.vavedin.euder.feature.user.domain.User;
import org.junit.jupiter.api.*;

import static com.vavedin.euder.feature.security.enums.Feature.ORDER_ITEM;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EurderControllerTest {
    private final EurderService service = mock(EurderService.class);
    private final SecurityService security = mock(SecurityService.class);
    private final EurderController controller = new EurderController(service, security);
    private final CreateEurderDto request = mock(CreateEurderDto.class);

    @Test
    void usesAuthenticatedCustomerAndReturnsServiceResponse() {
        User customer = mock(User.class);
        EurderDto expected = mock(EurderDto.class);
        when(security.validateAuthorization("Basic credentials", ORDER_ITEM)).thenReturn(customer);
        when(service.addEurder(request, customer)).thenReturn(expected);
        assertThat(controller.addEurder("Basic credentials", request)).isSameAs(expected);
        var order = inOrder(security, service);
        order.verify(security).validateAuthorization("Basic credentials", ORDER_ITEM);
        order.verify(service).addEurder(request, customer);
    }

    @Test
    void missingCredentialsDoesNotCallService() {
        when(security.validateAuthorization(null, ORDER_ITEM)).thenThrow(new UnauthorizedException());
        assertThatThrownBy(() -> controller.addEurder(null, request)).isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(service);
    }

    @Test
    void deniedFeatureDoesNotCallService() {
        when(security.validateAuthorization("Basic denied", ORDER_ITEM))
                .thenThrow(new FeatureAccessDeniedException("test@example.com", ORDER_ITEM));
        assertThatThrownBy(() -> controller.addEurder("Basic denied", request)).isInstanceOf(FeatureAccessDeniedException.class);
        verifyNoInteractions(service);
    }
}
