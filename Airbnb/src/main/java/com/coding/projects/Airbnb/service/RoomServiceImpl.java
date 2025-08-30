package com.coding.projects.Airbnb.service;

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
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements IRoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final IInventoryService iInventoryService;
    private final ModelMapper modelMapper;

    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating the new Room with Hotel id {}:", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID "+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("The user does not own this hotel with id "+ hotelId);
        }

        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);
        log.info("Created the new Room with id {}:", room.getId());

        // Inventory business logic
        if(hotel.getActive()){
            iInventoryService.initializeRoomForAYear(room);
        }

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all the Rooms from Hotel id {}:", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID "+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("The user does not own this hotel with id "+ hotelId);
        }

        return hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the Room with the id {}:", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with ID "+ roomId));
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the Room with the id {}:", roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with ID "+ roomId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorisedException("The user does not own this room with id "+ roomId);
        }


        iInventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);

    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID "+hotelId));

        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("The user does not own this hotel with id "+ hotelId);
        }

        Room room = roomRepository.findById(roomId).orElseThrow(()->new ResourceNotFoundException("Room not found with ID "+roomId));
        modelMapper.map(roomDto, room);
        room.setId(roomId);

        room = roomRepository.save(room);
        return modelMapper.map(room, RoomDto.class);
    }
}
