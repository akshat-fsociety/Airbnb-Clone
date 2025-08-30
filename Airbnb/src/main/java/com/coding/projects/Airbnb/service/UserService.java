package com.coding.projects.Airbnb.service;

import com.coding.projects.Airbnb.dto.ProfileUpdateRequestDto;
import com.coding.projects.Airbnb.dto.UserDto;
import com.coding.projects.Airbnb.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}