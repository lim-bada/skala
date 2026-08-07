package com.skala.shop.repository;

import com.skala.shop.entity.OrderHistory;
import com.skala.shop.entity.Product;
import com.skala.shop.entity.Customer;
import com.skala.shop.entity.OrderType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    List<OrderHistory> findByCustomerCustomerIdOrderByCreatedAtDesc(String customerId);

    boolean existsByProduct(Product product);

    boolean existsByCustomer(Customer customer);

    @Query("""
            select h.product.category as category,
                   sum(case when h.type = :orderType then h.quantity else -h.quantity end) as netQuantity,
                   sum(case when h.type = :orderType then h.totalAmount else -h.totalAmount end) as netAmount
            from OrderHistory h
            where h.customer.customerId = :customerId
            group by h.product.category
            """)
    List<CategoryPreferenceProjection> findCategoryPreferences(
            @Param("customerId") String customerId,
            @Param("orderType") OrderType orderType);

    @Query("""
            select h.product.id as productId,
                   sum(case when h.type = :orderType then h.quantity else -h.quantity end) as netQuantity
            from OrderHistory h
            group by h.product.id
            """)
    List<ProductPopularityProjection> findProductPopularity(
            @Param("orderType") OrderType orderType);
}
