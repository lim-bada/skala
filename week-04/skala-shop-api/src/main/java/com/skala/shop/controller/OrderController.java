package com.skala.shop.controller;

import com.skala.shop.common.ApiResponse;
import com.skala.shop.dto.OrderHistoryResponse;
import com.skala.shop.dto.OrderRequest;
import com.skala.shop.dto.OrderResultResponse;
import com.skala.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "주문 관리", description = "상품 주문·취소 및 주문 이력 API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    @Operation(summary = "상품 주문", description = "포인트·재고·주문 수량·주문 이력을 하나의 트랜잭션으로 처리합니다")
    public ApiResponse<OrderResultResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.success("상품 주문을 처리했습니다", orderService.placeOrder(request));
    }

    @PostMapping("/cancel")
    @Operation(summary = "주문 취소", description = "주문 수량 감소·포인트 환급·재고 복구를 하나의 트랜잭션으로 처리합니다")
    public ApiResponse<OrderResultResponse> cancelOrder(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.success("주문 취소를 처리했습니다", orderService.cancelOrder(request));
    }

    @GetMapping("/{customerId}/order-history")
    @Operation(summary = "고객 주문 이력 조회", description = "추천 계산에 사용되는 주문·취소 이력을 최신순으로 조회합니다")
    public ApiResponse<List<OrderHistoryResponse>> getOrderHistory(
            @PathVariable String customerId) {
        return ApiResponse.success("주문 이력을 조회했습니다", orderService.getOrderHistory(customerId));
    }
}
