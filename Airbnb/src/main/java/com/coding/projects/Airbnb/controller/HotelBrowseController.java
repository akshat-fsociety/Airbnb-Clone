package com.coding.projects.Airbnb.controller;

import com.coding.projects.Airbnb.dto.HotelDto;
import com.coding.projects.Airbnb.dto.HotelInfoDto;
import com.coding.projects.Airbnb.dto.HotelPriceDto;
import com.coding.projects.Airbnb.dto.HotelSearchRequest;
import com.coding.projects.Airbnb.service.IHotelService;
import com.coding.projects.Airbnb.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/hotels")
public class HotelBrowseController {

    private final IInventoryService iInventoryService;
    private final IHotelService iHotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest){
        var page = iInventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId){
        return ResponseEntity.ok(iHotelService.getHotelInfoById(hotelId));
    }

}
