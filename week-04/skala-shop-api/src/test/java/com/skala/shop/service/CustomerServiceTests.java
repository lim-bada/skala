package com.skala.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skala.shop.dto.CustomerCreateRequest;
import com.skala.shop.dto.CustomerLoginRequest;
import com.skala.shop.dto.CustomerPointUpdateRequest;
import com.skala.shop.dto.CustomerResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CustomerServiceTests {

    @Autowired
    private CustomerService customerService;

    @Test
    void customerReceivesInitialPointAndPasswordIsNotExposed() {
        CustomerResponse response = customerService.createCustomer(createRequest("skala01"));

        assertThat(response.getCustomerId()).isEqualTo("skala01");
        assertThat(response.getCustomerPoint()).isEqualTo(1_000_000L);
        assertThat(response.getProducts()).isEmpty();
    }

    @Test
    void duplicateCustomerIdIsRejected() {
        customerService.createCustomer(createRequest("duplicate"));

        assertThatThrownBy(() -> customerService.createCustomer(createRequest("duplicate")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DATA_DUPLICATED);
    }

    @Test
    void loginChecksPassword() {
        customerService.createCustomer(createRequest("login-user"));
        CustomerLoginRequest correct = loginRequest("login-user", "password");
        CustomerLoginRequest wrong = loginRequest("login-user", "wrong-password");

        assertThat(customerService.loginCustomer(correct).getCustomerId()).isEqualTo("login-user");
        assertThatThrownBy(() -> customerService.loginCustomer(wrong))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_AUTHENTICATED);
    }

    @Test
    void customerPointCanBeUpdatedAndCustomerCanBeDeleted() {
        customerService.createCustomer(createRequest("update-user"));
        CustomerPointUpdateRequest updateRequest = new CustomerPointUpdateRequest();
        updateRequest.setCustomerPoint(500_000L);

        CustomerResponse updated = customerService.updateCustomerPoint("update-user", updateRequest);
        assertThat(updated.getCustomerPoint()).isEqualTo(500_000L);

        customerService.deleteCustomer("update-user");
        assertThatThrownBy(() -> customerService.getCustomerById("update-user"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DATA_NOT_FOUND);
    }

    private CustomerCreateRequest createRequest(String customerId) {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerId(customerId);
        request.setCustomerPassword("password");
        return request;
    }

    private CustomerLoginRequest loginRequest(String customerId, String password) {
        CustomerLoginRequest request = new CustomerLoginRequest();
        request.setCustomerId(customerId);
        request.setCustomerPassword(password);
        return request;
    }
}
