package com.skala.shop.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import com.skala.shop.entity.Customer;
import com.skala.shop.entity.Product;
import com.skala.shop.repository.CustomerRepository;
import com.skala.shop.repository.ProductRepository;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class OptimisticLockConcurrencyTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void onlyOneConcurrentUpdateSucceedsForTheLastStock() throws Exception {
        Product setupProduct = productRepository.findById(9L).orElseThrow();
        int originalStock = setupProduct.getStockQuantity();
        setupProduct.setStockQuantity(1);
        setupProduct = productRepository.saveAndFlush(setupProduct);
        long versionBeforeOrders = setupProduct.getVersion();

        customerRepository.saveAll(List.of(
                new Customer("concurrent-a", "password", 1_000_000L),
                new Customer("concurrent-b", "password", 1_000_000L)));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);

        try {
            Future<Boolean> first = executor.submit(() -> updateLastStock("concurrent-a", barrier));
            Future<Boolean> second = executor.submit(() -> updateLastStock("concurrent-b", barrier));

            List<Boolean> results = List.of(first.get(), second.get());
            Product resultProduct = productRepository.findById(9L).orElseThrow();
            long totalPoint = customerRepository.findAllById(List.of("concurrent-a", "concurrent-b"))
                    .stream()
                    .mapToLong(Customer::getCustomerPoint)
                    .sum();

            assertThat(results).containsExactlyInAnyOrder(true, false);
            assertThat(resultProduct.getStockQuantity()).isZero();
            assertThat(resultProduct.getVersion()).isEqualTo(versionBeforeOrders + 1);
            assertThat(totalPoint).isEqualTo(1_955_000L);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            customerRepository.deleteAllById(List.of("concurrent-a", "concurrent-b"));
            Product cleanupProduct = productRepository.findById(9L).orElseThrow();
            cleanupProduct.setStockQuantity(originalStock);
            productRepository.saveAndFlush(cleanupProduct);
        }
    }

    private boolean updateLastStock(String customerId, CyclicBarrier barrier) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        try {
            return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                Product product = productRepository.findById(9L).orElseThrow();
                Customer customer = customerRepository.findById(customerId).orElseThrow();

                awaitBothTransactions(barrier);

                product.setStockQuantity(product.getStockQuantity() - 1);
                customer.setCustomerPoint(customer.getCustomerPoint() - product.getProductPrice());
                productRepository.saveAndFlush(product);
                return true;
            }));
        } catch (OptimisticLockingFailureException exception) {
            return false;
        }
    }

    private void awaitBothTransactions(CyclicBarrier barrier) {
        try {
            barrier.await(5, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException("동시 주문 테스트를 시작하지 못했습니다", exception);
        }
    }
}
