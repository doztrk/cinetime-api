package com.Cinetime.service.userservice;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.request.ForgotPasswordRequest;
import com.Cinetime.payload.dto.response.PasswordResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.service.EmailService;
import com.Cinetime.service.passwordbusiness.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateResetPasswordCodeTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private ForgotPasswordRequest validRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRequest = ForgotPasswordRequest.builder()
                .email("test@example.com")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstname("Test")
                .lastname("User")
                .build();
    }

    @Test
    void generateResetPasswordCode_WithValidEmail_ShouldSaveCodeAndSendEmail() {
        // Given
        when(userRepository.findByEmail(validRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        doNothing().when(emailService)
                .sendPasswordResetEmail(eq(validRequest.getEmail()), anyString());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.generateResetPasswordCode(validRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.GENERATE_PASSWORD_HAS_BEEN_SENT);

        // Verify email was sent
        verify(emailService).sendPasswordResetEmail(eq(validRequest.getEmail()), anyString());

        // Verify user was saved with reset code
        verify(userRepository).save(argThat(user ->
                user.getResetPasswordCode() != null &&
                        user.getResetPasswordCode().matches("\\d{6}")
        ));
    }

    @Test
    void generateResetPasswordCode_WithInvalidEmail_ShouldReturnSuccessWithoutSaving() {
        // Given
        when(userRepository.findByEmail(validRequest.getEmail()))
                .thenReturn(Optional.empty());

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.generateResetPasswordCode(validRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.GENERATE_PASSWORD_HAS_BEEN_SENT);

        // Verify no email was sent
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());

        // Verify no user was saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void generateResetPasswordCode_WhenEmailSendingFails_ShouldNotSaveCode() {
        // Given
        when(userRepository.findByEmail(validRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService)
                .sendPasswordResetEmail(eq(validRequest.getEmail()), anyString());

        // When
        ResponseMessage<?> result = passwordResetService.generateResetPasswordCode(validRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.GENERATE_PASSWORD_HAS_BEEN_SENT);

        // Verify email sending was attempted
        verify(emailService).sendPasswordResetEmail(eq(validRequest.getEmail()), anyString());

        // Verify user was NOT saved since email failed
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void generateResetPasswordCode_ShouldGenerateSixDigitCode() {
        // Given
        when(userRepository.findByEmail(validRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        doNothing().when(emailService)
                .sendPasswordResetEmail(eq(validRequest.getEmail()), anyString());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        passwordResetService.generateResetPasswordCode(validRequest);

        // Then
        verify(userRepository).save(argThat(user -> {
            String code = user.getResetPasswordCode();
            return code != null &&
                    code.length() == 6 &&
                    code.matches("\\d{6}") &&
                    Integer.parseInt(code) >= 0 &&
                    Integer.parseInt(code) <= 999999;
        }));
    }

    @Test
    void generateResetPasswordCode_ShouldHandleNullEmailGracefully() {
        // Given
        ForgotPasswordRequest nullEmailRequest = ForgotPasswordRequest.builder()
                .email(null)
                .build();
        when(userRepository.findByEmail(null))
                .thenReturn(Optional.empty());

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.generateResetPasswordCode(nullEmailRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.GENERATE_PASSWORD_HAS_BEEN_SENT);

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void generateResetPasswordCode_ShouldOverwriteExistingResetCode() {
        // Given
        testUser.setResetPasswordCode("123456"); // Existing code
        when(userRepository.findByEmail(validRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        doNothing().when(emailService)
                .sendPasswordResetEmail(eq(validRequest.getEmail()), anyString());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        passwordResetService.generateResetPasswordCode(validRequest);

        // Then
        verify(userRepository).save(argThat(user -> {
            String newCode = user.getResetPasswordCode();
            return newCode != null &&
                    !newCode.equals("123456") && // Should be different from original
                    newCode.matches("\\d{6}");
        }));
    }

    @Test
    void generateResetPasswordCode_ShouldReturnSameMessageForValidAndInvalidEmails() {
        // Test valid email
        when(userRepository.findByEmail("valid@example.com"))
                .thenReturn(Optional.of(testUser));
        ResponseMessage<?> validResult = passwordResetService.generateResetPasswordCode(
                ForgotPasswordRequest.builder().email("valid@example.com").build()
        );

        // Test invalid email
        when(userRepository.findByEmail("invalid@example.com"))
                .thenReturn(Optional.empty());
        ResponseMessage<?> invalidResult = passwordResetService.generateResetPasswordCode(
                ForgotPasswordRequest.builder().email("invalid@example.com").build()
        );

        // Both should return identical responses
        assertThat(validResult.getHttpStatus()).isEqualTo(invalidResult.getHttpStatus());
        assertThat(validResult.getMessage()).isEqualTo(invalidResult.getMessage());
    }
}