package com.skala.shop.dto;

import com.skala.shop.entity.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PopularProductResponse {

    private Integer rank;
    private Long productId;
    private String productName;
    private ProductCategory category;
    private Long productPrice;
    private Integer stockQuantity;
    private Long orderedQuantity;
}
