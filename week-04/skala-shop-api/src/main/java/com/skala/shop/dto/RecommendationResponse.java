package com.skala.shop.dto;

import com.skala.shop.entity.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RecommendationResponse {

    private Integer rank;
    private Long productId;
    private String productName;
    private ProductCategory category;
    private Long productPrice;
    private Integer stockQuantity;
    private Double recommendationScore;
    private String reason;

    public RecommendationResponse withRank(int newRank) {
        return RecommendationResponse.builder()
                .rank(newRank)
                .productId(productId)
                .productName(productName)
                .category(category)
                .productPrice(productPrice)
                .stockQuantity(stockQuantity)
                .recommendationScore(recommendationScore)
                .reason(reason)
                .build();
    }
}
