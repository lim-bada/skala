package com.skala.shop.dto;

import com.skala.shop.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemResponse {

    private Long productId;
    private String productName;
    private Long productPrice;
    private Integer quantity;
    private Long totalPrice;

    public static OrderItemResponse from(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getProductName())
                .productPrice(orderItem.getProduct().getProductPrice())
                .quantity(orderItem.getQuantity())
                .totalPrice(orderItem.getProduct().getProductPrice() * orderItem.getQuantity())
                .build();
    }
}
