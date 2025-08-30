package com.coding.projects.Airbnb.dto;

import com.coding.projects.Airbnb.entity.Hotel;
import com.coding.projects.Airbnb.entity.Room;
import com.coding.projects.Airbnb.entity.User;
import com.coding.projects.Airbnb.entity.enums.BookingStatus;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class BookingDto {
    private Long id;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
    private BigDecimal amount;
}
