package com.coding.projects.Airbnb.dto;

import com.coding.projects.Airbnb.entity.enums.Gender;
import com.stripe.model.tax.Registration;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {
    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
}
