package com.ecomm.cart.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.ecomm.cart.model.CartItem;
import com.ecomm.cart.repository.CartItemRepository;
import com.ecomm.cart.viewmodel.CartItemDeleteVm;
import com.ecomm.cart.viewmodel.CartItemPostVm;
import com.ecomm.cart.viewmodel.CartItemPutVm;
import com.ecomm.cart.viewmodel.ProductThumbnailVm;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import static io.restassured.RestAssured.given;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartItemControllerIT { 

    @Autowired
    private CartItemRepository cartItemRepository;

    private ProductThumbnailVm existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = ProductThumbnailVm
            .builder()
            .id(Long.MIN_VALUE)
            .name("product-name")
            .slug("product-slug")
            .thumbnailUrl("thumbnail-url")
            .build();
    }

    @AfterEach
    void tearDown() {
        cartItemRepository.deleteAll();
    }

    @Nested
    class AddCartItemTest {

        @Test
        void testAddCartItem_whenRequestIsValid_shouldReturnCartItemGetVm() {
            CartItemPostVm cartItemPostVm = new CartItemPostVm(existingProduct.id(), 1);

            performCreateCartItemThenExpectSuccess(cartItemPostVm)
                .body("productId", is(cartItemPostVm.productId()))
                .body("quantity", equalTo(cartItemPostVm.quantity()))
                .log().ifValidationFails();
        }

        @Test
        void testAddCartItem_whenCartItemExists_shouldUpdateCartItemQuantity() {
            CartItemPostVm addCartItemVm = CartItemPostVm
                .builder()
                .productId(existingProduct.id())
                .quantity(1)
                .build();
            performCreateCartItemThenExpectSuccess(addCartItemVm);

            CartItemPostVm addDuplicatedCartItemVm = CartItemPostVm
                .builder()
                .productId(addCartItemVm.productId())
                .quantity(1)
                .build();
            int expectedQuantity = addCartItemVm.quantity() + addDuplicatedCartItemVm.quantity();

            performCreateCartItemThenExpectSuccess(addDuplicatedCartItemVm)
                .body("productId", equalTo(addDuplicatedCartItemVm.productId()))
                .body("quantity", equalTo(expectedQuantity))
                .log().ifValidationFails();
        }
    }

    @Nested
    class UpdateCartItemTest {

        @Test
        void testUpdateCartItem_whenRequestIsValid_shouldReturnCartItemGetVm() {
            CartItemPutVm cartItemPutVm = new CartItemPutVm(1);

            givenLoggedInAsAdmin()
                .body(cartItemPutVm)
                .when()
                .put("/v1/storefront/cart/items/" + existingProduct.id())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("productId", equalTo(existingProduct.id()))
                .body("quantity", equalTo(cartItemPutVm.quantity()))
                .log().ifValidationFails();
        }
    }

    @Nested
    class GetCartItemsTest {

        @Test
        void testGetCartItems_whenCartItemsExist_shouldReturnCartItems() {
            CartItemPostVm cartItemPostVm = new CartItemPostVm(existingProduct.id(), 1);

            
            performCreateCartItemThenExpectSuccess(cartItemPostVm);

            performGetCartItemsThenExpect()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1))
                .body("[0].productId", equalTo(cartItemPostVm.productId()))
                .body("[0].quantity", equalTo(cartItemPostVm.quantity()))
                .log().ifValidationFails();
        }

    }

    @Nested
    class DeleteOrAdjustCartItemTest {
        private CartItem existingCartItem;

        @BeforeEach
        void setUp() {
            CartItemPostVm cartItemPostVm = new CartItemPostVm(existingProduct.id(), 10);
            
            performCreateCartItemThenExpectSuccess(cartItemPostVm);
            existingCartItem = CartItem
                .builder()
                .productId(cartItemPostVm.productId())
                .quantity(cartItemPostVm.quantity())
                .build();
        }

        @Test
        void testDeleteOrAdjustCartItem_whenDeleteQuantityIsGreaterThanOrEqualToCartItemQuantity_shouldDeleteCartItem() {
            CartItemDeleteVm cartItemDeleteVm =
                new CartItemDeleteVm(existingCartItem.getProductId(), existingCartItem.getQuantity() + 1);

            performRemoveCartItemsThenExpect(List.of(cartItemDeleteVm))
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(0))
                .log().ifValidationFails();
        }

        @Test
        void testDeleteOrAdjustCartItem_whenDeleteQuantityIsLessThanCartItemQuantity_shouldAdjustCartItemQuantity() {
            CartItemDeleteVm cartItemDeleteVm =
                new CartItemDeleteVm(existingCartItem.getProductId(), existingCartItem.getQuantity() - 1);
            int expectedQuantity = existingCartItem.getQuantity() - cartItemDeleteVm.quantity();

            performRemoveCartItemsThenExpect(List.of(cartItemDeleteVm))
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1))
                .body("[0].productId", equalTo(existingCartItem.getProductId()))
                .body("[0].quantity", equalTo(expectedQuantity))
                .log().ifValidationFails();
        }

        private ValidatableResponse performRemoveCartItemsThenExpect(List<CartItemDeleteVm> cartItemDeleteVms) {
            return givenLoggedInAsAdmin()
                .when()
                .body(cartItemDeleteVms)
                .post("/v1/storefront/cart/items/remove")
                .then();
        }
    }

    @Nested
    class DeleteCartItemTest {

        @Test
        void testDeleteCartItem_whenCartItemExists_shouldDeleteCartItem() {
            CartItemPostVm cartItemPostVm = new CartItemPostVm(existingProduct.id(), 10);
            
            performCreateCartItemThenExpectSuccess(cartItemPostVm);

            givenLoggedInAsAdmin()
                .when()
                .delete("/v1/storefront/cart/items/" + cartItemPostVm.productId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .log().ifValidationFails();

            performGetCartItemsThenExpect()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(0))
                .log().ifValidationFails();
        }
    }

    private ValidatableResponse performGetCartItemsThenExpect() {
        return givenLoggedInAsAdmin()
            .when()
            .get("/v1/storefront/cart/items")
            .then();
    }

    private ValidatableResponse performCreateCartItemThenExpectSuccess(CartItemPostVm cartItemPostVm) {
        return givenLoggedInAsAdmin()
            .body(cartItemPostVm)
            .when()
            .post("/v1/storefront/cart/items")
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    protected RequestSpecification givenLoggedInAsAdmin() {
    return given()
        .baseUri("http://localhost:8084/");
    }
}