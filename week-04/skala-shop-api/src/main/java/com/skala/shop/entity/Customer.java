package com.skala.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    @Column(name = "customer_id", length = 50)
    private String customerId;

    @Column(name = "customer_password", nullable = false, length = 100)
    private String customerPassword;

    @Column(name = "customer_point", nullable = false)
    private Long customerPoint;

    public Customer(String customerId, String customerPassword, Long customerPoint) {
        this.customerId = customerId;
        this.customerPassword = customerPassword;
        this.customerPoint = customerPoint;
    }
}
