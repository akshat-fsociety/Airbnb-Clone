package com.coding.projects.Airbnb.service;

import com.coding.projects.Airbnb.dto.ProfileUpdateRequestDto;
import com.coding.projects.Airbnb.dto.UserDto;
import com.coding.projects.Airbnb.entity.User;
import com.coding.projects.Airbnb.exception.ResourceNotFoundException;
import com.coding.projects.Airbnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.coding.projects.Airbnb.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User Not found with id: "+ id));
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user = getCurrentUser();

        if(profileUpdateRequestDto.getDateOfBirth()!=null)
            user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
        if(profileUpdateRequestDto.getGender() != null)
            user.setGender(profileUpdateRequestDto.getGender());
        if(profileUpdateRequestDto.getName() != null)
            user.setName(profileUpdateRequestDto.getName());

        userRepository.save(user);

    }

    @Override
    public UserDto getMyProfile() {
        return modelMapper.map(getCurrentUser(), UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElse(null);
    }
}
