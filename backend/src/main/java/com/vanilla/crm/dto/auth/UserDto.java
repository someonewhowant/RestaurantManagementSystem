package com.vanilla.crm.dto.auth;

import com.vanilla.crm.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String restaurantName;
    private String name; // Combined first + last
    private User.Role role;
    
    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .restaurantName(user.getRestaurantName())
                .name(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole())
                .build();
    }
}
