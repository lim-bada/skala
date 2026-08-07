package com.skala.shop.service;

import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.ProductRequest;
import com.skala.shop.dto.ProductResponse;
import com.skala.shop.dto.StockUpdateRequest;
import com.skala.shop.entity.Product;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;
import com.skala.shop.exception.ParameterException;
import com.skala.shop.repository.OrderHistoryRepository;
import com.skala.shop.repository.OrderItemRepository;
import com.skala.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<ProductResponse> result = productRepository.findAll(pageRequest).map(ProductResponse::from);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return ProductResponse.from(findProduct(id));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateDuplicateName(request.getProductName(), null);

        Product product = new Product(
                request.getProductName(),
                request.getProductPrice(),
                request.getCategory(),
                request.getStockQuantity());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);
        validateDuplicateName(request.getProductName(), id);

        product.setProductName(request.getProductName());
        product.setProductPrice(request.getProductPrice());
        product.setCategory(request.getCategory());
        product.setStockQuantity(request.getStockQuantity());
        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        if (orderItemRepository.existsByProduct(product)
                || orderHistoryRepository.existsByProduct(product)) {
            throw new BusinessException(ErrorCode.DATA_IN_USE, "주문 정보가 있는 상품은 삭제할 수 없습니다");
        }
        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse addStock(Long id, StockUpdateRequest request) {
        Product product = findProduct(id);
        try {
            product.setStockQuantity(Math.addExact(product.getStockQuantity(), request.getQuantity()));
        } catch (ArithmeticException exception) {
            throw new ParameterException("상품 재고 수량이 허용 범위를 초과했습니다");
        }
        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DATA_NOT_FOUND,
                        "상품을 찾을 수 없습니다: " + id));
    }

    private void validateDuplicateName(String productName, Long currentProductId) {
        productRepository.findByProductName(productName)
                .filter(product -> !product.getId().equals(currentProductId))
                .ifPresent(product -> {
                    throw new BusinessException(
                            ErrorCode.DATA_DUPLICATED,
                            "이미 등록된 상품명입니다: " + productName);
                });
    }
}
