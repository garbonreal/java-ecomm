package com.ecomm.cart.repository;

import com.ecomm.cart.model.CartItem;
import com.ecomm.cart.model.CartItemId;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;


public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
    /**
     * Retrieves a cart item for a specific customer and product, locking the record to prevent concurrent
     * modifications
     */
    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    // @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
    @Query("SELECT c From CartItem c WHERE c.customerId = :customerId AND c.productId = :productId")
    Optional<CartItem> findByCustomerIdAndProductId(String customerId, Long productId);

    /**
     * Retrieves a list of cart items for a specific customer, locking the records to prevent concurrent modifications.
     */
    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    // @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
    @Query("SELECT c FROM CartItem c WHERE c.customerId = :customerId AND c.productId IN :productIds")
    List<CartItem> findByCustomerIdAndProductIdIn(String customerId, List<Long> productIds);

    List<CartItem> findByCustomerId(String customerId);

    void deleteByCustomerIdAndProductId(String customerId, Long productId);
}