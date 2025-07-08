package com.ecomm.cart.service;

import com.ecomm.cart.mapper.CartItemMapper;
import com.ecomm.cart.model.CartItem;
import com.ecomm.cart.repository.CartItemRepository;
import com.ecomm.cart.viewmodel.CartItemGetVm;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;

    public List<CartItemGetVm> getCartItems() {
        // String currentUserId = AuthenticationUtils.extractUserId();
        String currentUserId = "123";
        List<CartItem> cartItems = cartItemRepository.findByCustomerId(currentUserId);
        return cartItemMapper.toGetVms(cartItems);        
    }

}