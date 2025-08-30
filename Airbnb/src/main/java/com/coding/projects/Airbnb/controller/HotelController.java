package com.coding.projects.Airbnb.controller;

import com.coding.projects.Airbnb.dto.BookingDto;
import com.coding.projects.Airbnb.dto.HotelDto;
import com.coding.projects.Airbnb.dto.HotelReportDto;
import com.coding.projects.Airbnb.service.IBookingService;
import com.coding.projects.Airbnb.service.IHotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final IHotelService iHotelService;
    private final IBookingService iBookingService;

    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto){

        log.info("Attempting to create the new Hotel with name: "+ hotelDto.getName());

        HotelDto hotel = iHotelService.createNewHotel(hotelDto);

        return new ResponseEntity<>(hotel, HttpStatus.CREATED);

    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId){
        HotelDto hotelDto = iHotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotelDto);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId, @RequestBody HotelDto hotelDto){
        HotelDto hotel = iHotelService.updateHotelById(hotelId, hotelDto);
        return ResponseEntity.ok(hotel);
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId){
        iHotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}")
    public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId){
        iHotelService.activateHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels(){
        return ResponseEntity.ok(iHotelService.getAllHotels());
    }

    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getAllBookingsByHotelId(@PathVariable Long hotelId){
        return ResponseEntity.ok(iBookingService.getAllBookingsByHotelId(hotelId));
    }

    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportDto> getHotelReport(@PathVariable Long hotelId,
                                                         @RequestParam(required = false)LocalDate startDate,
                                                         @RequestParam(required = false)LocalDate endDate){

        // If the user did not specify the date range then show them the 30 days data.
        if(startDate == null) startDate = startDate.now().minusMonths(1);
        if(endDate == null) endDate = endDate.now();

        return ResponseEntity.ok(iBookingService.getHotelReport(hotelId, startDate, endDate));
    }

}
