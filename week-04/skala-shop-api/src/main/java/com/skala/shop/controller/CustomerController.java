package com.skala.shop.controller;

import com.skala.shop.common.ApiResponse;
import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.CustomerCreateRequest;
import com.skala.shop.dto.CustomerLoginRequest;
import com.skala.shop.dto.CustomerPointUpdateRequest;
import com.skala.shop.dto.CustomerResponse;
import com.skala.shop.dto.CustomerSummaryResponse;
import com.skala.shop.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Validated
@Tag(name = "고객 관리", description = "고객 CRUD 및 로그인 API")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "고객 목록 조회", description = "고객을 ID 오름차순으로 페이징 조회합니다")
    public ApiResponse<PageResponse<CustomerSummaryResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "페이지는 0 이상이어야 합니다") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
                    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
                    int size) {
        return ApiResponse.success("고객 목록을 조회했습니다", customerService.getAllCustomers(page, size));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "고객 상세 및 주문 상품 조회")
    public ApiResponse<CustomerResponse> getCustomerById(@PathVariable String customerId) {
        return ApiResponse.success("고객을 조회했습니다", customerService.getCustomerById(customerId));
    }

    @PostMapping
    @Operation(summary = "고객 회원가입", description = "신규 고객에게 초기 포인트 1,000,000점을 지급합니다")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다", customer));
    }

    @PostMapping("/login")
    @Operation(summary = "고객 로그인", description = "JWT 없이 고객 ID와 비밀번호의 일치 여부만 검증합니다")
    public ApiResponse<CustomerResponse> loginCustomer(
            @Valid @RequestBody CustomerLoginRequest request) {
        return ApiResponse.success("로그인에 성공했습니다", customerService.loginCustomer(request));
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "고객 포인트 수정")
    public ApiResponse<CustomerResponse> updateCustomerPoint(
            @PathVariable String customerId,
            @Valid @RequestBody CustomerPointUpdateRequest request) {
        return ApiResponse.success(
                "고객 포인트를 수정했습니다",
                customerService.updateCustomerPoint(customerId, request));
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "고객 삭제")
    public ApiResponse<Void> deleteCustomer(@PathVariable String customerId) {
        customerService.deleteCustomer(customerId);
        return ApiResponse.success("고객을 삭제했습니다");
    }
}
