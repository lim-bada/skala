package com.skala.shop.service;

import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.CustomerCreateRequest;
import com.skala.shop.dto.CustomerLoginRequest;
import com.skala.shop.dto.CustomerPointUpdateRequest;
import com.skala.shop.dto.CustomerResponse;
import com.skala.shop.dto.CustomerSummaryResponse;
import com.skala.shop.dto.OrderItemResponse;
import com.skala.shop.entity.Customer;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;
import com.skala.shop.repository.CustomerRepository;
import com.skala.shop.repository.OrderHistoryRepository;
import com.skala.shop.repository.OrderItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final long INITIAL_POINT = 1_000_000L;

    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<CustomerSummaryResponse> getAllCustomers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("customerId").ascending());
        Page<CustomerSummaryResponse> result =
                customerRepository.findAll(pageRequest).map(CustomerSummaryResponse::from);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(String customerId) {
        Customer customer = findCustomer(customerId);
        List<OrderItemResponse> products = orderItemRepository
                .findByCustomerCustomerId(customerId)
                .stream()
                .map(OrderItemResponse::from)
                .toList();
        return CustomerResponse.from(customer, products);
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        if (customerRepository.existsById(request.getCustomerId())) {
            throw new BusinessException(
                    ErrorCode.DATA_DUPLICATED,
                    "이미 가입된 고객 ID입니다: " + request.getCustomerId());
        }

        Customer customer = new Customer(
                request.getCustomerId(),
                request.getCustomerPassword(),
                INITIAL_POINT);
        return CustomerResponse.from(customerRepository.save(customer), List.of());
    }

    @Transactional(readOnly = true)
    public CustomerResponse loginCustomer(CustomerLoginRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_AUTHENTICATED));

        if (!customer.getCustomerPassword().equals(request.getCustomerPassword())) {
            throw new BusinessException(ErrorCode.NOT_AUTHENTICATED);
        }

        List<OrderItemResponse> products = orderItemRepository
                .findByCustomerCustomerId(customer.getCustomerId())
                .stream()
                .map(OrderItemResponse::from)
                .toList();
        return CustomerResponse.from(customer, products);
    }

    @Transactional
    public CustomerResponse updateCustomerPoint(
            String customerId,
            CustomerPointUpdateRequest request) {
        Customer customer = findCustomer(customerId);
        customer.setCustomerPoint(request.getCustomerPoint());

        List<OrderItemResponse> products = orderItemRepository
                .findByCustomerCustomerId(customerId)
                .stream()
                .map(OrderItemResponse::from)
                .toList();
        return CustomerResponse.from(customer, products);
    }

    @Transactional
    public void deleteCustomer(String customerId) {
        Customer customer = findCustomer(customerId);
        if (orderItemRepository.existsByCustomer(customer)
                || orderHistoryRepository.existsByCustomer(customer)) {
            throw new BusinessException(
                    ErrorCode.DATA_IN_USE,
                    "주문 정보가 있는 고객은 삭제할 수 없습니다");
        }
        customerRepository.delete(customer);
    }

    private Customer findCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND,
                        "고객을 찾을 수 없습니다: " + customerId));
    }
}
