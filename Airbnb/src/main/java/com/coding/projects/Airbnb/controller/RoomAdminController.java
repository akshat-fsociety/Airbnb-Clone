package com.coding.projects.Airbnb.controller;

import com.coding.projects.Airbnb.dto.RoomDto;
import com.coding.projects.Airbnb.service.IRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomAdminController {

    private final IRoomService iRoomService;

    @PostMapping
    public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId, @RequestBody RoomDto roomDto){
        RoomDto room = iRoomService.createNewRoom(hotelId, roomDto);
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRoomsInHotel(@PathVariable Long hotelId){
        List<RoomDto> roomDtoList = iRoomService.getAllRoomsInHotel(hotelId);
        return ResponseEntity.ok(roomDtoList);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long roomId, @PathVariable Long hotelId){
        RoomDto roomDto = iRoomService.getRoomById(roomId);
        return ResponseEntity.ok(roomDto);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<RoomDto> deleteRoomById(@PathVariable Long roomId){
        iRoomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomDto> updateRoomById(@PathVariable Long hotelId, @PathVariable Long roomId, @RequestBody RoomDto roomDto){
        return ResponseEntity.ok(iRoomService.updateRoomById(hotelId, roomId, roomDto));
    }
}
