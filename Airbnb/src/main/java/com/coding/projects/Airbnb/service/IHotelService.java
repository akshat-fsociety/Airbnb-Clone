package com.coding.projects.Airbnb.service;

import com.coding.projects.Airbnb.dto.HotelDto;
import com.coding.projects.Airbnb.dto.HotelInfoDto;

import java.util.List;


public interface IHotelService {

    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id, HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId);

    List<HotelDto> getAllHotels();
}
