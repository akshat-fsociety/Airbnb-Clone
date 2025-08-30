package com.coding.projects.Airbnb.service;

import com.coding.projects.Airbnb.dto.HotelDto;
import com.coding.projects.Airbnb.dto.HotelInfoDto;
import com.coding.projects.Airbnb.dto.RoomDto;
import com.coding.projects.Airbnb.entity.Hotel;
import com.coding.projects.Airbnb.entity.Room;
import com.coding.projects.Airbnb.entity.User;
import com.coding.projects.Airbnb.exception.ResourceNotFoundException;
import com.coding.projects.Airbnb.exception.UnAuthorisedException;
import com.coding.projects.Airbnb.repository.HotelRepository;
import com.coding.projects.Airbnb.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.coding.projects.Airbnb.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements IHotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final IInventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {

        log.info("Creating the hotel with name : {}", hotelDto.getName());

        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);

        hotel = hotelRepository.save(hotel);

        log.info("Created the hotel with ID : {}", hotelDto.getId());

        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {

        log.info("Getting the Hotel with Id: {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID "+id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("The user does not own this hotel with id "+ id);
        }
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating the Hotel with Id: {}", id);
        log.info("Updating the Hotel with Name: {}", hotelDto.getName());
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID "+id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("The user does not own this hotel with id "+ id);
        }

        modelMapper.map(hotelDto, hotel);
        hotel.setId(id);
        Hotel hotelToBeSaved = hotelRepository.save(hotel);
        return modelMapper.map(hotelToBeSaved, HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID "+ id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("The user does not own this hotel with id "+ id);
        }

        // Inventory business logic as soon as hotel is deleted we will delete all the future inventory
        for(Room room: hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }

        hotelRepository.deleteById(id);

    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating the Hotel with Id: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID "+ hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("The user does not own this hotel with id ");
        }


        hotel.setActive(true);

        // Inventory business logic as soon as hotel is active
        for(Room room: hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        log.info("Getting the Hotel Info");
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID "+ hotelId));

        List<RoomDto> rooms = hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .toList();

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        User user = getCurrentUser();
        List<Hotel> hotels = hotelRepository.findByOwner(user);
        return hotels
                .stream()
                .map((element) -> modelMapper.map(element, HotelDto.class)).collect(Collectors.toList());
    }
}
