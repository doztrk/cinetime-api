package com.Cinetime.service;

import com.Cinetime.entity.User;
import com.Cinetime.enums.RoleName;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.helpers.TicketHelper;
import com.Cinetime.helpers.UniquePropertyValidator;
import com.Cinetime.helpers.UpdateUserHelper;
import com.Cinetime.payload.dto.request.user.AbstractUserRequest;
import com.Cinetime.payload.dto.request.user.UserRequest;
import com.Cinetime.payload.dto.request.user.UserRequestWithPasswordOnly;
import com.Cinetime.payload.dto.request.user.UserUpdateRequest;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.payload.response.BaseUserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniquePropertyValidator uniquePropertyValidator;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final UpdateUserHelper updateUserHelper;
    private final TicketHelper ticketHelper;
    private final PageableHelper pageableHelper;


    @Transactional
    public ResponseMessage<BaseUserResponse> register(AbstractUserRequest userRequest) {

        if (!uniquePropertyValidator.isRegistrationPropertiesUnique(userRequest.getEmail(), userRequest.getPhoneNumber())) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.DUPLICATE_USER_PROPERTIES)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
        ///DTO -> Entity
        User user = userMapper.mapUserRequestToUser(userRequest);

        //Soru: Role tipi icin her seferinde DB'ye sorgu atmak yerine nasil setleriz ?
        //Cevap ↴
        user.setRole(roleService.getRole(RoleName.ADMIN));

        //Password encoding
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setBuiltIn(false);
        User savedUser = userRepository.save(user);


        //Entity -> DTO


        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_CREATE)
                .httpStatus(HttpStatus.CREATED)
                .object(userMapper.mapUserToBaseUserResponse(savedUser))
                .build();
    }


    @Transactional
    public ResponseMessage<BaseUserResponse> createUser(UserRequest userCreateDTO) {
        boolean isUnique =
                uniquePropertyValidator
                        .isRegistrationPropertiesUnique(userCreateDTO.getEmail(), userCreateDTO.getPhoneNumber());

        if (!isUnique) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.DUPLICATE_USER_PROPERTIES)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
        User user = userMapper.mapUserRequestToUser(userCreateDTO);

        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        user.setRole(roleService.getRole(RoleName.MEMBER));

        user.setBuiltIn(false);

        userRepository.save(user);

        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_CREATE)
                .httpStatus(HttpStatus.OK)
                .object(userMapper.mapUserToBaseUserResponse(user))
                .build();
    }

    @Transactional
    public ResponseMessage<BaseUserResponse> updateUser(UserUpdateRequest userUpdateRequest) {

        //Istek gonderen kullaniciyi buluyoruz
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phoneNumber = authentication.getName();


        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database. This indicates a system error."));


        if (user.getBuiltIn().equals(true)) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.BUILTIN_USER_UPDATE)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        if (StringUtils.hasText(userUpdateRequest.getEmail()) &&
                !userUpdateRequest.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(userUpdateRequest.getEmail())) {

            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.DUPLICATE_USER_PROPERTIES)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        if (StringUtils.hasText(userUpdateRequest.getPhoneNumber()) &&
                !userUpdateRequest.getPhoneNumber().equals(user.getPhoneNumber()) &&
                userRepository.existsByPhoneNumber(userUpdateRequest.getPhoneNumber())) {

            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.DUPLICATE_USER_PROPERTIES)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }


        User updatedUser = updateUserHelper.updateUserIfUpdatesExistInRequest(userUpdateRequest, user);

        userRepository.save(updatedUser);

        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_UPDATE)
                .httpStatus(HttpStatus.OK)
                .object(userMapper.mapUserToBaseUserResponse(updatedUser))
                .build();
    }

    @Transactional
    public ResponseMessage<BaseUserResponse> deleteUser(UserRequestWithPasswordOnly request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String phoneNumber = authentication.getName();


        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

        if (user.isEmpty()) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message("Authenticated user not found in database. This indicates a system error.")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
        if (!passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.INVALID_PASSWORD)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        if (user.get().getBuiltIn()) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.BUILTIN_USER_DELETE)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        if (!ticketHelper.canDeleteUser(user.get())) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.USER_HAS_UNUSED_TICKETS)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .object(userMapper.mapUserToBaseUserResponse(user.get()))
                    .build();
        }

        BaseUserResponse userResponse = userMapper.mapUserToBaseUserResponse(user.get());

        userRepository.delete(user.get());

        SecurityContextHolder.clearContext();

        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_DELETE)
                .httpStatus(HttpStatus.OK)
                .object(userResponse)
                .build();

    }

    public Page<BaseUserResponse> getUserWithParam(String q, int page, int size, String sort, String type) {

        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        Page<User> users = userRepository.searchUsers(q, pageable);

        return users.map(userMapper::mapUserToBaseUserResponse);
    }
}
