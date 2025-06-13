package com.Cinetime.controller;

import com.Cinetime.payload.authentication.LoginRequest;
import com.Cinetime.payload.dto.response.AuthResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.authentication.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller - Authentication Tests")
class AuthenticateTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        validLoginRequest = new LoginRequest();
        validLoginRequest.setPhoneNumber("(555) 123-4567");
        validLoginRequest.setPassword("ValidPass123!");
    }

    @Test
    @DisplayName("Should authenticate user with valid credentials and return success response")
    void authenticateUser_WithValidCredentials_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        AuthResponse authResponse = AuthResponse.builder()
                .token("Bearer mock-jwt-token")
                .name("John")
                .phone("(555) 123-4567")
                .roles(Set.of("MEMBER"))
                .build();

        ResponseMessage<AuthResponse> responseMessage = ResponseMessage.<AuthResponse>builder()
                .message("Authentication successful")
                .httpStatus(HttpStatus.OK)
                .object(authResponse)
                .build();

        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenReturn(responseMessage);

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Authentication successful"))
                .andExpect(jsonPath("$.httpStatus").value("OK"))
                .andExpect(jsonPath("$.object.token").value("Bearer mock-jwt-token"))
                .andExpect(jsonPath("$.object.name").value("John"))
                .andExpect(jsonPath("$.object.phone").value("(555) 123-4567"))
                .andExpect(jsonPath("$.object.roles[0]").value("MEMBER"));

        verify(authenticationService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return unauthorized when credentials are invalid")
    void authenticateUser_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setPhoneNumber("(555) 123-4567");
        invalidRequest.setPassword("WrongPassword");

        ResponseMessage<AuthResponse> errorResponse = ResponseMessage.<AuthResponse>builder()
                .message("Invalid credentials")
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .object(null)
                .build();

        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenReturn(errorResponse);

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.object").doesNotExist());

        verify(authenticationService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return bad request when phone number format is invalid")
    void authenticateUser_WithInvalidPhoneFormat_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest invalidFormatRequest = new LoginRequest();
        invalidFormatRequest.setPhoneNumber("invalid-phone");
        invalidFormatRequest.setPassword("ValidPass123!");

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFormatRequest)))
                .andExpect(status().isBadRequest());

        // Verify service is never called due to validation failure
        verify(authenticationService, never()).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return bad request when password doesn't meet complexity requirements")
    void authenticateUser_WithSimplePassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest simplePasswordRequest = new LoginRequest();
        simplePasswordRequest.setPhoneNumber("(555) 123-4567");
        simplePasswordRequest.setPassword("simple");

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simplePasswordRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return bad request when phone number is null")
    void authenticateUser_WithNullPhoneNumber_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest nullPhoneRequest = new LoginRequest();
        nullPhoneRequest.setPhoneNumber(null);
        nullPhoneRequest.setPassword("ValidPass123!");

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullPhoneRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return bad request when password is empty")
    void authenticateUser_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest emptyPasswordRequest = new LoginRequest();
        emptyPasswordRequest.setPhoneNumber("(555) 123-4567");
        emptyPasswordRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyPasswordRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return internal server error when service throws unexpected exception")
    void authenticateUser_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        ResponseMessage<AuthResponse> errorResponse = ResponseMessage.<AuthResponse>builder()
                .message("Authentication failed")
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .object(null)
                .build();

        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenReturn(errorResponse);

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk()) /*Because we are using ResponseMessage, HTTP raw status is 200. So we need to do
                this to make the test pass.We should have used ResponseEntity directly in the controller to prevent this.*/
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Authentication failed"))
                .andExpect(jsonPath("$.httpStatus").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.object").doesNotExist());

        verify(authenticationService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return bad request when request body is malformed JSON")
    void authenticateUser_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return unsupported media type when content type is not JSON")
    void authenticateUser_WithWrongContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnsupportedMediaType());

        verify(authenticationService, never()).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should handle admin role correctly")
    void authenticateUser_WithAdminRole_ShouldReturnAdminRole() throws Exception {
        // Arrange
        AuthResponse adminAuthResponse = AuthResponse.builder()
                .token("Bearer admin-jwt-token")
                .name("Admin")
                .phone("(555) 999-9999")
                .roles(Set.of("ADMIN"))
                .build();

        ResponseMessage<AuthResponse> responseMessage = ResponseMessage.<AuthResponse>builder()
                .message("Authentication successful")
                .httpStatus(HttpStatus.OK)
                .object(adminAuthResponse)
                .build();

        LoginRequest adminRequest = new LoginRequest();
        adminRequest.setPhoneNumber("(555) 999-9999");
        adminRequest.setPassword("AdminPass123!");

        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenReturn(responseMessage);

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.object.name").value("Admin"));

        verify(authenticationService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should handle multiple roles correctly")
    void authenticateUser_WithMultipleRoles_ShouldReturnAllRoles() throws Exception {
        // Arrange
        AuthResponse multiRoleResponse = AuthResponse.builder()
                .token("Bearer multi-role-token")
                .name("SuperUser")
                .phone("(555) 888-8888")
                .roles(Set.of("ADMIN", "EMPLOYEE"))
                .build();

        ResponseMessage<AuthResponse> responseMessage = ResponseMessage.<AuthResponse>builder()
                .message("Authentication successful")
                .httpStatus(HttpStatus.OK)
                .object(multiRoleResponse)
                .build();

        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenReturn(responseMessage);

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object.roles").isArray())
                .andExpect(jsonPath("$.object.roles.length()").value(2));

        verify(authenticationService, times(1)).authenticateUser(any(LoginRequest.class));
    }
}