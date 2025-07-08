package com.ecomm.cart.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@lombok.Getter
@lombok.Setter
@EqualsAndHashCode
public class CartItemId {
  private String customerId;
  private Long productId;
}