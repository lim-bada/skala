package com.skala.shop.service;

import com.skala.shop.dto.OrderHistoryResponse;
import com.skala.shop.dto.OrderRequest;
import com.skala.shop.dto.OrderResultResponse;
import com.skala.shop.entity.Customer;
import com.skala.shop.entity.OrderHistory;
import com.skala.shop.entity.OrderItem;
import com.skala.shop.entity.OrderType;
import com.skala.shop.entity.Product;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;
import com.skala.shop.exception.ParameterException;
import com.skala.shop.repository.CustomerRepository;
import com.skala.shop.repository.OrderHistoryRepository;
import com.skala.shop.repository.OrderItemRepository;
import com.skala.shop.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    @Transactional
    public OrderResultResponse placeOrder(OrderRequest request) {
        Customer customer = findCustomer(request.getCustomerId());
        Product product = findProduct(request.getProductId());
        long totalAmount = calculateAmount(product.getProductPrice(), request.getQuantity());

        if (customer.getCustomerPoint() < totalAmount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FUNDS);
        }
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException(
                    ErrorCode.INSUFFICIENT_STOCK,
                    "상품 재고가 부족합니다. 현재 재고: " + product.getStockQuantity());
        }

        OrderItem orderItem = orderItemRepository.findByCustomerAndProduct(customer, product)
                .orElseGet(() -> new OrderItem(customer, product, 0));
        try {
            orderItem.setQuantity(Math.addExact(orderItem.getQuantity(), request.getQuantity()));
        } catch (ArithmeticException exception) {
            throw new ParameterException("주문 수량이 허용 범위를 초과했습니다");
        }

        customer.setCustomerPoint(customer.getCustomerPoint() - totalAmount);
        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        orderItemRepository.save(orderItem);
        orderHistoryRepository.save(new OrderHistory(
                customer,
                product,
                OrderType.ORDER,
                request.getQuantity(),
                product.getProductPrice()));
        productRepository.saveAndFlush(product);

        return orderResult(
                "상품 주문이 완료되었습니다",
                customer,
                product,
                OrderType.ORDER,
                request.getQuantity(),
                orderItem.getQuantity(),
                totalAmount);
    }

    @Transactional
    public OrderResultResponse cancelOrder(OrderRequest request) {
        Customer customer = findCustomer(request.getCustomerId());
        Product product = findProduct(request.getProductId());
        OrderItem orderItem = orderItemRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INSUFFICIENT_QUANTITY,
                        "취소할 주문 상품이 없습니다"));

        if (orderItem.getQuantity() < request.getQuantity()) {
            throw new BusinessException(
                    ErrorCode.INSUFFICIENT_QUANTITY,
                    "주문 수량보다 많이 취소할 수 없습니다. 현재 주문 수량: " + orderItem.getQuantity());
        }

        long refundAmount = calculateAmount(product.getProductPrice(), request.getQuantity());
        int remainingQuantity = orderItem.getQuantity() - request.getQuantity();
        try {
            customer.setCustomerPoint(Math.addExact(customer.getCustomerPoint(), refundAmount));
            product.setStockQuantity(Math.addExact(product.getStockQuantity(), request.getQuantity()));
        } catch (ArithmeticException exception) {
            throw new ParameterException("포인트 또는 재고 수량이 허용 범위를 초과했습니다");
        }

        if (remainingQuantity == 0) {
            orderItemRepository.delete(orderItem);
        } else {
            orderItem.setQuantity(remainingQuantity);
        }

        orderHistoryRepository.save(new OrderHistory(
                customer,
                product,
                OrderType.CANCEL,
                request.getQuantity(),
                product.getProductPrice()));
        productRepository.saveAndFlush(product);

        return orderResult(
                "주문 취소가 완료되었습니다",
                customer,
                product,
                OrderType.CANCEL,
                request.getQuantity(),
                remainingQuantity,
                refundAmount);
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> getOrderHistory(String customerId) {
        findCustomer(customerId);
        return orderHistoryRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(OrderHistoryResponse::from)
                .toList();
    }

    private Customer findCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND,
                        "고객을 찾을 수 없습니다: " + customerId));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND,
                        "상품을 찾을 수 없습니다: " + productId));
    }

    private long calculateAmount(Long unitPrice, Integer quantity) {
        try {
            return Math.multiplyExact(unitPrice, quantity.longValue());
        } catch (ArithmeticException exception) {
            throw new ParameterException("주문 금액이 허용 범위를 초과했습니다");
        }
    }

    private OrderResultResponse orderResult(
            String message,
            Customer customer,
            Product product,
            OrderType type,
            Integer processedQuantity,
            Integer currentOrderQuantity,
            Long totalAmount) {
        return OrderResultResponse.builder()
                .message(message)
                .customerId(customer.getCustomerId())
                .remainingPoint(customer.getCustomerPoint())
                .productId(product.getId())
                .productName(product.getProductName())
                .type(type)
                .processedQuantity(processedQuantity)
                .currentOrderQuantity(currentOrderQuantity)
                .remainingStock(product.getStockQuantity())
                .totalAmount(totalAmount)
                .productVersion(product.getVersion())
                .build();
    }
}
