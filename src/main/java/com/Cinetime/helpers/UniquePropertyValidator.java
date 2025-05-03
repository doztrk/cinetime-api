package com.Cinetime.helpers;

import com.Cinetime.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniquePropertyValidator {

    private final UserRepository userRepository;


    //Property checker to be used for creating user only.
    public boolean uniquePropertyChecker(String email, String phoneNumber) {
        return !userRepository.existsByEmail(email) && !userRepository.existsByPhoneNumber(phoneNumber);
    }

    public boolean isEmailUniqueForUpdate(String email, Long userId) {
        return !userRepository.existsByEmailAndIdNot(email, userId);
    }

    public boolean isPhoneNumberUniqueForUpdate(String phoneNumber, Long userId) {
        return !userRepository.existsByPhoneNumberAndIdNot(phoneNumber, userId);
    }
}

