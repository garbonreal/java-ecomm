package com.ecomm.cart.service;

import com.ecomm.cart.mapper.CartItemMapper;
import com.ecomm.cart.model.CartItem;
import com.ecomm.cart.repository.CartItemRepository;
import com.ecomm.cart.viewmodel.CartItemDeleteVm;
import com.ecomm.cart.viewmodel.CartItemGetVm;
import com.ecomm.cart.viewmodel.CartItemPostVm;
import com.ecomm.cart.viewmodel.CartItemPutVm;
import com.ecomm.cart.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;

    @Transactional
    public CartItemGetVm addCartItem(CartItemPostVm cartItemPostVm) {
        // validateProduct(cartItemPostVm.productId());

        // String currentUserId = AuthenticationUtils.extractUserId();
        String currentUserId = "123";
        CartItem cartItem = performAddCartItem(cartItemPostVm, currentUserId);

        return cartItemMapper.toGetVm(cartItem);
    }

    @Transactional
    public CartItemGetVm updateCartItem(Long productId, CartItemPutVm cartItemPutVm) {
        // validateProduct(productId);

        // String currentUserId = AuthenticationUtils.extractUserId();
        String currentUserId = "123";
        CartItem cartItem = cartItemMapper.toCartItem(currentUserId, productId, cartItemPutVm.quantity());

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return cartItemMapper.toGetVm(savedCartItem);
    }

    public List<CartItemGetVm> getCartItems() {
        // String currentUserId = AuthenticationUtils.extractUserId();
        String currentUserId = "123";
        List<CartItem> cartItems = cartItemRepository.findByCustomerId(currentUserId);
        return cartItemMapper.toGetVms(cartItems);        
    }

    @Transactional
    public List<CartItemGetVm> deleteOrAdjustCartItem(List<CartItemDeleteVm> cartItemDeleteVms) {

        Map<Long, CartItem> cartItemById = getCartItemsByProductIds(cartItemDeleteVms);

        List<CartItem> cartItemsToDelete = new ArrayList<>();
        List<CartItem> cartItemsToAdjust = new ArrayList<>();

        for (CartItemDeleteVm cartItemDeleteVm : cartItemDeleteVms) {
            Optional<CartItem> optionalCartItem = Optional.ofNullable(cartItemById.get(cartItemDeleteVm.productId()));
            optionalCartItem.ifPresent(cartItem -> {
                if (cartItem.getQuantity() <= cartItemDeleteVm.quantity()) {
                    cartItemsToDelete.add(cartItem);
                } else {
                    cartItem.setQuantity(cartItem.getQuantity() - cartItemDeleteVm.quantity());
                    cartItemsToAdjust.add(cartItem);
                }
            });
        }

        cartItemRepository.deleteAll(cartItemsToDelete);
        List<CartItem> updatedCartItems = cartItemRepository.saveAll(cartItemsToAdjust);

        return cartItemMapper.toGetVms(updatedCartItems);
    }

    @Transactional
    public void deleteCartItem(Long productId) {
        // String currentUserId = AuthenticationUtils.extractUserId();
        String currentUserId = "123";
        cartItemRepository.deleteByCustomerIdAndProductId(currentUserId, productId);
    }

    private CartItem performAddCartItem(CartItemPostVm cartItemPostVm, String currentUserId) {
        return cartItemRepository.findByCustomerIdAndProductId(currentUserId, cartItemPostVm.productId())
            .map(existingCartItem -> updateExistingCartItem(cartItemPostVm, existingCartItem))
            .orElseGet(() -> createNewCartItem(cartItemPostVm, currentUserId));
    }

    private CartItem createNewCartItem(CartItemPostVm cartItemPostVm, String currentUserId) {
        CartItem cartItem = cartItemMapper.toCartItem(cartItemPostVm, currentUserId);
        return cartItemRepository.save(cartItem);
    }

    private CartItem updateExistingCartItem(CartItemPostVm cartItemPostVm, CartItem existingCartItem) {
        existingCartItem.setQuantity(existingCartItem.getQuantity() + cartItemPostVm.quantity());
        return cartItemRepository.save(existingCartItem);
    }

    private Map<Long, CartItem> getCartItemsByProductIds(List<CartItemDeleteVm> cartItemDeleteVms) {
        // String currentUserId = AuthenticationUtils.extractUserId();
        String currentUserId = "123";
        List<Long> productIds = cartItemDeleteVms
            .stream()
            .map(CartItemDeleteVm::productId)
            .toList();
        List<CartItem> cartItems = cartItemRepository.findByCustomerIdAndProductIdIn(currentUserId, productIds);
        return cartItems
            .stream()
            .collect(Collectors.toMap(CartItem::getProductId, Function.identity()));
    }

    // private void validateProduct(Long productId) {
    //     if (!productService.existsById(productId)) {
    //         throw new NotFoundException(Constants.ErrorCode.NOT_FOUND_PRODUCT, productId);
    //     }
    // }

}