package com.coding.projects.Airbnb.strategy;

import com.coding.projects.Airbnb.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


/*
 *
 * DECORATOR PATTERN -> IT WILL RETURN THE BASE PRICE OF THE ROOM.
 *
 * */
public class BasePricingStrategy implements PricingStrategy{
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        return inventory.getRoom().getBasePrice();
    }
}
