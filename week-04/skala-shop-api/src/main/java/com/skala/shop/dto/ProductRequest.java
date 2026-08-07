package com.skala.shop.dto;

import com.skala.shop.entity.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequest {

    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 100, message = "상품명은 100자 이하여야 합니다")
    private String productName;

    @NotNull(message = "상품 가격은 필수입니다")
    @Positive(message = "상품 가격은 1원 이상이어야 합니다")
    private Long productPrice;

    @NotNull(message = "상품 카테고리는 필수입니다")
    private ProductCategory category;

    @NotNull(message = "상품 재고는 필수입니다")
    @PositiveOrZero(message = "상품 재고는 0개 이상이어야 합니다")
    private Integer stockQuantity;
}
