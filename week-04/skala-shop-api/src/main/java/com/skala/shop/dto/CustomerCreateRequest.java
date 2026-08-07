package com.skala.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerCreateRequest {

    @NotBlank(message = "고객 ID는 필수입니다")
    @Size(min = 3, max = 50, message = "고객 ID는 3자 이상 50자 이하여야 합니다")
    private String customerId;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 4, max = 100, message = "비밀번호는 4자 이상 100자 이하여야 합니다")
    private String customerPassword;
}
