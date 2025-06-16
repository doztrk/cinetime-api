package com.Cinetime.controller;

import com.Cinetime.payload.dto.request.ResetCodeRequest;
import com.Cinetime.payload.dto.response.PasswordResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.security.JwtAuthFilter;
import com.Cinetime.security.UserDetailsServiceImpl;
import com.Cinetime.service.passwordbusiness.PasswordResetService;
import com.Cinetime.service.UserService;
import com.Cinetime.service.authentication.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.security.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class ValidateResetPasswordCodeTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock ALL dependencies of UserController
    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private PasswordResetService passwordResetService;


    @Autowired
    private ObjectMapper objectMapper;

    private ResetCodeRequest validRequest;
    private ResponseMessage<PasswordResponse> successResponse;
    private ResponseMessage<PasswordResponse> errorResponse;

    @BeforeEach
    void setUp() {
        validRequest = ResetCodeRequest.builder()
                .resetCode("123456")
                .build();

        PasswordResponse passwordResponse = PasswordResponse.builder()
                .email("test@example.com")
                .build();

        successResponse = ResponseMessage.<PasswordResponse>builder()
                .message("Reset code is valid")
                .httpStatus(HttpStatus.OK)
                .object(passwordResponse)
                .build();

        errorResponse = ResponseMessage.<PasswordResponse>builder()
                .message("Invalid or expired reset code")
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();
    }

    @Test
    @DisplayName("Should return 200 OK when reset code is valid")
    void validateResetPasswordCode_ValidRequest_Returns200() throws Exception {
        // Given
        when(passwordResetService.validateResetPasswordCode(any(ResetCodeRequest.class)))
                .thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/api/validate-reset-password-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reset code is valid"))
                .andExpect(jsonPath("$.object.email").value("test@example.com"));

        verify(passwordResetService).validateResetPasswordCode(any(ResetCodeRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when reset code is invalid")
    void validateResetPasswordCode_InvalidRequest_Returns400() throws Exception {
        // Given
        when(passwordResetService.validateResetPasswordCode(any(ResetCodeRequest.class)))
                .thenReturn(errorResponse);

        // When & Then
        mockMvc.perform(post("/api/validate-reset-password-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invalid or expired reset code"))
                .andExpect(jsonPath("$.object").doesNotExist());
    }

    @Test
    @DisplayName("Should return 400 when resetCode is empty")
    void validateResetPasswordCode_EmptyResetCode_Returns400() throws Exception {
        // Given
        ResetCodeRequest emptyRequest = ResetCodeRequest.builder()
                .resetCode("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/validate-reset-password-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should return 400 when request body is missing")
    void validateResetPasswordCode_MissingBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/validate-reset-password-code")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}