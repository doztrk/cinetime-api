package com.Cinetime.controller;

import com.Cinetime.enums.Gender;
import com.Cinetime.enums.RoleName;
import com.Cinetime.payload.dto.request.user.UserCreateRequest;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.security.JwtAuthFilter;
import com.Cinetime.security.JwtUtils;
import com.Cinetime.security.UserDetailsServiceImpl;
import com.Cinetime.service.UserService;
import com.Cinetime.service.authentication.AuthenticationService;
import com.Cinetime.service.passwordbusiness.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class CreateUserTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private PasswordResetService passwordResetService;

    // Mock all security-related dependencies
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private UserCreateRequest validUserRequest;
    private BaseUserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        validUserRequest = UserCreateRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .password("SecurePass123!")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .role(RoleName.MEMBER)
                .builtIn(false)
                .build();

        mockUserResponse = BaseUserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .build();
    }

    @Test
    @DisplayName("Should create user successfully with ADMIN role")
    @WithMockUser(roles = {"ADMIN"})
    void shouldCreateUserSuccessfullyWithAdminRole() throws Exception {
        // Given
        ResponseMessage<BaseUserResponse> mockResponse = ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_CREATE)
                .httpStatus(HttpStatus.OK)
                .object(mockUserResponse)
                .build();

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_CREATE))
                .andExpect(jsonPath("$.httpStatus").value("OK"))
                .andExpect(jsonPath("$.object.id").value(1L))
                .andExpect(jsonPath("$.object.name").value("John Doe"))
                .andExpect(jsonPath("$.object.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.object.phoneNumber").value("(555) 123-4567"));
    }

    @Test
    @DisplayName("Should create user successfully with EMPLOYEE role")
    @WithMockUser(roles = {"EMPLOYEE"})
    void shouldCreateUserSuccessfullyWithEmployeeRole() throws Exception {
        // Given
        ResponseMessage<BaseUserResponse> mockResponse = ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_CREATE)
                .httpStatus(HttpStatus.OK)
                .object(mockUserResponse)
                .build();

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_CREATE));
    }

    @Test
    @DisplayName("Should return 403 FORBIDDEN when user has MEMBER role")
    @WithMockUser(roles = {"MEMBER"})
    void shouldReturnForbiddenWhenUserHasMemberRole() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 UNAUTHORIZED when user is not authenticated")
    void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST for invalid email format")
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        // Given
        validUserRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST for invalid phone number format")
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnBadRequestForInvalidPhoneNumber() throws Exception {
        // Given
        validUserRequest.setPhoneNumber("invalid-phone");

        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST for weak password")
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnBadRequestForWeakPassword() throws Exception {
        // Given
        validUserRequest.setPassword("weak");

        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST for missing required fields")
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnBadRequestForMissingRequiredFields() throws Exception {
        // Given
        UserCreateRequest incompleteRequest = UserCreateRequest.builder()
                .firstname("John")
                // Missing lastname, email, phone, password, gender, dateOfBirth
                .build();

        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should return 409 CONFLICT when user already exists")
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnConflictWhenUserAlreadyExists() throws Exception {
        // Given
        ResponseMessage<BaseUserResponse> mockResponse = ResponseMessage.<BaseUserResponse>builder()
                .message(ErrorMessages.DUPLICATE_USER_PROPERTIES)
                .httpStatus(HttpStatus.CONFLICT)
                .build();

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isOk()) // Your service returns the status, not HTTP status
                .andExpect(jsonPath("$.message").value(ErrorMessages.DUPLICATE_USER_PROPERTIES))
                .andExpect(jsonPath("$.httpStatus").value("CONFLICT"));
    }

    @Test
    @DisplayName("Should handle malformed JSON gracefully")
    @WithMockUser(roles = {"ADMIN"})
    void shouldHandleMalformedJsonGracefully() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate date of birth is in the past")
    @WithMockUser(roles = {"ADMIN"})
    void shouldValidateDateOfBirthIsInThePast() throws Exception {
        // Given
        validUserRequest.setDateOfBirth(LocalDate.now().plusDays(1)); // Future date

        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should accept valid edge case values")
    @WithMockUser(roles = {"ADMIN"})
    void shouldAcceptValidEdgeCaseValues() throws Exception {
        // Given
        validUserRequest.setFirstname("A".repeat(3)); // Minimum length
        validUserRequest.setLastname("B".repeat(20)); // Maximum length
        validUserRequest.setPassword("Aa1!aaaa"); // Minimum valid password

        ResponseMessage<BaseUserResponse> mockResponse = ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_CREATE)
                .httpStatus(HttpStatus.OK)
                .object(mockUserResponse)
                .build();

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/users/auth")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_CREATE));
    }
}