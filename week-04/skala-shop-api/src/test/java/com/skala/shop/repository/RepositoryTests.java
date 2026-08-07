package com.skala.shop.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.skala.shop.entity.Customer;
import com.skala.shop.entity.OrderItem;
import com.skala.shop.entity.OrderHistory;
import com.skala.shop.entity.OrderType;
import com.skala.shop.entity.Product;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Test
    void initialProductsAreLoaded() {
        assertThat(productRepository.count()).isEqualTo(9);
        assertThat(productRepository.findByProductName("무선마우스")).isPresent();
    }

    @Test
    void orderItemCanBeFoundByCustomerAndProduct() {
        Customer customer = customerRepository.save(
                new Customer("repository-test", "password", 1_000_000L));
        Product product = productRepository.findByProductName("무선마우스").orElseThrow();
        orderItemRepository.save(new OrderItem(customer, product, 2));

        List<OrderItem> items = orderItemRepository.findByCustomerCustomerId(customer.getCustomerId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getProduct().getProductName()).isEqualTo("무선마우스");
        assertThat(items.get(0).getQuantity()).isEqualTo(2);
        assertThat(orderItemRepository.findByCustomerAndProduct(customer, product)).isPresent();
    }

    @Test
    void orderHistoryRemainsAsAnIndependentRecord() {
        Customer customer = customerRepository.save(
                new Customer("history-test", "password", 1_000_000L));
        Product product = productRepository.findByProductName("무선마우스").orElseThrow();
        orderHistoryRepository.save(new OrderHistory(customer, product, OrderType.ORDER, 2, 15_000L));

        List<OrderHistory> histories =
                orderHistoryRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customer.getCustomerId());

        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getType()).isEqualTo(OrderType.ORDER);
        assertThat(histories.get(0).getTotalAmount()).isEqualTo(30_000L);
    }
}
