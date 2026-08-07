package com.skala.shop.repository;

import com.skala.shop.entity.ProductCategory;

public interface CategoryPreferenceProjection {

    ProductCategory getCategory();

    Long getNetQuantity();

    Long getNetAmount();
}
