package com.skala.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.ProductRequest;
import com.skala.shop.dto.ProductResponse;
import com.skala.shop.dto.StockUpdateRequest;
import com.skala.shop.entity.ProductCategory;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductServiceTests {

    @Autowired
    private ProductService productService;

    @Test
    void productsCanBePaged() {
        PageResponse<ProductResponse> result = productService.getAllProducts(0, 5);

        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(9);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void productCanBeCreatedUpdatedAndDeleted() {
        ProductRequest createRequest = productRequest("테스트상품", 10_000L, 5);
        ProductResponse created = productService.createProduct(createRequest);

        ProductRequest updateRequest = productRequest("수정상품", 12_000L, 7);
        ProductResponse updated = productService.updateProduct(created.getId(), updateRequest);

        assertThat(updated.getProductName()).isEqualTo("수정상품");
        assertThat(updated.getStockQuantity()).isEqualTo(7);

        productService.deleteProduct(created.getId());
        assertThatThrownBy(() -> productService.getProductById(created.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DATA_NOT_FOUND);
    }

    @Test
    void duplicateProductNameIsRejected() {
        ProductRequest request = productRequest("무선마우스", 15_000L, 10);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DATA_DUPLICATED);
    }

    @Test
    void stockCanBeAdded() {
        ProductResponse before = productService.getProductById(1L);
        StockUpdateRequest request = new StockUpdateRequest();
        request.setQuantity(5);

        ProductResponse after = productService.addStock(1L, request);

        assertThat(after.getStockQuantity()).isEqualTo(before.getStockQuantity() + 5);
    }

    private ProductRequest productRequest(String name, Long price, Integer stock) {
        ProductRequest request = new ProductRequest();
        request.setProductName(name);
        request.setProductPrice(price);
        request.setCategory(ProductCategory.ELECTRONICS);
        request.setStockQuantity(stock);
        return request;
    }
}
