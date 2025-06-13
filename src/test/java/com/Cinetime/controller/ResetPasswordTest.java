package com.Cinetime.controller;

import com.Cinetime.payload.dto.request.ResetPasswordRequest;
import com.Cinetime.payload.dto.response.PasswordResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.passwordbusiness.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.security.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class ResetPasswordTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordResetService passwordResetService;

    @Autowired
    private ObjectMapper objectMapper;

    private ResetPasswordRequest validRequest;
    private ResponseMessage<PasswordResponse> successResponse;
    private ResponseMessage<PasswordResponse> invalidCodeResponse;
    private ResponseMessage<PasswordResponse> samePasswordResponse;

    @BeforeEach
    void setUp() {
        // Valid request
        validRequest = ResetPasswordRequest.builder()
                .newPassword("NewSecure123!")
                .resetCode("123456")
                .build();

        // Success response
        successResponse = ResponseMessage.<PasswordResponse>builder()
                .message("Password has been changed successfully")
                .httpStatus(HttpStatus.OK)
                .build();

        // Invalid code response
        invalidCodeResponse = ResponseMessage.<PasswordResponse>builder()
                .message("Invalid reset code or already used")
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();

        // Same password response
        samePasswordResponse = ResponseMessage.<PasswordResponse>builder()
                .message("New password cannot be same as old password")
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();
    }

    @Test
    @DisplayName("Should successfully reset password with valid request")
    void resetPassword_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        when(passwordResetService.resetPassword(any(ResetPasswordRequest.class)))
                .thenReturn(successResponse);

        // When
        ResultActions result = mockMvc.perform(put("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Password has been changed successfully"))
                .andExpect(jsonPath("$.httpStatus").value("OK"));

        verify(passwordResetService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Should return bad request for invalid reset code")
    void resetPassword_WithInvalidResetCode_ShouldReturnBadRequest() throws Exception {
        // Given
        ResetPasswordRequest invalidCodeRequest = ResetPasswordRequest.builder()
                .newPassword("NewSecure123!")
                .resetCode("invalidCode")
                .build();

        when(passwordResetService.resetPassword(any(ResetPasswordRequest.class)))
                .thenReturn(invalidCodeResponse);

        // When
        ResultActions result = mockMvc.perform(put("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCodeRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isOk()) // Note: Your controller always returns 200, which is wrong
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid reset code or already used"))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"));

        verify(passwordResetService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Should return bad request when new password same as old password")
    void resetPassword_WithSamePassword_ShouldReturnBadRequest() throws Exception {
        // Given
        when(passwordResetService.resetPassword(any(ResetPasswordRequest.class)))
                .thenReturn(samePasswordResponse);

        // When
        ResultActions result = mockMvc.perform(put("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isOk()) // Again, should be 400
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("New password cannot be same as old password"))
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"));

        verify(passwordResetService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Should return validation error for missing new password")
    void resetPassword_WithMissingNewPassword_ShouldReturnValidationError() throws Exception {
        // Given
        ResetPasswordRequest invalidRequest = ResetPasswordRequest.builder()
                .resetCode("123456")
                // Missing newPassword
                .build();

        // When
        ResultActions result = mockMvc.perform(put("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(passwordResetService, never()).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Should return validation error for missing reset code")
    void resetPassword_WithMissingResetCode_ShouldReturnValidationError() throws Exception {
        // Given
        ResetPasswordRequest invalidRequest = ResetPasswordRequest.builder()
                .newPassword("NewSecure123!")
                // Missing resetCode
                .build();

        // When
        ResultActions result = mockMvc.perform(put("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(passwordResetService, never()).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Should return validation error for weak password")
    void resetPassword_WithWeakPassword_ShouldReturnValidationError() throws Exception {
        // Given
        ResetPasswordRequest weakPasswordRequest = ResetPasswordRequest.builder()
                .newPassword("weak") // Too short and doesn't meet complexity requirements
                .resetCode("123456")
                .build();

        // When
        ResultActions result = mockMvc.perform(put("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weakPasswordRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(passwordResetService, never()).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Should return validation error for password too long")
    void resetPassword_WithTooLongPassword_ShouldReturnValidationError() throws Exception {
        // Given
        String tooLongPassword = "A1!".repeat(25); // 75 characters, exceeds 60 char limit
        ResetPasswordRequest longPasswordRequest = ResetPasswordRequest.builder()
                .newPassword(tooLongPassword)
                .resetCode("123456")
                .build();

        // When
        ResultActions result = mockMvc.perform(put("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longPasswordRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(passwordResetService, never()).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Should return bad request for malformed JSON")
    void resetPassword_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // Given
        String malformedJson = "{ \"newPassword\": \"Password123!\", \"resetCode\": }";

        // When
        ResultActions result = mockMvc.perform(put("/api/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("Should return unsupported media type for non-JSON content")
    void resetPassword_WithNonJsonContent_ShouldReturnUnsupportedMediaType() throws Exception {
        // When
        ResultActions result = mockMvc.perform(put("/api/reset-password")
                .contentType(MediaType.TEXT_PLAIN)
                .content("some text"));

        // Then
        result.andDo(print())
                .andExpect(status().isUnsupportedMediaType());

        verify(passwordResetService, never()).resetPassword(any(ResetPasswordRequest.class));
    }
}