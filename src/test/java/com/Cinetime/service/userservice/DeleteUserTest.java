package com.Cinetime.service.userservice;

import com.Cinetime.entity.User;
import com.Cinetime.enums.Gender;
import com.Cinetime.helpers.TicketHelper;
import com.Cinetime.payload.dto.request.user.UserRequestWithPasswordOnly;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.service.SecurityService;
import com.Cinetime.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TicketHelper ticketHelper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequestWithPasswordOnly request;
    private BaseUserResponse expectedUserResponse;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .password("$2a$10$encodedPassword")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .builtIn(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create request
        request = UserRequestWithPasswordOnly.builder()
                .password("plainPassword123!")
                .build();

        // Create expected response
        expectedUserResponse = BaseUserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .build();
    }

    @Test
    void deleteUser_WhenValidRequest_ShouldDeleteUserSuccessfully() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(passwordEncoder.matches("plainPassword123!", testUser.getPassword())).thenReturn(true);
        when(ticketHelper.canDeleteUser(testUser)).thenReturn(true);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(expectedUserResponse);

        // Static mock only needed for SecurityContextHolder.clearContext() call
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // When
            ResponseMessage<BaseUserResponse> result = userService.deleteUser(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
            assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_DELETE);
            assertThat(result.getObject()).isEqualTo(expectedUserResponse);

            // Verify interactions
            verify(securityService).getCurrentUser();
            verify(passwordEncoder).matches("plainPassword123!", testUser.getPassword());
            verify(ticketHelper).canDeleteUser(testUser);
            verify(userMapper).mapUserToBaseUserResponse(testUser);
            verify(userRepository).delete(testUser);

            // Verify the static method call
            mockedSecurityContext.verify(SecurityContextHolder::clearContext);
        }
    }

    @Test
    void deleteUser_WhenUserNotFound_ShouldReturnBadRequest() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(null);

        // When
        ResponseMessage<BaseUserResponse> result = userService.deleteUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo("Authenticated user not found in database. This indicates a system error.");
        assertThat(result.getObject()).isNull();

        // Verify no further interactions
        verify(securityService).getCurrentUser();
        verifyNoInteractions(passwordEncoder, ticketHelper, userMapper, userRepository);
        verifyNoMoreInteractions(securityService);
    }

    @Test
    void deleteUser_WhenInvalidPassword_ShouldReturnBadRequest() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(passwordEncoder.matches("plainPassword123!", testUser.getPassword())).thenReturn(false);

        // When
        ResponseMessage<BaseUserResponse> result = userService.deleteUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.INVALID_PASSWORD);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(securityService).getCurrentUser();
        verify(passwordEncoder).matches("plainPassword123!", testUser.getPassword());
        verifyNoInteractions(ticketHelper, userMapper, userRepository);
    }

    @Test
    void deleteUser_WhenBuiltInUser_ShouldReturnBadRequest() {
        // Given
        testUser.setBuiltIn(true);
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(passwordEncoder.matches("plainPassword123!", testUser.getPassword())).thenReturn(true);

        // When
        ResponseMessage<BaseUserResponse> result = userService.deleteUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.BUILTIN_USER_DELETE);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(securityService).getCurrentUser();
        verify(passwordEncoder).matches("plainPassword123!", testUser.getPassword());
        verifyNoInteractions(ticketHelper, userMapper, userRepository);
    }

    @Test
    void deleteUser_WhenUserHasUnusedTickets_ShouldReturnBadRequestWithUserObject() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(passwordEncoder.matches("plainPassword123!", testUser.getPassword())).thenReturn(true);
        when(ticketHelper.canDeleteUser(testUser)).thenReturn(false);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(expectedUserResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.deleteUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.USER_HAS_UNUSED_TICKETS);
        assertThat(result.getObject()).isEqualTo(expectedUserResponse);

        // Verify interactions
        verify(securityService).getCurrentUser();
        verify(passwordEncoder).matches("plainPassword123!", testUser.getPassword());
        verify(ticketHelper).canDeleteUser(testUser);
        verify(userMapper).mapUserToBaseUserResponse(testUser);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(passwordEncoder.matches("plainPassword123!", testUser.getPassword())).thenReturn(true);
        when(ticketHelper.canDeleteUser(testUser)).thenReturn(true);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(expectedUserResponse);
        doThrow(new RuntimeException("Database error")).when(userRepository).delete(testUser);

        // Static mock only needed for verifying clearContext is NOT called on failure
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
                userService.deleteUser(request);
            });

            // Verify interactions up to the point of failure
            verify(securityService).getCurrentUser();
            verify(passwordEncoder).matches("plainPassword123!", testUser.getPassword());
            verify(ticketHelper).canDeleteUser(testUser);
            verify(userMapper).mapUserToBaseUserResponse(testUser);
            verify(userRepository).delete(testUser);

            // SecurityContext should not be cleared if deletion fails
            mockedSecurityContext.verify(SecurityContextHolder::clearContext, never());
        }
    }

    @Test
    void deleteUser_VerifyExecutionOrder() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(passwordEncoder.matches("plainPassword123!", testUser.getPassword())).thenReturn(true);
        when(ticketHelper.canDeleteUser(testUser)).thenReturn(true);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(expectedUserResponse);

        // Static mock only for verifying clearContext timing
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // When
            userService.deleteUser(request);

            // Then - Verify execution order
            var inOrder = inOrder(securityService, passwordEncoder, ticketHelper, userMapper, userRepository);

            inOrder.verify(securityService).getCurrentUser();
            inOrder.verify(passwordEncoder).matches("plainPassword123!", testUser.getPassword());
            inOrder.verify(ticketHelper).canDeleteUser(testUser);
            inOrder.verify(userMapper).mapUserToBaseUserResponse(testUser);
            inOrder.verify(userRepository).delete(testUser);

            // SecurityContext clearing happens after repository deletion
            mockedSecurityContext.verify(SecurityContextHolder::clearContext);
        }
    }
}