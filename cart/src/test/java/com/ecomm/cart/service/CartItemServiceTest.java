package com.ecomm.cart.service;

import com.ecomm.cart.mapper.CartItemMapper;
import com.ecomm.cart.model.CartItem;
import com.ecomm.cart.repository.CartItemRepository;
import com.ecomm.cart.viewmodel.CartItemDeleteVm;
import com.ecomm.cart.viewmodel.CartItemGetVm;
import com.ecomm.cart.viewmodel.CartItemPostVm;
import com.ecomm.cart.viewmodel.CartItemPutVm;
import com.github.dockerjava.api.exception.InternalServerErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
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
import org.springframework.dao.PessimisticLockingFailureException;


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
    private static final Long PRODUCT_ID_SAMPLE = 1L;

    @Nested
    class AddCartItemTest {
        private CartItemPostVm.CartItemPostVmBuilder cartItemPostVmBuilder;

        @BeforeEach
        void setUp() {
            cartItemPostVmBuilder = CartItemPostVm.builder()
                .productId(PRODUCT_ID_SAMPLE)
                .quantity(1);
        }

        @Test
        void testAddCartItem_whenCartItemExists_shouldUpdateQuantity() {
            CartItemPostVm cartItemPostVm = cartItemPostVmBuilder.build();
            CartItem existingCartItem = CartItem
                .builder()
                .customerId(CURRENT_USER_ID_SAMPLE)
                .productId(cartItemPostVm.productId())
                .quantity(1)
                .build();
            int expectedQuantity = existingCartItem.getQuantity() + cartItemPostVm.quantity();

            when(cartItemRepository.findByCustomerIdAndProductId(anyString(), anyLong())).thenReturn(
                Optional.of(existingCartItem));
            when(cartItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            CartItemGetVm cartItem = cartItemService.addCartItem(cartItemPostVm);

            verify(cartItemRepository).save(any());
            assertEquals(expectedQuantity, cartItem.quantity());
            assertEquals(CURRENT_USER_ID_SAMPLE, cartItem.customerId());
            assertEquals(cartItemPostVm.productId(), cartItem.productId());
        }

        @Test
        void testAddCartItem_whenCartItemDoesNotExist_shouldCreateCartItem() {
            CartItemPostVm cartItemPostVm = cartItemPostVmBuilder.build();

            when(cartItemRepository.findByCustomerIdAndProductId(anyString(), anyLong())).thenReturn(
                java.util.Optional.empty());
            when(cartItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            CartItemGetVm cartItem = cartItemService.addCartItem(cartItemPostVm);

            verify(cartItemRepository).save(any());
            assertEquals(CURRENT_USER_ID_SAMPLE, cartItem.customerId());
            assertEquals(cartItemPostVm.productId(), cartItem.productId());
            assertEquals(cartItemPostVm.quantity(), cartItem.quantity());
        }
    }

    @Nested
    class UpdateCartItemTest {
        private CartItemPutVm cartItemPutVm;

        @BeforeEach
        void setUp() {
            cartItemPutVm = new CartItemPutVm(1);
        }

        @Test
        void testUpdateCartItem_whenRequestIsValid_shouldReturnCartItem() {
            when(cartItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            CartItemGetVm updatedCartItem = cartItemService.updateCartItem(PRODUCT_ID_SAMPLE, cartItemPutVm);

            verify(cartItemRepository).save(any());
            assertEquals(CURRENT_USER_ID_SAMPLE, updatedCartItem.customerId());
            assertEquals(PRODUCT_ID_SAMPLE, updatedCartItem.productId());
            assertEquals(cartItemPutVm.quantity(), updatedCartItem.quantity());
        }
    }

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

            List<CartItemGetVm> cartItemGetVms = cartItemService.getCartItems();

            verify(cartItemRepository).findByCustomerId(CURRENT_USER_ID_SAMPLE);
            assertEquals(existingCartItems.size(), cartItemGetVms.size());
        }
    }

    @Nested
    class DeleteOrAdjustCartItemTest {

        @Test
        void testDeleteOrAdjustCartItem_whenDeleteQuantityGreaterThanCartItemQuantity_shouldDeleteCartItem() {
            CartItem existingCartItem = CartItem.builder()
                .customerId(CURRENT_USER_ID_SAMPLE)
                .productId(PRODUCT_ID_SAMPLE)
                .quantity(1)
                .build();
            CartItemDeleteVm cartItemDeleteVm =
                new CartItemDeleteVm(existingCartItem.getProductId(), existingCartItem.getQuantity() + 1);
            List<CartItemDeleteVm> cartItemDeleteVms = List.of(cartItemDeleteVm);

            when(cartItemRepository.findByCustomerIdAndProductIdIn(any(), any())).thenReturn(List.of(existingCartItem));

            List<CartItemGetVm> cartItemGetVms = cartItemService.deleteOrAdjustCartItem(cartItemDeleteVms);

            verify(cartItemRepository).deleteAll(List.of(existingCartItem));
            assertEquals(0, cartItemGetVms.size());
        }

        @Test
        void testDeleteOrAdjustCartItem_whenDeleteQuantityLessThanCartItemQuantity_shouldUpdateCartItem() {
            CartItemDeleteVm cartItemDeleteVm = new CartItemDeleteVm(PRODUCT_ID_SAMPLE, 1);
            CartItem existingCartItem = CartItem.builder()
                .customerId(CURRENT_USER_ID_SAMPLE)
                .productId(cartItemDeleteVm.productId())
                .quantity(cartItemDeleteVm.quantity() + 1)
                .build();
            List<CartItemDeleteVm> cartItemDeleteVms = List.of(cartItemDeleteVm);
            int expectedQuantity = existingCartItem.getQuantity() - cartItemDeleteVm.quantity();

            when(cartItemRepository.findByCustomerIdAndProductIdIn(any(), any())).thenReturn(List.of(existingCartItem));
            when(cartItemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

            List<CartItemGetVm> cartItemGetVms = cartItemService.deleteOrAdjustCartItem(cartItemDeleteVms);

            verify(cartItemRepository).saveAll(List.of(existingCartItem));
            assertEquals(1, cartItemGetVms.size());
            assertEquals(expectedQuantity, cartItemGetVms.get(0).quantity());
        }
    }
}
