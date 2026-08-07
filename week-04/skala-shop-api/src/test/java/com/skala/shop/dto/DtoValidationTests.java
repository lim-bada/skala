package com.skala.shop.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.skala.shop.entity.ProductCategory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DtoValidationTests {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void productRequestRejectsBlankNameAndNegativeStock() {
        ProductRequest request = new ProductRequest();
        request.setProductName(" ");
        request.setProductPrice(15_000L);
        request.setCategory(ProductCategory.ELECTRONICS);
        request.setStockQuantity(-1);

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("productName", "stockQuantity");
    }

    @Test
    void orderRequestRejectsZeroQuantity() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId("skala01");
        request.setProductId(1L);
        request.setQuantity(0);

        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("quantity");
    }
}
