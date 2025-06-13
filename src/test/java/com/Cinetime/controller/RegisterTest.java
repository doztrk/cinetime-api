package com.Cinetime.controller;

import com.Cinetime.enums.Gender;
import com.Cinetime.payload.dto.request.user.UserRegisterRequest;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for UserController with proper CSRF handling
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegisterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRegisterRequest validRequest;
    private ResponseMessage<BaseUserResponse> successResponse;

    @BeforeEach
    void setUp() {
        validRequest = UserRegisterRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .password("Password123!")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        BaseUserResponse userResponse = BaseUserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .build();

        successResponse = ResponseMessage.<BaseUserResponse>builder()
                .message("User created successfully")
                .httpStatus(HttpStatus.CREATED)
                .object(userResponse)
                .build();
    }

    @Test
    void register_WithValidData_ShouldReturnSuccessResponseMessage() throws Exception {
        // Arrange
        when(userService.register(any(UserRegisterRequest.class))).thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.httpStatus").value("CREATED"))
                .andExpect(jsonPath("$.object.id").value(1))
                .andExpect(jsonPath("$.object.name").value("John Doe"));
    }

    @Test
    void register_WithDuplicateUser_ShouldReturnConflict() throws Exception {
        // Arrange
        ResponseMessage<BaseUserResponse> conflictResponse = ResponseMessage.<BaseUserResponse>builder()
                .message(ErrorMessages.DUPLICATE_USER_PROPERTIES)
                .httpStatus(HttpStatus.CONFLICT)
                .object(null)
                .build();

        when(userService.register(any(UserRegisterRequest.class))).thenReturn(conflictResponse);

        // Act & Assert
        mockMvc.perform(post("/api/register")
                        .with(csrf()) // CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk()) //Since we are using ResponseMessage Wrapper, it returns ResponseObject as 200 OK. We need to use this to get the error message
                .andExpect(jsonPath("$.message").value(ErrorMessages.DUPLICATE_USER_PROPERTIES))
                .andExpect(jsonPath("$.httpStatus").value("CONFLICT"));
    }

    @Test
    void register_WithInvalidData_ShouldReturnValidationError() throws Exception {
        // Arrange - Invalid request
        UserRegisterRequest invalidRequest = UserRegisterRequest.builder()
                .firstname("") // Invalid
                .lastname("Doe")
                .email("invalid-email") // Invalid
                .phoneNumber("invalid") // Invalid
                .password("weak") // Invalid
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/register")
                        .with(csrf()) // CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest()) // Validation error
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.object.firstname").exists())
                .andExpect(jsonPath("$.object.email").exists())
                .andExpect(jsonPath("$.object.phoneNumber").exists())
                .andExpect(jsonPath("$.object.password").exists());
    }
}