package com.skala.shop.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.skala.shop.dto.CustomerCreateRequest;
import com.skala.shop.dto.OrderRequest;
import com.skala.shop.dto.RecommendationResponse;
import com.skala.shop.entity.ProductCategory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RecommendationServiceTests {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderService orderService;

    @Test
    void electronicPurchaseProducesElectronicRecommendation() {
        createCustomer("electronics-user");
        orderService.placeOrder(orderRequest("electronics-user", 1L, 3));
        orderService.placeOrder(orderRequest("electronics-user", 2L, 1));

        List<RecommendationResponse> recommendations =
                recommendationService.recommendProducts("electronics-user", 3);

        assertThat(recommendations).hasSize(3);
        assertThat(recommendations.get(0).getRank()).isEqualTo(1);
        assertThat(recommendations.get(0).getCategory()).isEqualTo(ProductCategory.ELECTRONICS);
        assertThat(recommendations.get(0).getReason()).contains("선호 카테고리");
    }

    @Test
    void livingPurchaseProducesLivingRecommendation() {
        createCustomer("living-user");
        orderService.placeOrder(orderRequest("living-user", 4L, 3));

        List<RecommendationResponse> recommendations =
                recommendationService.recommendProducts("living-user", 3);

        assertThat(recommendations.get(0).getCategory()).isEqualTo(ProductCategory.LIVING);
    }

    @Test
    void newCustomerReceivesFallbackRecommendation() {
        createCustomer("new-user");

        List<RecommendationResponse> recommendations =
                recommendationService.recommendProducts("new-user", 3);

        assertThat(recommendations).hasSize(3);
        assertThat(recommendations)
                .allMatch(response -> response.getReason().contains("구매 이력이 없어"));
    }

    @Test
    void fullyCancelledPurchaseDoesNotRemainAsPreference() {
        createCustomer("cancelled-user");
        OrderRequest request = orderRequest("cancelled-user", 1L, 2);
        orderService.placeOrder(request);
        orderService.cancelOrder(request);

        List<RecommendationResponse> recommendations =
                recommendationService.recommendProducts("cancelled-user", 3);

        assertThat(recommendations)
                .allMatch(response -> response.getReason().contains("구매 이력이 없어"));
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
