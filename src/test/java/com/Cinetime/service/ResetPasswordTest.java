package com.Cinetime.service.passwordbusiness;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.request.ResetPasswordRequest;
import com.Cinetime.payload.dto.response.PasswordResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;
    private ResetPasswordRequest validRequest;
    private final String RESET_CODE = "123456";
    private final String OLD_PASSWORD_HASH = "$2a$10$oldHashedPassword";
    private final String NEW_PASSWORD = "NewSecure123!";
    private final String NEW_PASSWORD_HASH = "$2a$10$newHashedPassword";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .password(OLD_PASSWORD_HASH)
                .resetPasswordCode(RESET_CODE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = ResetPasswordRequest.builder()
                .newPassword(NEW_PASSWORD)
                .resetCode(RESET_CODE)
                .build();
    }

    @Test
    @DisplayName("Should successfully reset password with valid reset code")
    void resetPassword_WithValidResetCode_ShouldResetPasswordSuccessfully() {
        // Given
        when(userRepository.findByResetPasswordCode(RESET_CODE))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(NEW_PASSWORD, OLD_PASSWORD_HASH))
                .thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD))
                .thenReturn(NEW_PASSWORD_HASH);
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.resetPassword(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Password has been changed successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        // Verify user was updated correctly
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo(NEW_PASSWORD_HASH);
        assertThat(savedUser.getResetPasswordCode()).isNull();

        // Verify interactions
        verify(userRepository).findByResetPasswordCode(RESET_CODE);
        verify(passwordEncoder).matches(NEW_PASSWORD, OLD_PASSWORD_HASH);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(userRepository).save(testUser);
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should return error when reset code is invalid")
    void resetPassword_WithInvalidResetCode_ShouldReturnError() {
        // Given
        String invalidCode = "invalid_code";
        ResetPasswordRequest invalidRequest = ResetPasswordRequest.builder()
                .newPassword(NEW_PASSWORD)
                .resetCode(invalidCode)
                .build();

        when(userRepository.findByResetPasswordCode(invalidCode))
                .thenReturn(Optional.empty());

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.resetPassword(invalidRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Invalid reset code or already used");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(userRepository).findByResetPasswordCode(invalidCode);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should return error when new password is same as old password")
    void resetPassword_WithSamePassword_ShouldReturnError() {
        // Given
        when(userRepository.findByResetPasswordCode(RESET_CODE))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(NEW_PASSWORD, OLD_PASSWORD_HASH))
                .thenReturn(true); // Same password

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.resetPassword(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("New password cannot be same as old password");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(userRepository).findByResetPasswordCode(RESET_CODE);
        verify(passwordEncoder).matches(NEW_PASSWORD, OLD_PASSWORD_HASH);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle null reset code gracefully")
    void resetPassword_WithNullResetCode_ShouldReturnError() {
        // Given
        ResetPasswordRequest nullCodeRequest = ResetPasswordRequest.builder()
                .newPassword(NEW_PASSWORD)
                .resetCode(null)
                .build();

        when(userRepository.findByResetPasswordCode(null))
                .thenReturn(Optional.empty());

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.resetPassword(nullCodeRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Invalid reset code or already used");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepository).findByResetPasswordCode(null);
        verifyNoMoreInteractions(passwordEncoder, userRepository);
    }

    @Test
    @DisplayName("Should handle empty reset code gracefully")
    void resetPassword_WithEmptyResetCode_ShouldReturnError() {
        // Given
        ResetPasswordRequest emptyCodeRequest = ResetPasswordRequest.builder()
                .newPassword(NEW_PASSWORD)
                .resetCode("")
                .build();

        when(userRepository.findByResetPasswordCode(""))
                .thenReturn(Optional.empty());

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.resetPassword(emptyCodeRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Invalid reset code or already used");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepository).findByResetPasswordCode("");
        verifyNoMoreInteractions(passwordEncoder, userRepository);
    }

    @Test
    @DisplayName("Should handle user with null reset password code")
    void resetPassword_WithUserHavingNullResetCode_ShouldStillWork() {
        // Given
        User userWithNullCode = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password(OLD_PASSWORD_HASH)
                .resetPasswordCode(null) // User's reset code is null
                .build();

        when(userRepository.findByResetPasswordCode(RESET_CODE))
                .thenReturn(Optional.of(userWithNullCode));
        when(passwordEncoder.matches(NEW_PASSWORD, OLD_PASSWORD_HASH))
                .thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD))
                .thenReturn(NEW_PASSWORD_HASH);

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.resetPassword(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Password has been changed successfully");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getResetPasswordCode()).isNull();
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    void resetPassword_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        when(userRepository.findByResetPasswordCode(RESET_CODE))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        try {
            passwordResetService.resetPassword(validRequest);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Database connection failed");
        }

        verify(userRepository).findByResetPasswordCode(RESET_CODE);
        verifyNoMoreInteractions(passwordEncoder, userRepository);
    }

    @Test
    @DisplayName("Should handle password encoder exception gracefully")
    void resetPassword_WhenPasswordEncoderThrowsException_ShouldPropagateException() {
        // Given
        when(userRepository.findByResetPasswordCode(RESET_CODE))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(NEW_PASSWORD, OLD_PASSWORD_HASH))
                .thenThrow(new RuntimeException("Password encoding failed"));

        // When & Then
        try {
            passwordResetService.resetPassword(validRequest);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Password encoding failed");
        }

        verify(userRepository).findByResetPasswordCode(RESET_CODE);
        verify(passwordEncoder).matches(NEW_PASSWORD, OLD_PASSWORD_HASH);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reset password code to null after successful reset")
    void resetPassword_AfterSuccessfulReset_ShouldClearResetCode() {
        // Given
        when(userRepository.findByResetPasswordCode(RESET_CODE))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(NEW_PASSWORD, OLD_PASSWORD_HASH))
                .thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD))
                .thenReturn(NEW_PASSWORD_HASH);

        // When
        passwordResetService.resetPassword(validRequest);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getResetPasswordCode()).isNull();
        assertThat(savedUser.getPassword()).isEqualTo(NEW_PASSWORD_HASH);
    }

    @Test
    @DisplayName("Should preserve all other user data during password reset")
    void resetPassword_ShouldPreserveAllOtherUserData() {
        // Given
        when(userRepository.findByResetPasswordCode(RESET_CODE))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(NEW_PASSWORD, OLD_PASSWORD_HASH))
                .thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD))
                .thenReturn(NEW_PASSWORD_HASH);

        // When
        passwordResetService.resetPassword(validRequest);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getId()).isEqualTo(testUser.getId());
        assertThat(savedUser.getFirstname()).isEqualTo(testUser.getFirstname());
        assertThat(savedUser.getLastname()).isEqualTo(testUser.getLastname());
        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(savedUser.getPhoneNumber()).isEqualTo(testUser.getPhoneNumber());
        assertThat(savedUser.getCreatedAt()).isEqualTo(testUser.getCreatedAt());
    }
}