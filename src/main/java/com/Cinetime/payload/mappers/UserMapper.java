package com.Cinetime.payload.mappers;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.user.AbstractUserRequest;
import com.Cinetime.payload.response.BaseUserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {


    public User mapUserRequestToUser(AbstractUserRequest userRequest) {


        return User.builder()
                .firstname(userRequest.getFirstname())
                .lastname(userRequest.getLastname())
                .email(userRequest.getEmail())
                .password(userRequest.getPassword())
                .phoneNumber(userRequest.getPhoneNumber())
                .dateOfBirth(userRequest.getDateOfBirth())
                .gender(userRequest.getGender())
                .build();
    }

    public BaseUserResponse mapUserToBaseUserResponse(User user) {

        return BaseUserResponse.builder()
                .id(user.getId())
                .name(user.getFirstname() + " " + user.getLastname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
