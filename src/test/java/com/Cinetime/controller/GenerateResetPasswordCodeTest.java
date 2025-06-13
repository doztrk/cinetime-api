package com.Cinetime.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.Cinetime.payload.dto.request.ForgotPasswordRequest;
import com.Cinetime.payload.dto.response.PasswordResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.passwordbusiness.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.security.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class GenerateResetPasswordCodeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordResetService passwordResetService;

    @Autowired
    private ObjectMapper objectMapper;

    private ForgotPasswordRequest validRequest;
    private ResponseMessage<PasswordResponse> successResponse;

    @BeforeEach
    void setUp() {
        validRequest = new ForgotPasswordRequest();
        validRequest.setEmail("test@example.com");

        PasswordResponse passwordResponse = PasswordResponse.builder()
                .message("Reset code sent")
                .build();

        successResponse = ResponseMessage.<PasswordResponse>builder()
                .message("Password reset code has been sent successfully to this email")
                .httpStatus(HttpStatus.OK)
                .object(passwordResponse)
                .build();
    }

    @Test
    void forgotPassword_WithValidEmail_ShouldReturnSuccessResponse() throws Exception {
        // Given
        when(passwordResetService.generateResetPasswordCode(any(ForgotPasswordRequest.class)))
                .thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/api/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Password reset code has been sent successfully to this email"))
                .andExpect(jsonPath("$.httpStatus").value("OK"));

        // Verify the service was called
        verify(passwordResetService, times(1))
                .generateResetPasswordCode(any(ForgotPasswordRequest.class));
    }
}