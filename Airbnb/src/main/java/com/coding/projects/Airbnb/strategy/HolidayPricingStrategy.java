package com.coding.projects.Airbnb.strategy;

import com.coding.projects.Airbnb.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


/*
 *
 * DECORATOR PATTERN -> IT WILL SURGE THE PRICE BY 1.6X IF THE BOOKING DATE IS DURING HOLIDAYS.
 *
 * */
@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        boolean isHolidayToday = true;  // CALL THE 3RD PARTY API TO CHECK IF TODAY IS HOLIDAY
        if(isHolidayToday){
            price = price.multiply(BigDecimal.valueOf(1.6));
        }
        return price;
    }
}
