package com.Cinetime.service.userservice;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.service.UserService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - getUserById Tests")
class GetUserByIdTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private BaseUserResponse expectedUserResponse;
    private Long validUserId;
    private Long invalidUserId;

    @BeforeEach
    void setUp() {
        validUserId = 1L;
        invalidUserId = 999L;

        testUser = User.builder()
                .id(validUserId)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .build();

        expectedUserResponse = BaseUserResponse.builder()
                .id(validUserId)
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .build();
    }

    @Test
    @DisplayName("Should return user successfully when user exists")
    void getUserById_WhenUserExists_ShouldReturnUserSuccessfully() {
        // Given
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(testUser));
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(expectedUserResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.getUserById(validUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_FOUND);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getId()).isEqualTo(validUserId);
        assertThat(result.getObject().getName()).isEqualTo("John Doe");
        assertThat(result.getObject().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getObject().getPhoneNumber()).isEqualTo("(555) 123-4567");

        // Verify interactions
        verify(userRepository, times(1)).findById(validUserId);
        verify(userMapper, times(1)).mapUserToBaseUserResponse(testUser);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when user does not exist")
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        // When
        ResponseMessage<BaseUserResponse> result = userService.getUserById(invalidUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.USER_NOT_FOUND_WITH_ID);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(userRepository, times(1)).findById(invalidUserId);
        verify(userMapper, never()).mapUserToBaseUserResponse(any());
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Should handle null userId gracefully")
    void getUserById_WhenUserIdIsNull_ShouldReturnNotFound() {
        // Given
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // When
        ResponseMessage<BaseUserResponse> result = userService.getUserById(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.USER_NOT_FOUND_WITH_ID);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(userRepository, times(1)).findById(null);
        verify(userMapper, never()).mapUserToBaseUserResponse(any());
    }

    @Test
    @DisplayName("Should verify mapper is called with correct user object")
    void getUserById_WhenUserExists_ShouldCallMapperWithCorrectUser() {
        // Given
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(testUser));
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(expectedUserResponse);

        // When
        userService.getUserById(validUserId);

        // Then
        verify(userMapper).mapUserToBaseUserResponse(testUser);
        // Ensure the exact same user object is passed to mapper
        verify(userMapper).mapUserToBaseUserResponse(argThat(user ->
                user.getId().equals(validUserId) &&
                        user.getFirstname().equals("John") &&
                        user.getLastname().equals("Doe")
        ));
    }

    @Test
    @DisplayName("Should return different user data for different valid IDs")
    void getUserById_WithDifferentValidIds_ShouldReturnCorrespondingUsers() {
        // Given - Second user
        Long secondUserId = 2L;
        User secondUser = User.builder()
                .id(secondUserId)
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("(555) 987-6543")
                .build();

        BaseUserResponse secondUserResponse = BaseUserResponse.builder()
                .id(secondUserId)
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .phoneNumber("(555) 987-6543")
                .build();

        when(userRepository.findById(secondUserId)).thenReturn(Optional.of(secondUser));
        when(userMapper.mapUserToBaseUserResponse(secondUser)).thenReturn(secondUserResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.getUserById(secondUserId);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject().getId()).isEqualTo(secondUserId);
        assertThat(result.getObject().getName()).isEqualTo("Jane Smith");
        assertThat(result.getObject().getEmail()).isEqualTo("jane.smith@example.com");
    }
}