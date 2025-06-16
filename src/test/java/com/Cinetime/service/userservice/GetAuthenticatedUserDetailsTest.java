package com.Cinetime.service.userservice;

import com.Cinetime.entity.Role;
import com.Cinetime.entity.User;
import com.Cinetime.enums.Gender;
import com.Cinetime.enums.RoleName;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.service.SecurityService;
import com.Cinetime.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAuthenticatedUserDetailsTest {

    @Mock
    private SecurityService securityService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private BaseUserResponse expectedResponse;

    @BeforeEach
    void setUp() {
        // Create test role
        Role memberRole = new Role();
        memberRole.setId(1L);
        memberRole.setRoleName(RoleName.MEMBER);

        // Create test user
        testUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .password("hashedPassword")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .role(memberRole)
                .builtIn(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create expected response
        expectedResponse = BaseUserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .build();
    }

    @Test
    void getAuthenticatedUserDetails_ShouldReturnUserDetails_WhenUserIsAuthenticated() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(expectedResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.getAuthenticatedUserDetails();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_FOUND);
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isEqualTo(expectedResponse);

        // Verify interactions
        verify(securityService, times(1)).getCurrentUser();
        verify(userMapper, times(1)).mapUserToBaseUserResponse(testUser);
    }

    @Test
    void getAuthenticatedUserDetails_ShouldMapUserCorrectly_WhenCalled() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(expectedResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.getAuthenticatedUserDetails();

        // Then
        BaseUserResponse actualResponse = result.getObject();
        assertThat(actualResponse.getId()).isEqualTo(testUser.getId());
        assertThat(actualResponse.getName()).isEqualTo("John Doe");
        assertThat(actualResponse.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(actualResponse.getPhoneNumber()).isEqualTo(testUser.getPhoneNumber());
    }

    @Test
    void getAuthenticatedUserDetails_ShouldHandleNullUser_WhenSecurityServiceReturnsNull() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(null);

        // When
        ResponseMessage<BaseUserResponse> result = userService.getAuthenticatedUserDetails();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Authenticated user not found in database. This indicates a system error.");
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getObject()).isNull();

        verify(securityService, times(1)).getCurrentUser();
        verify(userMapper, never()).mapUserToBaseUserResponse(any());
    }


    @Test
    void getAuthenticatedUserDetails_ShouldReturnCorrectStructure_Always() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(expectedResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.getAuthenticatedUserDetails();

        // Then
        assertThat(result.getMessage()).isNotNull();
        assertThat(result.getHttpStatus()).isNotNull();
        assertThat(result.getObject()).isNotNull();
    }
}