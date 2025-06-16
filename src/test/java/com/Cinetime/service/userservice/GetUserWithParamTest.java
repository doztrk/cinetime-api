package com.Cinetime.service.userservice;

import com.Cinetime.entity.Role;
import com.Cinetime.entity.User;
import com.Cinetime.enums.Gender;
import com.Cinetime.enums.RoleName;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - getUserWithParam Tests")
class GetUserWithParamTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser1;
    private User testUser2;
    private BaseUserResponse userResponse1;
    private BaseUserResponse userResponse2;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        // Create test roles
        Role memberRole = new Role();
        memberRole.setId(1L);
        memberRole.setRoleName(RoleName.MEMBER);

        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setRoleName(RoleName.ADMIN);

        // Create test users
        testUser1 = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@test.com")
                .phoneNumber("(123) 456-7890")
                .password("hashedPassword1")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .role(memberRole)
                .builtIn(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser2 = User.builder()
                .id(2L)
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@test.com")
                .phoneNumber("(987) 654-3210")
                .password("hashedPassword2")
                .gender(Gender.FEMALE)
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .role(adminRole)
                .builtIn(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create test responses
        userResponse1 = BaseUserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@test.com")
                .phoneNumber("(123) 456-7890")
                .build();

        userResponse2 = BaseUserResponse.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane.smith@test.com")
                .phoneNumber("(987) 654-3210")
                .build();

        // Create test pageable
        testPageable = PageRequest.of(0, 10, Sort.by("id").ascending());
    }

    @Test
    @DisplayName("Should return successful response with paginated users when search query matches")
    void shouldReturnSuccessfulResponseWithPaginatedUsers() {
        // Given
        String searchQuery = "john";
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        List<User> userList = Arrays.asList(testUser1);
        Page<User> userPage = new PageImpl<>(userList, testPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(userRepository.searchUsers(searchQuery, testPageable)).thenReturn(userPage);
        when(userMapper.mapUserToBaseUserResponse(testUser1)).thenReturn(userResponse1);

        // When
        ResponseMessage<Page<BaseUserResponse>> result = userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Users retrieved successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(1);
        assertThat(result.getObject().getContent().get(0)).isEqualTo(userResponse1);
        assertThat(result.getObject().getTotalElements()).isEqualTo(1);

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(userRepository).searchUsers(searchQuery, testPageable);
        verify(userMapper).mapUserToBaseUserResponse(testUser1);
    }

    @Test
    @DisplayName("Should return successful response with all users when search query is null")
    void shouldReturnAllUsersWhenSearchQueryIsNull() {
        // Given
        String searchQuery = null;
        int page = 0, size = 10;
        String sort = "firstname", type = "asc";

        List<User> userList = Arrays.asList(testUser1, testUser2);
        Page<User> userPage = new PageImpl<>(userList, testPageable, 2);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(userRepository.searchUsers(searchQuery, testPageable)).thenReturn(userPage);
        when(userMapper.mapUserToBaseUserResponse(testUser1)).thenReturn(userResponse1);
        when(userMapper.mapUserToBaseUserResponse(testUser2)).thenReturn(userResponse2);

        // When
        ResponseMessage<Page<BaseUserResponse>> result = userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Users retrieved successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getContent()).containsExactly(userResponse1, userResponse2);

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(userRepository).searchUsers(searchQuery, testPageable);
        verify(userMapper, times(2)).mapUserToBaseUserResponse(any(User.class));
    }

    @Test
    @DisplayName("Should return successful response with empty page when no users match search criteria")
    void shouldReturnEmptyPageWhenNoUsersMatch() {
        // Given
        String searchQuery = "nonexistent";
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(userRepository.searchUsers(searchQuery, testPageable)).thenReturn(emptyPage);

        // When
        ResponseMessage<Page<BaseUserResponse>> result = userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Users retrieved successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).isEmpty();
        assertThat(result.getObject().getTotalElements()).isEqualTo(0);

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(userRepository).searchUsers(searchQuery, testPageable);
        verify(userMapper, never()).mapUserToBaseUserResponse(any(User.class));
    }

    @Test
    @DisplayName("Should handle different pagination parameters correctly")
    void shouldHandleDifferentPaginationParameters() {
        // Given
        String searchQuery = "test";
        int page = 2, size = 5;
        String sort = "lastname", type = "desc";

        Pageable customPageable = PageRequest.of(page, size, Sort.by("lastname").descending());
        List<User> userList = Arrays.asList(testUser1);
        Page<User> userPage = new PageImpl<>(userList, customPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(userRepository.searchUsers(searchQuery, customPageable)).thenReturn(userPage);
        when(userMapper.mapUserToBaseUserResponse(testUser1)).thenReturn(userResponse1);

        // When
        ResponseMessage<Page<BaseUserResponse>> result = userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject().getNumber()).isEqualTo(2);
        assertThat(result.getObject().getSize()).isEqualTo(5);

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(userRepository).searchUsers(searchQuery, customPageable);
    }

    @Test
    @DisplayName("Should handle empty string search query successfully")
    void shouldHandleEmptyStringSearchQuery() {
        // Given
        String searchQuery = "";
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        List<User> userList = Arrays.asList(testUser1, testUser2);
        Page<User> userPage = new PageImpl<>(userList, testPageable, 2);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(userRepository.searchUsers(searchQuery, testPageable)).thenReturn(userPage);
        when(userMapper.mapUserToBaseUserResponse(testUser1)).thenReturn(userResponse1);
        when(userMapper.mapUserToBaseUserResponse(testUser2)).thenReturn(userResponse2);

        // When
        ResponseMessage<Page<BaseUserResponse>> result = userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject().getContent()).hasSize(2);

        verify(userRepository).searchUsers(searchQuery, testPageable);
    }

    @Test
    @DisplayName("Should return internal server error when repository throws unexpected exception")
    void shouldReturnInternalServerErrorOnRepositoryException() {
        // Given
        String searchQuery = "test";
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(userRepository.searchUsers(searchQuery, testPageable))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        ResponseMessage<Page<BaseUserResponse>> result = userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getMessage()).isEqualTo("Failed to retrieve users: Database connection failed");
        assertThat(result.getObject()).isNull();

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(userRepository).searchUsers(searchQuery, testPageable);
        verify(userMapper, never()).mapUserToBaseUserResponse(any(User.class));
    }

    @Test
    @DisplayName("Should return internal server error when PageableHelper throws exception")
    void shouldReturnInternalServerErrorOnPageableHelperException() {
        // Given
        String searchQuery = "test";
        int page = -1, size = 10; // Invalid page number
        String sort = "id", type = "asc";

        when(pageableHelper.pageableSort(page, size, sort, type))
                .thenThrow(new IllegalArgumentException("Invalid page number"));

        // When
        ResponseMessage<Page<BaseUserResponse>> result = userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getMessage()).isEqualTo("Failed to retrieve users: Invalid page number");
        assertThat(result.getObject()).isNull();

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(userRepository, never()).searchUsers(any(), any());
        verify(userMapper, never()).mapUserToBaseUserResponse(any(User.class));
    }

    @Test
    @DisplayName("Should return internal server error when UserMapper throws exception")
    void shouldReturnInternalServerErrorOnMapperException() {
        // Given
        String searchQuery = "test";
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        List<User> userList = Arrays.asList(testUser1);
        Page<User> userPage = new PageImpl<>(userList, testPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(userRepository.searchUsers(searchQuery, testPageable)).thenReturn(userPage);
        when(userMapper.mapUserToBaseUserResponse(testUser1))
                .thenThrow(new RuntimeException("Mapping failed"));

        // When
        ResponseMessage<Page<BaseUserResponse>> result = userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getMessage()).isEqualTo("Failed to retrieve users: Mapping failed");
        assertThat(result.getObject()).isNull();

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(userRepository).searchUsers(searchQuery, testPageable);
        verify(userMapper).mapUserToBaseUserResponse(testUser1);
    }

    @Test
    @DisplayName("Should verify exact parameter passing to dependencies")
    void shouldVerifyExactParameterPassing() {
        // Given
        String searchQuery = "specific query";
        int page = 3, size = 15;
        String sort = "email", type = "desc";

        Pageable expectedPageable = PageRequest.of(page, size, Sort.by("email").descending());
        Page<User> userPage = new PageImpl<>(Collections.emptyList(), expectedPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(expectedPageable);
        when(userRepository.searchUsers(searchQuery, expectedPageable)).thenReturn(userPage);

        // When
        userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then
        verify(pageableHelper).pageableSort(eq(page), eq(size), eq(sort), eq(type));
        verify(userRepository).searchUsers(eq(searchQuery), eq(expectedPageable));
    }

    @Test
    @DisplayName("Should handle null response from repository gracefully")
    void shouldHandleNullResponseFromRepository() {
        // Given
        String searchQuery = "test";
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(userRepository.searchUsers(searchQuery, testPageable)).thenReturn(null);

        // When
        ResponseMessage<Page<BaseUserResponse>> result = userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getMessage()).contains("Failed to retrieve users");
        assertThat(result.getObject()).isNull();

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(userRepository).searchUsers(searchQuery, testPageable);
    }

    @Test
    @DisplayName("Should maintain response message structure consistency")
    void shouldMaintainResponseMessageStructureConsistency() {
        // Given
        String searchQuery = "test";
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        List<User> userList = Arrays.asList(testUser1);
        Page<User> userPage = new PageImpl<>(userList, testPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(userRepository.searchUsers(searchQuery, testPageable)).thenReturn(userPage);
        when(userMapper.mapUserToBaseUserResponse(testUser1)).thenReturn(userResponse1);

        // When
        ResponseMessage<Page<BaseUserResponse>> result = userService.getUserWithParam(searchQuery, page, size, sort, type);

        // Then - Verify ResponseMessage structure
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isNotNull().isNotEmpty();
        assertThat(result.getHttpStatus()).isNotNull();
        assertThat(result.getObject()).isNotNull();

        // Verify Page structure
        Page<BaseUserResponse> responsePage = result.getObject();
        assertThat(responsePage.getContent()).isNotNull();
        assertThat(responsePage.getTotalElements()).isNotNegative();
        assertThat(responsePage.getNumber()).isNotNegative();
        assertThat(responsePage.getSize()).isPositive();
    }
}