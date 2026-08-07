package com.skala.shop.service;

import com.skala.shop.dto.PopularProductResponse;
import com.skala.shop.dto.RecommendationResponse;
import com.skala.shop.entity.Customer;
import com.skala.shop.entity.OrderType;
import com.skala.shop.entity.Product;
import com.skala.shop.entity.ProductCategory;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;
import com.skala.shop.repository.CategoryPreferenceProjection;
import com.skala.shop.repository.CustomerRepository;
import com.skala.shop.repository.OrderHistoryRepository;
import com.skala.shop.repository.ProductPopularityProjection;
import com.skala.shop.repository.ProductRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final double CATEGORY_WEIGHT = 40.0;
    private static final double PRICE_WEIGHT = 30.0;
    private static final double POPULARITY_WEIGHT = 20.0;
    private static final double STOCK_WEIGHT = 10.0;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    @Transactional(readOnly = true)
    public List<RecommendationResponse> recommendProducts(String customerId, int limit) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND,
                        "고객을 찾을 수 없습니다: " + customerId));

        List<CategoryPreferenceProjection> preferences =
                orderHistoryRepository.findCategoryPreferences(customerId, OrderType.ORDER);
        Map<ProductCategory, Long> categoryQuantities = new HashMap<>();
        long totalQuantity = 0L;
        long totalAmount = 0L;
        for (CategoryPreferenceProjection preference : preferences) {
            long quantity = Math.max(preference.getNetQuantity(), 0L);
            long amount = Math.max(preference.getNetAmount(), 0L);
            if (quantity > 0) {
                categoryQuantities.put(preference.getCategory(), quantity);
                totalQuantity += quantity;
                totalAmount += amount;
            }
        }

        boolean hasPurchaseHistory = totalQuantity > 0;
        double averagePrice = hasPurchaseHistory ? (double) totalAmount / totalQuantity : 0.0;
        long maxCategoryQuantity = categoryQuantities.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        Map<Long, Long> popularity = productPopularity();
        long maxPopularity = popularity.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        List<Product> candidates = productRepository
                .findByStockQuantityGreaterThanOrderByIdAsc(0)
                .stream()
                .filter(product -> product.getProductPrice() <= customer.getCustomerPoint())
                .toList();
        int maxStock = candidates.stream()
                .mapToInt(Product::getStockQuantity)
                .max()
                .orElse(1);

        List<RecommendationResponse> sorted = candidates.stream()
                .map(product -> scoreProduct(
                        product,
                        categoryQuantities,
                        maxCategoryQuantity,
                        averagePrice,
                        popularity,
                        maxPopularity,
                        maxStock,
                        hasPurchaseHistory))
                .sorted(Comparator
                        .comparing(RecommendationResponse::getRecommendationScore)
                        .reversed()
                        .thenComparing(RecommendationResponse::getProductId))
                .limit(limit)
                .toList();

        return IntStream.range(0, sorted.size())
                .mapToObj(index -> sorted.get(index).withRank(index + 1))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PopularProductResponse> getPopularProducts(int limit) {
        Map<Long, Long> popularity = productPopularity();
        List<Product> products = productRepository.findAll();

        List<Product> sorted = products.stream()
                .sorted(Comparator
                        .<Product>comparingLong(product -> popularity.getOrDefault(product.getId(), 0L))
                        .reversed()
                        .thenComparing(Product::getId))
                .limit(limit)
                .toList();

        return IntStream.range(0, sorted.size())
                .mapToObj(index -> {
                    Product product = sorted.get(index);
                    return PopularProductResponse.builder()
                            .rank(index + 1)
                            .productId(product.getId())
                            .productName(product.getProductName())
                            .category(product.getCategory())
                            .productPrice(product.getProductPrice())
                            .stockQuantity(product.getStockQuantity())
                            .orderedQuantity(popularity.getOrDefault(product.getId(), 0L))
                            .build();
                })
                .toList();
    }

    private Map<Long, Long> productPopularity() {
        Map<Long, Long> result = new HashMap<>();
        for (ProductPopularityProjection projection
                : orderHistoryRepository.findProductPopularity(OrderType.ORDER)) {
            result.put(projection.getProductId(), Math.max(projection.getNetQuantity(), 0L));
        }
        return result;
    }

    private RecommendationResponse scoreProduct(
            Product product,
            Map<ProductCategory, Long> categoryQuantities,
            long maxCategoryQuantity,
            double averagePrice,
            Map<Long, Long> popularity,
            long maxPopularity,
            int maxStock,
            boolean hasPurchaseHistory) {
        double categoryScore = maxCategoryQuantity == 0
                ? 0.0
                : CATEGORY_WEIGHT * categoryQuantities.getOrDefault(product.getCategory(), 0L)
                        / maxCategoryQuantity;
        double priceScore = averagePrice == 0.0
                ? 0.0
                : PRICE_WEIGHT * Math.max(
                        0.0,
                        1.0 - Math.abs(product.getProductPrice() - averagePrice) / averagePrice);
        double popularityScore = maxPopularity == 0
                ? 0.0
                : POPULARITY_WEIGHT * popularity.getOrDefault(product.getId(), 0L) / maxPopularity;
        double stockScore = STOCK_WEIGHT * product.getStockQuantity() / maxStock;
        double totalScore = round(categoryScore + priceScore + popularityScore + stockScore);

        return RecommendationResponse.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .category(product.getCategory())
                .productPrice(product.getProductPrice())
                .stockQuantity(product.getStockQuantity())
                .recommendationScore(totalScore)
                .reason(recommendationReason(
                        product,
                        categoryScore,
                        priceScore,
                        popularityScore,
                        hasPurchaseHistory))
                .build();
    }

    private String recommendationReason(
            Product product,
            double categoryScore,
            double priceScore,
            double popularityScore,
            boolean hasPurchaseHistory) {
        if (!hasPurchaseHistory) {
            return "구매 이력이 없어 재고와 전체 인기도를 기준으로 추천했습니다";
        }
        if (categoryScore >= CATEGORY_WEIGHT && priceScore >= PRICE_WEIGHT * 0.7) {
            return "선호 카테고리이며 평균 구매 가격과 유사한 상품입니다";
        }
        if (categoryScore > 0) {
            return "고객이 자주 구매한 " + product.getCategory() + " 카테고리 상품입니다";
        }
        if (popularityScore >= POPULARITY_WEIGHT * 0.7) {
            return "전체 고객에게 인기가 높은 상품입니다";
        }
        return "현재 포인트로 구매 가능하며 재고가 있는 상품입니다";
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
