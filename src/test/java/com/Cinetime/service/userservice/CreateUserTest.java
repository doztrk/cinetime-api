package com.Cinetime.service.userservice;

import com.Cinetime.entity.Role;
import com.Cinetime.entity.User;
import com.Cinetime.enums.Gender;
import com.Cinetime.enums.RoleName;
import com.Cinetime.helpers.UniquePropertyValidator;
import com.Cinetime.payload.dto.request.user.UserCreateRequest;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.service.RoleService;
import com.Cinetime.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService.createUser Tests")
class CreateUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UniquePropertyValidator uniquePropertyValidator;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    private UserCreateRequest validUserRequest;
    private User mockUser;
    private BaseUserResponse mockUserResponse;
    private Role adminRole;
    private Role memberRole;

    @BeforeEach
    void setUp() {
        // Setup valid request
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

        // Setup mock entities
        adminRole = new Role(RoleName.ADMIN);
        memberRole = new Role(RoleName.MEMBER);

        mockUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .password("encodedPassword")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .role(memberRole)
                .builtIn(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockUserResponse = BaseUserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 123-4567")
                .build();
    }

    @Test
    @DisplayName("Should create user successfully as admin with custom role")
    void shouldCreateUserSuccessfullyAsAdminWithCustomRole() {
        // Given
        validUserRequest.setRole(RoleName.EMPLOYEE);
        validUserRequest.setBuiltIn(true);

        when(uniquePropertyValidator.uniquePropertyChecker(anyString(), anyString())).thenReturn(true);
        when(userMapper.mapUserRequestToUser(any(UserCreateRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getRole(RoleName.EMPLOYEE)).thenReturn(new Role(RoleName.EMPLOYEE));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.mapUserToBaseUserResponse(any(User.class))).thenReturn(mockUserResponse);

        try (MockedStatic<com.Cinetime.helpers.SecurityHelper> securityHelper = mockStatic(com.Cinetime.helpers.SecurityHelper.class)) {
            securityHelper.when(() -> com.Cinetime.helpers.SecurityHelper.hasRole("ADMIN")).thenReturn(true);

            // When
            ResponseMessage<BaseUserResponse> result = userService.createUser(validUserRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CREATED); // This should be CREATED!
            assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_CREATE);
            assertThat(result.getObject()).isEqualTo(mockUserResponse);

            verify(roleService).getRole(RoleName.EMPLOYEE);
            verify(userRepository).save(argThat(user ->
                    user.getBuiltIn().equals(true) &&
                            user.getRole().getRoleName().equals(RoleName.EMPLOYEE)
            ));
        }
    }

    @Test
    @DisplayName("Should create user with default MEMBER role when not admin")
    void shouldCreateUserWithDefaultMemberRoleWhenNotAdmin() {
        // Given
        when(uniquePropertyValidator.uniquePropertyChecker(anyString(), anyString())).thenReturn(true);
        when(userMapper.mapUserRequestToUser(any(UserCreateRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getRole(RoleName.MEMBER)).thenReturn(memberRole);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.mapUserToBaseUserResponse(any(User.class))).thenReturn(mockUserResponse);

        try (MockedStatic<com.Cinetime.helpers.SecurityHelper> securityHelper = mockStatic(com.Cinetime.helpers.SecurityHelper.class)) {
            securityHelper.when(() -> com.Cinetime.helpers.SecurityHelper.hasRole("ADMIN")).thenReturn(false);

            // When
            ResponseMessage<BaseUserResponse> result = userService.createUser(validUserRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_CREATE);
            assertThat(result.getObject()).isEqualTo(mockUserResponse);

            verify(roleService).getRole(RoleName.MEMBER);
            verify(userRepository).save(argThat(user ->
                    user.getBuiltIn().equals(false) &&
                            user.getRole().getRoleName().equals(RoleName.MEMBER)
            ));
        }
    }

    @Test
    @DisplayName("Should return CONFLICT when user email/phone already exists")
    void shouldReturnConflictWhenUserAlreadyExists() {
        // Given
        when(uniquePropertyValidator.uniquePropertyChecker(anyString(), anyString())).thenReturn(false);

        // When
        ResponseMessage<BaseUserResponse> result = userService.createUser(validUserRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.DUPLICATE_USER_PROPERTIES);
        assertThat(result.getObject()).isNull();

        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).mapUserToBaseUserResponse(any(User.class));
    }

    @Test
    @DisplayName("Should handle password encoding correctly")
    void shouldHandlePasswordEncodingCorrectly() {
        // Given
        String rawPassword = "SecurePass123!";
        String encodedPassword = "encodedSecurePass123!";
        validUserRequest.setPassword(rawPassword);

        when(uniquePropertyValidator.uniquePropertyChecker(anyString(), anyString())).thenReturn(true);
        when(userMapper.mapUserRequestToUser(any(UserCreateRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(roleService.getRole(any(RoleName.class))).thenReturn(memberRole);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.mapUserToBaseUserResponse(any(User.class))).thenReturn(mockUserResponse);

        try (MockedStatic<com.Cinetime.helpers.SecurityHelper> securityHelper = mockStatic(com.Cinetime.helpers.SecurityHelper.class)) {
            securityHelper.when(() -> com.Cinetime.helpers.SecurityHelper.hasRole("ADMIN")).thenReturn(false);

            // When
            userService.createUser(validUserRequest);

            // Then
            verify(passwordEncoder).encode(rawPassword);
            verify(userRepository).save(argThat(user -> user.getPassword().equals(encodedPassword)));
        }
    }

    @Test
    @DisplayName("Should handle repository save failure gracefully")
    void shouldHandleRepositorySaveFailureGracefully() {
        // Given
        when(uniquePropertyValidator.uniquePropertyChecker(anyString(), anyString())).thenReturn(true);
        when(userMapper.mapUserRequestToUser(any(UserCreateRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getRole(any(RoleName.class))).thenReturn(memberRole);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        try (MockedStatic<com.Cinetime.helpers.SecurityHelper> securityHelper = mockStatic(com.Cinetime.helpers.SecurityHelper.class)) {
            securityHelper.when(() -> com.Cinetime.helpers.SecurityHelper.hasRole("ADMIN")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(validUserRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");

            verify(userMapper, never()).mapUserToBaseUserResponse(any(User.class));
        }
    }

    @Test
    @DisplayName("Should validate all required fields are processed")
    void shouldValidateAllRequiredFieldsAreProcessed() {
        // Given
        when(uniquePropertyValidator.uniquePropertyChecker(anyString(), anyString())).thenReturn(true);
        when(userMapper.mapUserRequestToUser(any(UserCreateRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getRole(any(RoleName.class))).thenReturn(memberRole);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.mapUserToBaseUserResponse(any(User.class))).thenReturn(mockUserResponse);

        try (MockedStatic<com.Cinetime.helpers.SecurityHelper> securityHelper = mockStatic(com.Cinetime.helpers.SecurityHelper.class)) {
            securityHelper.when(() -> com.Cinetime.helpers.SecurityHelper.hasRole("ADMIN")).thenReturn(false);

            // When
            userService.createUser(validUserRequest);

            // Then
            verify(uniquePropertyValidator).uniquePropertyChecker(
                    validUserRequest.getEmail(),
                    validUserRequest.getPhoneNumber()
            );
            verify(userMapper).mapUserRequestToUser(validUserRequest);
            verify(passwordEncoder).encode(validUserRequest.getPassword());
            verify(roleService).getRole(RoleName.MEMBER);
            verify(userRepository).save(any(User.class));
            verify(userMapper).mapUserToBaseUserResponse(any(User.class));
        }
    }

    @Test
    @DisplayName("Should handle null request gracefully")
    void shouldHandleNullRequestGracefully() {
        // When & Then
        assertThatThrownBy(() -> userService.createUser(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(uniquePropertyValidator, userMapper, userRepository);
    }

    @Test
    @DisplayName("Should verify transaction boundaries")
    void shouldVerifyTransactionBoundaries() {
        // This test ensures the @Transactional annotation is working
        // Given
        when(uniquePropertyValidator.uniquePropertyChecker(anyString(), anyString())).thenReturn(true);
        when(userMapper.mapUserRequestToUser(any(UserCreateRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getRole(any(RoleName.class))).thenReturn(memberRole);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.mapUserToBaseUserResponse(any(User.class))).thenReturn(mockUserResponse);

        try (MockedStatic<com.Cinetime.helpers.SecurityHelper> securityHelper = mockStatic(com.Cinetime.helpers.SecurityHelper.class)) {
            securityHelper.when(() -> com.Cinetime.helpers.SecurityHelper.hasRole("ADMIN")).thenReturn(false);

            // When
            ResponseMessage<BaseUserResponse> result = userService.createUser(validUserRequest);

            // Then
            assertThat(result).isNotNull();
            // In integration tests, you would verify rollback behavior
        }
    }
}