package com.vanilla.crm.mapper;

import com.vanilla.crm.dto.auth.UserDto;
import com.vanilla.crm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public UserDto toDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .restaurantName(user.getRestaurantName())
                .name(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }
}
