package com.Cinetime.service.passwordbusiness;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.ForgotPasswordRequest;
import com.Cinetime.payload.dto.ResetCodeRequest;
import com.Cinetime.payload.dto.ResetPasswordRequest;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Random random = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseMessage<?> generateResetPasswordCode(ForgotPasswordRequest request) {
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


    public ResponseMessage<?> resetPassword(ResetPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByResetPasswordCode(request.getResetCode());

        //Double defense
        if (userOptional.isEmpty()) {
            return ResponseMessage.builder()
                    .message("Invalid reset code or already used")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
        //Bring user
        User user = userOptional.get();


        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            return ResponseMessage.builder()
                    .message("New password cannot be same as old password")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }


        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordCode(null);
        userRepository.save(user);


        return ResponseMessage.builder()
                .message("Password has been changed successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public ResponseMessage<?> validateResetPasswordCode(ResetCodeRequest request) {
        Optional<User> userOptional = userRepository.findByResetPasswordCode(request.getResetCode());

        if (userOptional.isEmpty()) {
            return ResponseMessage.builder()
                    .message("Invalid or expired reset code")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        return ResponseMessage.builder()
                .message("Approved")
                .httpStatus(HttpStatus.OK)
                .object(userOptional.get().getEmail())
                .build();
    }
}
