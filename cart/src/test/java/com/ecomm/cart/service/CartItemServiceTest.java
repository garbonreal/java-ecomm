package com.ecomm.cart.service;

import com.ecomm.cart.mapper.CartItemMapper;
import com.ecomm.cart.model.CartItem;
import com.ecomm.cart.repository.CartItemRepository;
import com.ecomm.cart.viewmodel.CartItemGetVm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;


@ExtendWith(MockitoExtension.class)
public class CartItemServiceTest {
    @Mock
    private CartItemRepository cartItemRepository;

    @Spy
    private CartItemMapper cartItemMapper = new CartItemMapper();

    @InjectMocks
    private CartItemService cartItemService;

    @BeforeEach
    void setUp() {
        Mockito.reset(cartItemRepository);
    }

    private static final String CURRENT_USER_ID_SAMPLE = "123";

    @Nested
    class GetCartItemsTest {

        @Test
        void testGetCartItems() {
            CartItem existingCartItem = CartItem.builder()
                .customerId(CURRENT_USER_ID_SAMPLE)
                .productId(1L)
                .quantity(1)
                .build();
            List<CartItem> existingCartItems = List.of(existingCartItem);

            when(cartItemRepository.findByCustomerId(CURRENT_USER_ID_SAMPLE))
                .thenReturn(existingCartItems);
            mockCurrentUserId(CURRENT_USER_ID_SAMPLE);

            List<CartItemGetVm> cartItemGetVms = cartItemService.getCartItems();

            verify(cartItemRepository).findByCustomerId(CURRENT_USER_ID_SAMPLE);
            assertEquals(existingCartItems.size(), cartItemGetVms.size());
        }
    }

    private void mockCurrentUserId(String userIdToMock) {
        Jwt jwt = mock(Jwt.class);
        JwtAuthenticationToken jwtToken = new JwtAuthenticationToken(jwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(jwtToken);

        when(jwt.getSubject()).thenReturn(userIdToMock);
        SecurityContextHolder.setContext(securityContext);
    }
}
