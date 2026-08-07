package com.skala.shop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerPointUpdateRequest {

    @NotNull(message = "고객 포인트는 필수입니다")
    @PositiveOrZero(message = "고객 포인트는 0 이상이어야 합니다")
    private Long customerPoint;
}
