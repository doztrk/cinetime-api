package com.Cinetime.helpers;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.UserUpdateRequest;
import org.apache.commons.lang3.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateUserHelper {

    private final PasswordEncoder passwordEncoder;

    public User updateUserIfUpdatesExistInRequest(UserUpdateRequest userUpdateRequest, User user) {

        if (!StringUtils.isBlank(userUpdateRequest.getFirstname())) {
            user.setFirstname(userUpdateRequest.getFirstname());
        }
        if (!StringUtils.isBlank(userUpdateRequest.getLastname())) {
            user.setLastname(userUpdateRequest.getLastname());
        }
        if (!StringUtils.isBlank(userUpdateRequest.getEmail())) {
            user.setEmail(userUpdateRequest.getEmail());
        }
        if (!StringUtils.isBlank(userUpdateRequest.getPhoneNumber())) {
            user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        }
        if (!StringUtils.isBlank(userUpdateRequest.getPassword())) {
            user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        }
        if (userUpdateRequest.getGender() != null) {
            user.setGender(userUpdateRequest.getGender());
        }
        if (userUpdateRequest.getDateOfBirth() != null) {
            user.setDateOfBirth(userUpdateRequest.getDateOfBirth());
        }
        return user;

    }
}
