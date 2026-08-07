package com.skala.shop.dto;

import com.skala.shop.entity.Product;
import com.skala.shop.entity.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String productName;
    private Long productPrice;
    private ProductCategory category;
    private Integer stockQuantity;
    private Long version;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .version(product.getVersion())
                .build();
    }
}
