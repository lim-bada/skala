package com.skala.shop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockUpdateRequest {

    @NotNull(message = "추가할 재고 수량은 필수입니다")
    @Positive(message = "추가할 재고 수량은 1개 이상이어야 합니다")
    private Integer quantity;
}
