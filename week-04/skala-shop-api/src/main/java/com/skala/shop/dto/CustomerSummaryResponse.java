package com.skala.shop.dto;

import com.skala.shop.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CustomerSummaryResponse {

    private String customerId;
    private Long customerPoint;

    public static CustomerSummaryResponse from(Customer customer) {
        return CustomerSummaryResponse.builder()
                .customerId(customer.getCustomerId())
                .customerPoint(customer.getCustomerPoint())
                .build();
    }
}
