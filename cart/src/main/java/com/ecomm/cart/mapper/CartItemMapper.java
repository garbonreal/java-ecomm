package com.ecomm.cart.mapper;

import com.ecomm.cart.model.CartItem;
import com.ecomm.cart.viewmodel.CartItemGetVm;

import java.util.List;
import org.springframework.stereotype.Component;


@Component
public class CartItemMapper {
    public CartItemGetVm toGetVm(CartItem cartItem){
        return CartItemGetVm
            .builder()
            .customerId(cartItem.getCustomerId())
            .productId(cartItem.getProductId())
            .quantity(cartItem.getQuantity())
            .build();
    }

    public List<CartItemGetVm> toGetVms (List<CartItem> cartItems) {
        return cartItems
            .stream()
            .map(this::toGetVm)
            .toList();
    }
}