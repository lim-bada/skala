package com.skala.shop.controller;

import com.skala.shop.common.ApiResponse;
import com.skala.shop.dto.RecommendationResponse;
import com.skala.shop.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Validated
@Tag(name = "개인화 추천", description = "구매 이력 기반 개인화 상품 추천 API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/{customerId}/recommendations")
    @Operation(summary = "개인화 상품 추천", description = "카테고리·가격·인기도·재고를 점수화하여 고객별 상품을 추천합니다")
    public ApiResponse<List<RecommendationResponse>> recommendProducts(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "5")
                    @Min(value = 1, message = "추천 개수는 1 이상이어야 합니다")
                    @Max(value = 20, message = "추천 개수는 20 이하여야 합니다")
                    int limit) {
        return ApiResponse.success(
                "개인화 상품을 추천했습니다",
                recommendationService.recommendProducts(customerId, limit));
    }
}
