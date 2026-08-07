package com.skala.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skala.shop.dto.CustomerCreateRequest;
import com.skala.shop.dto.OrderRequest;
import com.skala.shop.dto.OrderResultResponse;
import com.skala.shop.entity.Customer;
import com.skala.shop.entity.OrderType;
import com.skala.shop.entity.Product;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;
import com.skala.shop.repository.CustomerRepository;
import com.skala.shop.repository.OrderHistoryRepository;
import com.skala.shop.repository.OrderItemRepository;
import com.skala.shop.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderServiceTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Test
    void orderChangesPointStockQuantityAndHistoryTogether() {
        createCustomer("order-user");

        OrderResultResponse result = orderService.placeOrder(orderRequest("order-user", 1L, 2));
        Customer customer = customerRepository.findById("order-user").orElseThrow();
        Product product = productRepository.findById(1L).orElseThrow();

        assertThat(result.getRemainingPoint()).isEqualTo(970_000L);
        assertThat(customer.getCustomerPoint()).isEqualTo(970_000L);
        assertThat(product.getStockQuantity()).isEqualTo(28);
        assertThat(orderItemRepository.findByCustomerAndProduct(customer, product).orElseThrow().getQuantity())
                .isEqualTo(2);
        assertThat(orderHistoryRepository.findByCustomerCustomerIdOrderByCreatedAtDesc("order-user"))
                .extracting("type")
                .containsExactly(OrderType.ORDER);
    }

    @Test
    void repeatedOrderAccumulatesQuantity() {
        createCustomer("repeat-user");
        orderService.placeOrder(orderRequest("repeat-user", 1L, 2));

        OrderResultResponse result = orderService.placeOrder(orderRequest("repeat-user", 1L, 1));

        assertThat(result.getCurrentOrderQuantity()).isEqualTo(3);
        assertThat(result.getRemainingPoint()).isEqualTo(955_000L);
        assertThat(result.getRemainingStock()).isEqualTo(27);
        assertThat(orderHistoryRepository.findByCustomerCustomerIdOrderByCreatedAtDesc("repeat-user"))
                .hasSize(2);
    }

    @Test
    void partialAndFullCancellationRefundPointAndRestoreStock() {
        createCustomer("cancel-user");
        orderService.placeOrder(orderRequest("cancel-user", 1L, 2));

        OrderResultResponse partial = orderService.cancelOrder(orderRequest("cancel-user", 1L, 1));
        OrderResultResponse full = orderService.cancelOrder(orderRequest("cancel-user", 1L, 1));
        Customer customer = customerRepository.findById("cancel-user").orElseThrow();
        Product product = productRepository.findById(1L).orElseThrow();

        assertThat(partial.getCurrentOrderQuantity()).isEqualTo(1);
        assertThat(full.getCurrentOrderQuantity()).isZero();
        assertThat(customer.getCustomerPoint()).isEqualTo(1_000_000L);
        assertThat(product.getStockQuantity()).isEqualTo(30);
        assertThat(orderItemRepository.findByCustomerAndProduct(customer, product)).isEmpty();
        assertThat(orderHistoryRepository.findByCustomerCustomerIdOrderByCreatedAtDesc("cancel-user"))
                .extracting("type")
                .containsExactly(OrderType.CANCEL, OrderType.CANCEL, OrderType.ORDER);
    }

    @Test
    void insufficientPointDoesNotChangeData() {
        createCustomer("poor-user");
        Customer customer = customerRepository.findById("poor-user").orElseThrow();
        customer.setCustomerPoint(10_000L);
        Product product = productRepository.findById(1L).orElseThrow();
        int stockBefore = product.getStockQuantity();

        assertThatThrownBy(() -> orderService.placeOrder(orderRequest("poor-user", 1L, 1)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_FUNDS);

        assertThat(product.getStockQuantity()).isEqualTo(stockBefore);
        assertThat(orderHistoryRepository.findByCustomerCustomerIdOrderByCreatedAtDesc("poor-user"))
                .isEmpty();
    }

    @Test
    void insufficientStockDoesNotChangeData() {
        createCustomer("stock-user");
        Customer customer = customerRepository.findById("stock-user").orElseThrow();
        long pointBefore = customer.getCustomerPoint();

        assertThatThrownBy(() -> orderService.placeOrder(orderRequest("stock-user", 1L, 31)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);

        assertThat(customer.getCustomerPoint()).isEqualTo(pointBefore);
        assertThat(orderHistoryRepository.findByCustomerCustomerIdOrderByCreatedAtDesc("stock-user"))
                .isEmpty();
    }

    private void createCustomer(String customerId) {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerId(customerId);
        request.setCustomerPassword("password");
        customerService.createCustomer(request);
    }

    private OrderRequest orderRequest(String customerId, Long productId, Integer quantity) {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(customerId);
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }
}
