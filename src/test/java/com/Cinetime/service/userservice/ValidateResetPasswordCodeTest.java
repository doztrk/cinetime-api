package com.Cinetime.service.userservice;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.request.ResetCodeRequest;
import com.Cinetime.payload.dto.response.PasswordResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.service.passwordbusiness.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateResetPasswordCodeTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private ResetCodeRequest validRequest;
    private ResetCodeRequest invalidRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRequest = ResetCodeRequest.builder()
                .resetCode("123456")
                .build();

        invalidRequest = ResetCodeRequest.builder()
                .resetCode("invalid_code")
                .build();

        testUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("test@example.com")
                .resetPasswordCode("123456")
                .build();
    }

    @Test
    @DisplayName("Should return success response when reset code is valid")
    void validateResetPasswordCode_ValidCode_ReturnsSuccessResponse() {
        // Given
        when(userRepository.findByResetPasswordCode("123456"))
                .thenReturn(Optional.of(testUser));

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.validateResetPasswordCode(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Reset code is valid");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findByResetPasswordCode("123456");
    }

    @Test
    @DisplayName("Should return error response when reset code is invalid")
    void validateResetPasswordCode_InvalidCode_ReturnsErrorResponse() {
        // Given
        when(userRepository.findByResetPasswordCode("invalid_code"))
                .thenReturn(Optional.empty());

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.validateResetPasswordCode(invalidRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo("Invalid or expired reset code");
        assertThat(result.getObject()).isNull();

        verify(userRepository).findByResetPasswordCode("invalid_code");
    }

    @Test
    @DisplayName("Should return error response when reset code is null")
    void validateResetPasswordCode_NullCode_ReturnsErrorResponse() {
        // Given
        ResetCodeRequest nullRequest = ResetCodeRequest.builder()
                .resetCode(null)
                .build();

        when(userRepository.findByResetPasswordCode(null))
                .thenReturn(Optional.empty());

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.validateResetPasswordCode(nullRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo("Invalid or expired reset code");
        assertThat(result.getObject()).isNull();
    }

    @Test
    @DisplayName("Should return error response when reset code is empty string")
    void validateResetPasswordCode_EmptyCode_ReturnsErrorResponse() {
        // Given
        ResetCodeRequest emptyRequest = ResetCodeRequest.builder()
                .resetCode("")
                .build();

        when(userRepository.findByResetPasswordCode(""))
                .thenReturn(Optional.empty());

        // When
        ResponseMessage<PasswordResponse> result = passwordResetService.validateResetPasswordCode(emptyRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo("Invalid or expired reset code");
        assertThat(result.getObject()).isNull();
    }
}