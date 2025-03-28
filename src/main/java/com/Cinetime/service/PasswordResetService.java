package com.Cinetime.service;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.ResetPasswordRequest;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Random random = new SecureRandom();

    public ResponseMessage<?> generateResetPasswordCode(ResetPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseMessage.builder()
                    .message("If the email exists, a reset code has will be sent")
                    .httpStatus(HttpStatus.OK)
                    .build();
        }
        User user = userOptional.get();
        String resetCode = String.format("%06d", random.nextInt(1000000));

        user.setResetPasswordCode(resetCode);
        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(request.getEmail(), resetCode);
            return ResponseMessage.builder()
                    .message("Password reset code has been sent")
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            return ResponseMessage.builder()
                    .message("Failed to send password reset email: " + e.getMessage())
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
