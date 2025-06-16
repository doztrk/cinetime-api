package com.Cinetime.service.userservice;

import com.Cinetime.entity.Role;
import com.Cinetime.entity.User;
import com.Cinetime.enums.Gender;
import com.Cinetime.enums.RoleName;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.helpers.SecurityHelper;
import com.Cinetime.helpers.TicketHelper;
import com.Cinetime.helpers.UniquePropertyValidator;
import com.Cinetime.helpers.UpdateUserHelper;
import com.Cinetime.payload.dto.request.user.UserUpdateRequest;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.service.RoleService;
import com.Cinetime.service.SecurityService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserByIdTest {

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

    @Mock
    private UpdateUserHelper updateUserHelper;

    @Mock
    private TicketHelper ticketHelper;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private UserService userService;

    private User existingUser;
    private UserUpdateRequest updateRequest;
    private BaseUserResponse baseUserResponse;
    private Role memberRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Setup roles
        memberRole = new Role();
        memberRole.setId(1L);
        memberRole.setRoleName(RoleName.MEMBER);

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setRoleName(RoleName.ADMIN);

        // Setup existing user
        existingUser = User.builder()
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

        // Setup update request
        updateRequest = UserUpdateRequest.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("(555) 987-6543")
                .password("newPassword123!")
                .gender(Gender.FEMALE)
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .build();

        // Setup response
        baseUserResponse = BaseUserResponse.builder()
                .id(1L)
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .phoneNumber("(555) 987-6543")
                .build();
    }

    @Test
    @DisplayName("Should return NOT_FOUND when user does not exist")
    void updateUser_UserNotFound_ReturnsNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.USER_NOT_FOUND_WITH_ID);
        assertThat(result.getObject()).isNull();

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when employee tries to update non-member user")
    void updateUser_EmployeeUpdatingNonMember_ReturnsBadRequest() {
        // Given
        Long userId = 1L;
        existingUser.setRole(adminRole); // Non-member user
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        try (MockedStatic<SecurityHelper> securityHelper = mockStatic(SecurityHelper.class)) {
            securityHelper.when(SecurityHelper::getCurrentUserRole)
                    .thenReturn(Optional.of("EMPLOYEE"));

            // When
            ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getMessage()).isEqualTo(ErrorMessages.UNAUTHORIZED_USER_UPDATE);
            assertThat(result.getObject()).isNull();
        }
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when trying to update built-in user")
    void updateUser_BuiltInUser_ReturnsBadRequest() {
        // Given
        Long userId = 1L;
        existingUser.setBuiltIn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        try (MockedStatic<SecurityHelper> securityHelper = mockStatic(SecurityHelper.class)) {
            securityHelper.when(SecurityHelper::getCurrentUserRole)
                    .thenReturn(Optional.of("ADMIN"));

            // When
            ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getMessage()).isEqualTo(ErrorMessages.BUILTIN_USER_UPDATE);
            assertThat(result.getObject()).isNull();
        }
    }

    @Test
    @DisplayName("Should return CONFLICT when email is already taken by another user")
    void updateUser_EmailAlreadyTaken_ReturnsConflict() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(uniquePropertyValidator.isEmailUniqueForUpdate(updateRequest.getEmail(), userId))
                .thenReturn(false);

        try (MockedStatic<SecurityHelper> securityHelper = mockStatic(SecurityHelper.class)) {
            securityHelper.when(SecurityHelper::getCurrentUserRole)
                    .thenReturn(Optional.of("ADMIN"));

            // When
            ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(result.getMessage()).isEqualTo(ErrorMessages.DUPLICATE_EMAIL);
            assertThat(result.getObject()).isNull();
        }
    }

    @Test
    @DisplayName("Should return CONFLICT when phone number is already taken by another user")
    void updateUser_PhoneNumberAlreadyTaken_ReturnsConflict() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(uniquePropertyValidator.isEmailUniqueForUpdate(updateRequest.getEmail(), userId))
                .thenReturn(true);
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(updateRequest.getPhoneNumber(), userId))
                .thenReturn(false);

        try (MockedStatic<SecurityHelper> securityHelper = mockStatic(SecurityHelper.class)) {
            securityHelper.when(SecurityHelper::getCurrentUserRole)
                    .thenReturn(Optional.of("ADMIN"));

            // When
            ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(result.getMessage()).isEqualTo(ErrorMessages.DUPLICATE_PHONE_NUMBER);
            assertThat(result.getObject()).isNull();
        }
    }

    @Test
    @DisplayName("Should successfully update user when all validations pass")
    void updateUser_ValidUpdate_ReturnsSuccess() {
        // Given
        Long userId = 1L;
        User updatedUser = User.builder()
                .id(1L)
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("(555) 987-6543")
                .password("hashedNewPassword")
                .gender(Gender.FEMALE)
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .role(memberRole)
                .builtIn(false)
                .createdAt(existingUser.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(uniquePropertyValidator.isEmailUniqueForUpdate(updateRequest.getEmail(), userId))
                .thenReturn(true);
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(updateRequest.getPhoneNumber(), userId))
                .thenReturn(true);
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(updateRequest, existingUser))
                .thenReturn(updatedUser);
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.mapUserToBaseUserResponse(updatedUser)).thenReturn(baseUserResponse);

        try (MockedStatic<SecurityHelper> securityHelper = mockStatic(SecurityHelper.class)) {
            securityHelper.when(SecurityHelper::getCurrentUserRole)
                    .thenReturn(Optional.of("ADMIN"));

            // When
            ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
            assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_UPDATE);
            assertThat(result.getObject()).isNotNull();
            assertThat(result.getObject().getId()).isEqualTo(1L);
            assertThat(result.getObject().getName()).isEqualTo("Jane Smith");
            assertThat(result.getObject().getEmail()).isEqualTo("jane.smith@example.com");

            verify(userRepository).findById(userId);
            verify(uniquePropertyValidator).isEmailUniqueForUpdate(updateRequest.getEmail(), userId);
            verify(uniquePropertyValidator).isPhoneNumberUniqueForUpdate(updateRequest.getPhoneNumber(), userId);
            verify(updateUserHelper).updateUserIfUpdatesExistInRequest(updateRequest, existingUser);
            verify(userRepository).save(updatedUser);
            verify(userMapper).mapUserToBaseUserResponse(updatedUser);
        }
    }

    @Test
    @DisplayName("Should allow admin to update any user")
    void updateUser_AdminUser_CanUpdateAnyUser() {
        // Given
        Long userId = 1L;
        existingUser.setRole(adminRole); // Admin user being updated

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(uniquePropertyValidator.isEmailUniqueForUpdate(anyString(), anyLong())).thenReturn(true);
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(anyString(), anyLong())).thenReturn(true);
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(any(), any())).thenReturn(existingUser);
        when(userRepository.save(any())).thenReturn(existingUser);
        when(userMapper.mapUserToBaseUserResponse(any())).thenReturn(baseUserResponse);

        try (MockedStatic<SecurityHelper> securityHelper = mockStatic(SecurityHelper.class)) {
            securityHelper.when(SecurityHelper::getCurrentUserRole)
                    .thenReturn(Optional.of("ADMIN"));

            // When
            ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
            assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_UPDATE);
        }
    }

    @Test
    @DisplayName("Should allow employee to update member user")
    void updateUser_EmployeeUser_CanUpdateMemberUser() {
        // Given
        Long userId = 1L;
        existingUser.setRole(memberRole); // Member user being updated

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(uniquePropertyValidator.isEmailUniqueForUpdate(anyString(), anyLong())).thenReturn(true);
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(anyString(), anyLong())).thenReturn(true);
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(any(), any())).thenReturn(existingUser);
        when(userRepository.save(any())).thenReturn(existingUser);
        when(userMapper.mapUserToBaseUserResponse(any())).thenReturn(baseUserResponse);

        try (MockedStatic<SecurityHelper> securityHelper = mockStatic(SecurityHelper.class)) {
            securityHelper.when(SecurityHelper::getCurrentUserRole)
                    .thenReturn(Optional.of("EMPLOYEE"));

            // When
            ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
            assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_UPDATE);
        }
    }

    @Test
    @DisplayName("Should skip email validation when email is unchanged")
    void updateUser_UnchangedEmail_SkipsEmailValidation() {
        // Given
        Long userId = 1L;
        updateRequest.setEmail(existingUser.getEmail()); // Same email

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(anyString(), anyLong())).thenReturn(true);
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(any(), any())).thenReturn(existingUser);
        when(userRepository.save(any())).thenReturn(existingUser);
        when(userMapper.mapUserToBaseUserResponse(any())).thenReturn(baseUserResponse);

        try (MockedStatic<SecurityHelper> securityHelper = mockStatic(SecurityHelper.class)) {
            securityHelper.when(SecurityHelper::getCurrentUserRole)
                    .thenReturn(Optional.of("ADMIN"));

            // When
            ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
            verify(uniquePropertyValidator, never()).isEmailUniqueForUpdate(anyString(), anyLong());
            verify(uniquePropertyValidator).isPhoneNumberUniqueForUpdate(anyString(), anyLong());
        }
    }

    @Test
    @DisplayName("Should skip phone validation when phone is unchanged")
    void updateUser_UnchangedPhone_SkipsPhoneValidation() {
        // Given
        Long userId = 1L;
        updateRequest.setPhoneNumber(existingUser.getPhoneNumber()); // Same phone

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(uniquePropertyValidator.isEmailUniqueForUpdate(anyString(), anyLong())).thenReturn(true);
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(any(), any())).thenReturn(existingUser);
        when(userRepository.save(any())).thenReturn(existingUser);
        when(userMapper.mapUserToBaseUserResponse(any())).thenReturn(baseUserResponse);

        try (MockedStatic<SecurityHelper> securityHelper = mockStatic(SecurityHelper.class)) {
            securityHelper.when(SecurityHelper::getCurrentUserRole)
                    .thenReturn(Optional.of("ADMIN"));

            // When
            ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
            verify(uniquePropertyValidator).isEmailUniqueForUpdate(anyString(), anyLong());
            verify(uniquePropertyValidator, never()).isPhoneNumberUniqueForUpdate(anyString(), anyLong());
        }
    }

    @Test
    @DisplayName("Should handle empty security role gracefully")
    void updateUser_EmptySecurityRole_ContinuesExecution() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(uniquePropertyValidator.isEmailUniqueForUpdate(anyString(), anyLong())).thenReturn(true);
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(anyString(), anyLong())).thenReturn(true);
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(any(), any())).thenReturn(existingUser);
        when(userRepository.save(any())).thenReturn(existingUser);
        when(userMapper.mapUserToBaseUserResponse(any())).thenReturn(baseUserResponse);

        try (MockedStatic<SecurityHelper> securityHelper = mockStatic(SecurityHelper.class)) {
            securityHelper.when(SecurityHelper::getCurrentUserRole)
                    .thenReturn(Optional.empty()); // No role present

            // When
            ResponseMessage<BaseUserResponse> result = userService.updateUser(userId, updateRequest);

            // Then
            assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
            assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_UPDATE);
        }
    }
}