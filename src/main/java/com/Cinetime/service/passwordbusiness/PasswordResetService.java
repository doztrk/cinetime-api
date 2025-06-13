package com.Cinetime.service.passwordbusiness;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.request.ForgotPasswordRequest;
import com.Cinetime.payload.dto.request.ResetCodeRequest;
import com.Cinetime.payload.dto.request.ResetPasswordRequest;
import com.Cinetime.payload.dto.response.PasswordResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Random random = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseMessage<PasswordResponse> generateResetPasswordCode(ForgotPasswordRequest request) {
        // Always generate code and simulate work to prevent timing attacks
        String resetCode = String.format("%06d", random.nextInt(1000000));

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            try {
                // Send email first - only save code if email succeeds
                emailService.sendPasswordResetEmail(request.getEmail(), resetCode);

                // Only set and save if email was successful
                user.setResetPasswordCode(resetCode);
                userRepository.save(user);

            } catch (Exception e) {
                log.error("Failed to send password reset email to {}: {}", request.getEmail(), e.getMessage());
                // Don't save the code if email failed
            }
        } else {
            // Simulate email sending delay for invalid emails
            try {
                Thread.sleep(100 + random.nextInt(200)); // 100-300ms delay
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // Always return same message regardless of email validity
        return ResponseMessage.<PasswordResponse>builder()
                .message(SuccessMessages.GENERATE_PASSWORD_HAS_BEEN_SENT)
                .httpStatus(HttpStatus.OK)
                .build();
    }


    public ResponseMessage<PasswordResponse> resetPassword(ResetPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByResetPasswordCode(request.getResetCode());

        //Double defense
        if (userOptional.isEmpty()) {
            return ResponseMessage.<PasswordResponse>builder()
                    .message("Invalid reset code or already used")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
        //Bring user
        User user = userOptional.get();


        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            return ResponseMessage.<PasswordResponse>builder()
                    .message("New password cannot be same as old password")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }


        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordCode(null);
        userRepository.save(user);


        return ResponseMessage.<PasswordResponse>builder()
                .message("Password has been changed successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public ResponseMessage<PasswordResponse> validateResetPasswordCode(ResetCodeRequest request) {
        Optional<User> userOptional = userRepository.findByResetPasswordCode(request.getResetCode());

        if (userOptional.isEmpty()) {
            return ResponseMessage.<PasswordResponse>builder()
                    .message("Invalid or expired reset code")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
        PasswordResponse passwordResponse = PasswordResponse.builder()
                .email(userOptional.get().getEmail())
                .build();

        return ResponseMessage.<PasswordResponse>builder()
                .message("Reset code is valid")
                .httpStatus(HttpStatus.OK)
                .object(passwordResponse)
                .build();
    }
}
