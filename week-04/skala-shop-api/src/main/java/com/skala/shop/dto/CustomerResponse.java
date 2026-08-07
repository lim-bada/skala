package com.skala.shop.dto;

import com.skala.shop.entity.Customer;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CustomerResponse {

    private String customerId;
    private Long customerPoint;
    private List<OrderItemResponse> products;

    public static CustomerResponse from(Customer customer, List<OrderItemResponse> products) {
        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .customerPoint(customer.getCustomerPoint())
                .products(products)
                .build();
    }
}
