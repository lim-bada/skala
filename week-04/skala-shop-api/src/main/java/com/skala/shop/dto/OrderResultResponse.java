package com.skala.shop.dto;

import com.skala.shop.entity.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderResultResponse {

    private String message;
    private String customerId;
    private Long remainingPoint;
    private Long productId;
    private String productName;
    private OrderType type;
    private Integer processedQuantity;
    private Integer currentOrderQuantity;
    private Integer remainingStock;
    private Long totalAmount;
    private Long productVersion;
}
