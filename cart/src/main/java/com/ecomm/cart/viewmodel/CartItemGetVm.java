package com.ecomm.cart.viewmodel;

import lombok.Builder;

@Builder
public record CartItemGetVm(
  String customerId,
  Long productId,
  Integer quantity
) {}