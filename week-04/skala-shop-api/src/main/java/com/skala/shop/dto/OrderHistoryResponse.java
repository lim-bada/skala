package com.skala.shop.dto;

import com.skala.shop.entity.OrderHistory;
import com.skala.shop.entity.OrderType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderHistoryResponse {

    private Long id;
    private Long productId;
    private String productName;
    private OrderType type;
    private Integer quantity;
    private Long unitPrice;
    private Long totalAmount;
    private LocalDateTime createdAt;

    public static OrderHistoryResponse from(OrderHistory history) {
        return OrderHistoryResponse.builder()
                .id(history.getId())
                .productId(history.getProduct().getId())
                .productName(history.getProduct().getProductName())
                .type(history.getType())
                .quantity(history.getQuantity())
                .unitPrice(history.getUnitPrice())
                .totalAmount(history.getTotalAmount())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
