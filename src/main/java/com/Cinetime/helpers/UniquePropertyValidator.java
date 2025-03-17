package com.Cinetime.helpers;

import com.Cinetime.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniquePropertyValidator {

    private final UserRepository userRepository;


    public boolean isRegistrationPropertiesUnique(String email, String phoneNumber) {
        return !userRepository.existsByEmail(email) && !userRepository.existsByPhoneNumber(phoneNumber);
    }
}

