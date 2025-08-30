package com.coding.projects.Airbnb.strategy;

import com.coding.projects.Airbnb.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


/*
 *
 * DECORATOR PATTERN -> IT WILL SURGE THE PRICE BY 1.2X IF THE OCCUPANCY OF THE ROOMS IS >= 80%.
 *
 * */
@RequiredArgsConstructor
public class OccupancyPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        double occupancyRate = (double) inventory.getBookedCount() / inventory.getTotalCount();
        if(occupancyRate>=0.80){
            price = price.multiply(BigDecimal.valueOf(1.2));
        }
        return price;
    }
}
