package com.ecomm.cart.repository;

import com.ecomm.cart.model.CartItem;
import com.ecomm.cart.model.CartItemId;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    private CartItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new CartItem();
        testItem.setCustomerId("customer-123");
        testItem.setProductId(1001L);
        testItem.setQuantity(2);
        cartItemRepository.save(testItem);
    }

    @Test
    void testFindByCustomerIdAndProductId() {
        Optional<CartItem> result = cartItemRepository.findByCustomerIdAndProductId("customer-123", 1001L);
        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    @Test
    void testFindByCustomerIdAndProductIdIn() {
        List<CartItem> results = cartItemRepository.findByCustomerIdAndProductIdIn("customer-123", Arrays.asList(1001L, 1002L));
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getProductId()).isEqualTo(1001L);
    }

    @Test
    void testDeleteByCustomerIdAndProductId() {
        cartItemRepository.deleteByCustomerIdAndProductId("customer-123", 1001L);
        Optional<CartItem> result = cartItemRepository.findByCustomerIdAndProductId("customer-123", 1001L);
        assertThat(result).isNotPresent();
    }
}
