package com.ecomm.cart.controller;

import com.ecomm.cart.service.CartItemService;
import com.ecomm.cart.viewmodel.CartItemGetVm;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequiredArgsConstructor
public class CartItemController {
    private final CartItemService cartItemService;

    @GetMapping("/storefront/cart/items")
    public ResponseEntity<List<CartItemGetVm>> getCartItems () {
        List<CartItemGetVm> cartItemGetVms = cartItemService.getCartItems();
        return ResponseEntity.ok(cartItemGetVms);
    }
}
