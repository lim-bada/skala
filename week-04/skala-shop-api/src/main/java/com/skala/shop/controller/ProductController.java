package com.skala.shop.controller;

import com.skala.shop.common.ApiResponse;
import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.ProductRequest;
import com.skala.shop.dto.ProductResponse;
import com.skala.shop.dto.PopularProductResponse;
import com.skala.shop.dto.StockUpdateRequest;
import com.skala.shop.service.ProductService;
import com.skala.shop.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "상품 관리", description = "상품 CRUD, 페이징 및 재고 관리 API")
public class ProductController {

    private final ProductService productService;
    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "상품을 ID 오름차순으로 페이징 조회합니다")
    public ApiResponse<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "페이지는 0 이상이어야 합니다") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
                    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
                    int size) {
        return ApiResponse.success("상품 목록을 조회했습니다", productService.getAllProducts(page, size));
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 상품 순위", description = "주문 수량에서 취소 수량을 뺀 순수 주문량을 기준으로 조회합니다")
    public ApiResponse<List<PopularProductResponse>> getPopularProducts(
            @RequestParam(defaultValue = "5")
                    @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다")
                    @Max(value = 20, message = "조회 개수는 20 이하여야 합니다")
                    int limit) {
        return ApiResponse.success(
                "인기 상품을 조회했습니다",
                recommendationService.getPopularProducts(limit));
    }

    @GetMapping("/{id}")
    @Operation(summary = "상품 상세 조회")
    public ApiResponse<ProductResponse> getProductById(@PathVariable Long id) {
        return ApiResponse.success("상품을 조회했습니다", productService.getProductById(id));
    }

    @PostMapping
    @Operation(summary = "상품 등록")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("상품을 등록했습니다", product));
    }

    @PutMapping("/{id}")
    @Operation(summary = "상품 수정")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ApiResponse.success("상품을 수정했습니다", productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "상품 삭제")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success("상품을 삭제했습니다");
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "상품 재고 추가", description = "기존 상품 재고에 입력한 수량을 추가합니다")
    public ApiResponse<ProductResponse> addStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        return ApiResponse.success("상품 재고를 추가했습니다", productService.addStock(id, request));
    }
}
