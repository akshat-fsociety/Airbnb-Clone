package com.coding.projects.Airbnb.dto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class RoomDto {
    private Long id;
    private String type;
    private BigDecimal basePrice;
    private String[] amenities;
    private Integer totalCount;
    private Integer capacity;
}
