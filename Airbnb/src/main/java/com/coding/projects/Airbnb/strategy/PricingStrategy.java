package com.coding.projects.Airbnb.strategy;

import com.coding.projects.Airbnb.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);

}
