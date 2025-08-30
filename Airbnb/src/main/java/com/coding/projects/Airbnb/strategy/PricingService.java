package com.coding.projects.Airbnb.strategy;

import com.coding.projects.Airbnb.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {

    public BigDecimal calculateDynamicPricing(Inventory inventory){
        PricingStrategy pricingStrategy = new BasePricingStrategy();

        // APPLY ADDITIONAL STRATEGIES;
        pricingStrategy = new SurgePricingStrategy(pricingStrategy);
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
        pricingStrategy = new HolidayPricingStrategy(pricingStrategy);

        return pricingStrategy.calculatePrice(inventory);
    }

    public BigDecimal calculateTotalPrice(List<Inventory> intevtoryList){
        return intevtoryList.stream()
                .map(inventory -> calculateDynamicPricing(inventory))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
