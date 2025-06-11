package com.Cinetime.service;

import com.Cinetime.entity.Role;
import com.Cinetime.entity.User;
import com.Cinetime.enums.Gender;
import com.Cinetime.enums.RoleName;
import com.Cinetime.helpers.UniquePropertyValidator;
import com.Cinetime.payload.dto.request.user.UserRegisterRequest;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
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

    private UserRegisterRequest validUserRequest;
    private User mockUser;
    private Role memberRole;
    private BaseUserResponse expectedResponse;

    @BeforeEach
    void setUp() {

        validUserRequest = UserRegisterRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 555-5555")
                .password("Password123!")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .build();

        memberRole = new Role();
        memberRole.setId(1L);
        memberRole.setRoleName(RoleName.MEMBER);

        mockUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 555-5555")
                .password("Password123!")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .role(memberRole)
                .build();

        expectedResponse = BaseUserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("(555) 555-5555")
                .build();
    }


    @Test
    @DisplayName("Should successfully register a new user when all data is valid")
    void register_WithValidData_ShouldReturnSuccessResponse() {
        //Arrange
        when(uniquePropertyValidator.uniquePropertyChecker(anyString(), anyString())).thenReturn(true);
        when(userMapper.mapUserRequestToUser(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(roleService.getRole(RoleName.MEMBER)).thenReturn(memberRole);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.mapUserToBaseUserResponse(any(User.class))).thenReturn(expectedResponse);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword123!");


        //Act
        ResponseMessage<BaseUserResponse> result = userService.register(validUserRequest);


        //Assert
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.USER_CREATE);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getId()).isEqualTo(1L);
        assertThat(result.getObject().getName()).isEqualTo("John Doe");
        assertThat(result.getObject().getEmail()).isEqualTo("john.doe@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User actualUserSaved = userCaptor.getValue();

        // Verify the service actually set these fields correctly
        assertThat(actualUserSaved.getRole()).isEqualTo(memberRole);
        assertThat(actualUserSaved.getBuiltIn()).isFalse();
        assertThat(actualUserSaved.getPassword()).isEqualTo("encodedPassword123!");
        //Verify interactions
        verify(uniquePropertyValidator).uniquePropertyChecker("john.doe@example.com", "(555) 555-5555");
        verify(userMapper).mapUserRequestToUser(validUserRequest);
        verify(roleService).getRole(RoleName.MEMBER);
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
        verify(userMapper).mapUserToBaseUserResponse(mockUser);
    }


    @Test
    @DisplayName("Should return conflict when user already exists")
    void register_WithDuplicateUser_ShouldReturnConflictResponse() {

        //Arrange
        String duplicateEmail = "john.doe@example.com";
        String duplicatePhoneNumber = "(555) 555-5555";

        when(uniquePropertyValidator.uniquePropertyChecker(duplicateEmail, duplicatePhoneNumber)).thenReturn(false);


        //Act
        ResponseMessage<BaseUserResponse> result = userService.register(validUserRequest);

        //Assert
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.DUPLICATE_USER_PROPERTIES);

        verify(uniquePropertyValidator).uniquePropertyChecker(duplicateEmail, duplicatePhoneNumber);
    }

    @Test
    @DisplayName("Should set correct user properties during registration")
    void register_WithValidData_ShouldSetCorrectUserProperties() {
        //Arrange
        when(uniquePropertyValidator.uniquePropertyChecker(anyString(), anyString())).thenReturn(true);
        when(userMapper.mapUserRequestToUser(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(roleService.getRole(RoleName.MEMBER)).thenReturn(memberRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.mapUserToBaseUserResponse(any(User.class))).thenReturn(expectedResponse);

        //Act
        userService.register(validUserRequest);

        //Assert
        verify(userRepository).save(argThat(user -> {
            assertThat(user.getFirstname()).isEqualTo("John");
            assertThat(user.getLastname()).isEqualTo("Doe");
            assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(user.getPhoneNumber()).isEqualTo("(555) 555-5555");
            assertThat(user.getPassword()).isEqualTo("encodedPassword");
            assertThat(user.getGender()).isEqualTo(Gender.MALE);
            assertThat(user.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
            assertThat(user.getRole()).isEqualTo(memberRole);
            assertThat(user.getBuiltIn()).isFalse();
            return true;
        }));

    }

    @Test
    @DisplayName("Should handle password encoding correctly")
    void register_ShouldEncodePasswordProperly() {
        // Arrange
        String rawPassword = "Password123!";
        String encodedPassword = "encodedPassword";

        when(uniquePropertyValidator.uniquePropertyChecker(anyString(), anyString())).thenReturn(true);
        when(userMapper.mapUserRequestToUser(any(UserRegisterRequest.class))).thenReturn(mockUser);
        when(roleService.getRole(RoleName.MEMBER)).thenReturn(memberRole);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.mapUserToBaseUserResponse(any(User.class))).thenReturn(expectedResponse);

        // Act
        userService.register(validUserRequest);

        // Assert
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals(encodedPassword)
        ));
    }
}
