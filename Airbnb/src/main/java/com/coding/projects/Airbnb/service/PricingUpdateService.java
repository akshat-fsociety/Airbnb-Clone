package com.coding.projects.Airbnb.service;

import com.coding.projects.Airbnb.entity.Hotel;
import com.coding.projects.Airbnb.entity.HotelMinPrice;
import com.coding.projects.Airbnb.entity.Inventory;
import com.coding.projects.Airbnb.repository.HotelMinPriceRepository;
import com.coding.projects.Airbnb.repository.HotelRepository;
import com.coding.projects.Airbnb.repository.InventoryRepository;
import com.coding.projects.Airbnb.strategy.PricingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

    // SCHEDULER TO UPDATE THE Inventory AND HotelMinPrice tables EVERY HOUR

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

    /*
    * cron="sec min hrs days week month"
    * the below line means run this job at every hr at 0 min at 0 sec line 1AM, 2AM, 3AM .....
    */
    @Scheduled(cron = "0 0 * * * *") // runs every 1 hr
    public void updatePrices(){
        int page=0;
        int batchSize=100;

        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));

            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrices);

            page++;
        }
    }

    private void updateHotelPrices(Hotel hotel){

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);

        updateInventoryPrices(inventoryList);

        updateHotelMinPrice(hotel, inventoryList, startDate, endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        //COMPUTE MIN PRICE PRE DAY FOR THE HOTEL
    Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList
            .stream()
            .collect(Collectors.groupingBy(
                    Inventory::getDate,
                    Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
            ))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue().orElse(BigDecimal.ZERO)));

    //PREPARE HOTEL PRICES ENTITY IN BULK
    List<HotelMinPrice> hotelPrices = new ArrayList<>();
    dailyMinPrices.forEach((date, price) -> {
        HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel, date)
                .orElse(new HotelMinPrice(hotel, date));
        hotelPrice.setPrice(price);
        hotelPrices.add(hotelPrice);
    });

    //SAVE ALL HOTEL PRICES
    hotelMinPriceRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList){
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }

}
