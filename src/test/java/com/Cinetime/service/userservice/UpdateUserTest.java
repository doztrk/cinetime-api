package com.Cinetime.service.userservice;

import com.Cinetime.entity.Role;
import com.Cinetime.entity.User;
import com.Cinetime.enums.Gender;
import com.Cinetime.enums.RoleName;
import com.Cinetime.helpers.UniquePropertyValidator;
import com.Cinetime.helpers.UpdateUserHelper;
import com.Cinetime.payload.dto.request.user.UserUpdateRequest;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.service.SecurityService;
import com.Cinetime.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UniquePropertyValidator uniquePropertyValidator;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UpdateUserHelper updateUserHelper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserUpdateRequest updateRequest;
    private BaseUserResponse baseUserResponse;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        updateRequest = createUpdateRequest();
        baseUserResponse = createBaseUserResponse();
    }

    @Test
    @DisplayName("Should successfully update user when all validations pass")
    void updateUser_Success() {
        // Given

        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(uniquePropertyValidator.isEmailUniqueForUpdate(anyString(), anyLong())).thenReturn(true);
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(anyString(), anyLong())).thenReturn(true);
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(updateRequest, testUser)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(baseUserResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.updateUser(updateRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_UPDATE);
        assertThat(result.getObject()).isEqualTo(baseUserResponse);

        verify(securityService).getCurrentUser();
        verify(uniquePropertyValidator).isEmailUniqueForUpdate(updateRequest.getEmail(), testUser.getId());
        verify(uniquePropertyValidator).isPhoneNumberUniqueForUpdate(updateRequest.getPhoneNumber(), testUser.getId());
        verify(updateUserHelper).updateUserIfUpdatesExistInRequest(updateRequest, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).mapUserToBaseUserResponse(testUser);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when user is built-in")
    void updateUser_BuiltInUser() {
        // Given
        testUser.setBuiltIn(true);
        when(securityService.getCurrentUser()).thenReturn(testUser);

        // When
        ResponseMessage<BaseUserResponse> result = userService.updateUser(updateRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.BUILTIN_USER_UPDATE);
        assertThat(result.getObject()).isNull();

        verify(securityService).getCurrentUser();
        verifyNoInteractions(uniquePropertyValidator, updateUserHelper, userMapper, userRepository);
    }

    @Test
    @DisplayName("Should return CONFLICT when email is duplicate")
    void updateUser_DuplicateEmail() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(uniquePropertyValidator.isEmailUniqueForUpdate(updateRequest.getEmail(), testUser.getId())).thenReturn(false);

        // When
        ResponseMessage<BaseUserResponse> result = userService.updateUser(updateRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.DUPLICATE_EMAIL);
        assertThat(result.getObject()).isNull();

        verify(securityService).getCurrentUser();
        verify(uniquePropertyValidator).isEmailUniqueForUpdate(updateRequest.getEmail(), testUser.getId());
        // Should not check phone number since email validation failed first
        verify(uniquePropertyValidator, never()).isPhoneNumberUniqueForUpdate(any(), anyLong());
        verifyNoInteractions(updateUserHelper, userMapper, userRepository);
    }

    @Test
    @DisplayName("Should return CONFLICT when phone number is duplicate")
    void updateUser_DuplicatePhoneNumber() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(uniquePropertyValidator.isEmailUniqueForUpdate(updateRequest.getEmail(), testUser.getId())).thenReturn(true);
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(updateRequest.getPhoneNumber(), testUser.getId())).thenReturn(false);

        // When
        ResponseMessage<BaseUserResponse> result = userService.updateUser(updateRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.DUPLICATE_PHONE_NUMBER);
        assertThat(result.getObject()).isNull();

        verify(securityService).getCurrentUser();
        verify(uniquePropertyValidator).isEmailUniqueForUpdate(updateRequest.getEmail(), testUser.getId());
        verify(uniquePropertyValidator).isPhoneNumberUniqueForUpdate(updateRequest.getPhoneNumber(), testUser.getId());
        verifyNoInteractions(updateUserHelper, userMapper, userRepository);
    }

    @Test
    @DisplayName("Should skip email validation when email is same as current")
    void updateUser_SameEmail_SkipsValidation() {
        // Given
        updateRequest.setEmail(testUser.getEmail()); // Same email
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(updateRequest.getPhoneNumber(), testUser.getId())).thenReturn(true);
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(updateRequest, testUser)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(baseUserResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.updateUser(updateRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        // Email uniqueness should not be checked since it's the same
        verify(uniquePropertyValidator, never()).isEmailUniqueForUpdate(any(), anyLong());
        verify(uniquePropertyValidator).isPhoneNumberUniqueForUpdate(updateRequest.getPhoneNumber(), testUser.getId());
    }

    @Test
    @DisplayName("Should skip validation when email is null or blank")
    void updateUser_BlankEmail_SkipsValidation() {
        // Given
        updateRequest.setEmail("   "); // Blank email
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(updateRequest.getPhoneNumber(), testUser.getId())).thenReturn(true);
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(updateRequest, testUser)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(baseUserResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.updateUser(updateRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        // Email uniqueness should not be checked for blank email
        verify(uniquePropertyValidator, never()).isEmailUniqueForUpdate(any(), anyLong());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when user is built-in")
    void updateUser_BuiltInUser_ReturnsBadRequest() {
        // Given
        testUser.setBuiltIn(true);  // Built-in user
        when(securityService.getCurrentUser()).thenReturn(testUser);

        // When
        ResponseMessage<BaseUserResponse> result = userService.updateUser(updateRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.BUILTIN_USER_UPDATE);
        assertThat(result.getObject()).isNull();

        // Verify NO further processing occurs
        verify(securityService).getCurrentUser();
        verifyNoInteractions(uniquePropertyValidator, updateUserHelper, userRepository, userMapper);
    }

    @Test
    @DisplayName("Should skip both validations when email and phone are unchanged")
    void updateUser_NoChanges_SkipsValidations() {
        // Given
        updateRequest.setEmail(testUser.getEmail());
        updateRequest.setPhoneNumber(testUser.getPhoneNumber());
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(updateRequest, testUser)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(baseUserResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.updateUser(updateRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        // No uniqueness validations should be performed
        verifyNoInteractions(uniquePropertyValidator);
    }

    @Test
    @DisplayName("Should update user even when helper returns same instance")
    void updateUser_HelperReturnsSameInstance() {
        // Given
        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(uniquePropertyValidator.isEmailUniqueForUpdate(updateRequest.getEmail(), testUser.getId())).thenReturn(true);
        when(uniquePropertyValidator.isPhoneNumberUniqueForUpdate(updateRequest.getPhoneNumber(), testUser.getId())).thenReturn(true);
        // Helper returns the same instance (common scenario)
        when(updateUserHelper.updateUserIfUpdatesExistInRequest(updateRequest, testUser)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.mapUserToBaseUserResponse(testUser)).thenReturn(baseUserResponse);

        // When
        ResponseMessage<BaseUserResponse> result = userService.updateUser(updateRequest);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        // Verify save is still called even with same instance
        verify(userRepository).save(testUser);
    }

    // Helper methods to create test data
    private User createTestUser() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleName(RoleName.MEMBER);

        User user = new User();
        user.setId(1L);
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("john.doe@example.com");
        user.setPhoneNumber("(555) 123-4567");
        user.setPassword("hashedPassword");
        user.setGender(Gender.MALE);
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setRole(role);
        user.setBuiltIn(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    private UserUpdateRequest createUpdateRequest() {
        return UserUpdateRequest.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("(555) 987-6543")
                .gender(Gender.FEMALE)
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .build();
    }

    private BaseUserResponse createBaseUserResponse() {
        return BaseUserResponse.builder()
                .id(1L)
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .phoneNumber("(555) 987-6543")
                .build();
    }
}