package com.coding.projects.Airbnb.dto;

import com.coding.projects.Airbnb.entity.User;
import com.coding.projects.Airbnb.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class GuestDto {
    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
