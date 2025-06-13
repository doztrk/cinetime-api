package com.Cinetime.service;

import com.Cinetime.entity.Role;
import com.Cinetime.entity.User;
import com.Cinetime.enums.RoleName;
import com.Cinetime.payload.authentication.LoginRequest;
import com.Cinetime.payload.dto.response.AuthResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.security.JwtUtils;
import com.Cinetime.security.UserDetailsImpl;
import com.Cinetime.service.authentication.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthenticationService authenticationService;

    private LoginRequest validLoginRequest;
    private User testUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest();
        validLoginRequest.setPhoneNumber("(555) 123-4567");
        validLoginRequest.setPassword("ValidPass123!");

        testUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .phoneNumber("(555) 123-4567")
                .role(new Role(RoleName.MEMBER))
                .build();

        userDetails = new UserDetailsImpl(testUser);
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnSuccessResponse() {
        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtUtils.generateToken(mockAuthentication)).thenReturn("mock-jwt-token");

        // Act
        ResponseMessage<AuthResponse> result = authenticationService.authenticateUser(validLoginRequest);

        // Assert
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Authentication successful");
        assertThat(result.getObject()).isNotNull();


        AuthResponse authResponse = result.getObject();
        assertThat(authResponse.getToken()).isEqualTo("Bearer mock-jwt-token");
        assertThat(authResponse.getName()).isEqualTo("John");
        assertThat(authResponse.getPhone()).isEqualTo("(555) 123-4567");
        assertThat(authResponse.getRoles()).containsExactly("MEMBER");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateToken(mockAuthentication);
    }

    @Test
    void authenticateUser_WithInvalidCredentials_ShouldReturnUnauthorizedResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        ResponseMessage<AuthResponse> result = authenticationService.authenticateUser(validLoginRequest);

        // Assert
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.getMessage()).isEqualTo("Invalid credentials");
        assertThat(result.getObject()).isNull();

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void authenticateUser_WithSystemError_ShouldReturnInternalServerError() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        ResponseMessage<AuthResponse> result = authenticationService.authenticateUser(validLoginRequest);

        // Assert
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getMessage()).isEqualTo("Authentication failed");
        assertThat(result.getObject()).isNull();
    }

    @Test
    void authenticateUser_ShouldExtractCorrectCredentials() {
        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtUtils.generateToken(any())).thenReturn("token");

        // Act
        authenticationService.authenticateUser(validLoginRequest);

        // Assert
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());

        UsernamePasswordAuthenticationToken captured = captor.getValue();
        assertThat(captured.getPrincipal()).isEqualTo("(555) 123-4567");
        assertThat(captured.getCredentials()).isEqualTo("ValidPass123!");
    }
}