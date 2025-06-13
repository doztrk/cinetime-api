package com.Cinetime.service;

import com.Cinetime.entity.User;
import com.Cinetime.enums.RoleName;
import com.Cinetime.helpers.*;
import com.Cinetime.payload.dto.request.user.UserCreateRequest;
import com.Cinetime.payload.dto.request.user.UserRegisterRequest;
import com.Cinetime.payload.dto.request.user.UserRequestWithPasswordOnly;
import com.Cinetime.payload.dto.request.user.UserUpdateRequest;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.payload.dto.response.BaseUserResponse;
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
    private final SecurityService securityService;


    @Transactional
    public ResponseMessage<BaseUserResponse> register(UserRegisterRequest userRequest) {

        if (!uniquePropertyValidator.uniquePropertyChecker(userRequest.getEmail(), userRequest.getPhoneNumber())) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.DUPLICATE_USER_PROPERTIES)
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }
        ///DTO -> Entity
        User user = userMapper.mapUserRequestToUser(userRequest);



        //Soru: Role tipi icin her seferinde DB'ye sorgu atmak yerine nasil setleriz ?
        //Cevap ↴
        user.setRole(roleService.getRole(RoleName.MEMBER));

        //Password encoding
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

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
    public ResponseMessage<BaseUserResponse> createUser(UserCreateRequest userCreateDTO) {


        boolean isUnique =
                uniquePropertyValidator
                        .uniquePropertyChecker(userCreateDTO.getEmail(), userCreateDTO.getPhoneNumber());

        if (!isUnique) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.DUPLICATE_USER_PROPERTIES)
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }
        User user = userMapper.mapUserRequestToUser(userCreateDTO);

        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));


        //1.a) Eger admin ise kullanicinin built-in ve rolelerini set edebilmesi lazim.Dolayisiyla authenticatede bakmamiz gerekiyor
        if (SecurityHelper.hasRole(String.valueOf(RoleName.ADMIN))) {
            user.setBuiltIn(userCreateDTO.isBuiltIn());
            user.setRole(roleService.getRole(userCreateDTO.getRole())); //Cache kullanarak requestten gelen rolu setliyoruz.
        } else {
            user.setBuiltIn(false);
            user.setRole(roleService.getRole(RoleName.MEMBER));
        }

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


        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);

        if (userOptional.isEmpty()) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.AUTHENTICATION_NOT_FOUND)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        User user = userOptional.get();

        if (user.getBuiltIn().equals(true)) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.BUILTIN_USER_UPDATE)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        //Kullanici email'ini guncelleyecekse bu mevcuttakiyle ayni emaili olmamali ve DB'de verdigi email ile ayni bir email kayitli olmamali
        //Hata mesaji donduruyoruz
        if (StringUtils.hasText(userUpdateRequest.getEmail()) &&
                !userUpdateRequest.getEmail().equals(user.getEmail()) &&
                !uniquePropertyValidator.isEmailUniqueForUpdate(userUpdateRequest.getEmail(), user.getId())) {

            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.DUPLICATE_EMAIL)
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }

        //Same as above but for phone
        if (StringUtils.hasText(userUpdateRequest.getPhoneNumber()) &&
                !userUpdateRequest.getPhoneNumber().equals(user.getPhoneNumber()) &&
                !uniquePropertyValidator.isPhoneNumberUniqueForUpdate(userUpdateRequest.getPhoneNumber(), user.getId())) {

            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.DUPLICATE_PHONE_NUMBER)
                    .httpStatus(HttpStatus.CONFLICT)
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

        String phoneNumber = SecurityHelper.getCurrentUserPhoneNumber();


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

        SecurityContextHolder.clearContext(); //Sildikten sonra securitycontexti temizliyoruz

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

    public ResponseMessage<BaseUserResponse> getUserById(Long userId) {


        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.USER_NOT_FOUND_WITH_ID)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        User user = userOptional.get();

        BaseUserResponse userResponse = userMapper.mapUserToBaseUserResponse(user);

        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_FOUND)
                .httpStatus(HttpStatus.OK)
                .object(userResponse)
                .build();
    }


    //Method overload
    @Transactional
    public ResponseMessage<BaseUserResponse> updateUser(Long userId, UserUpdateRequest userUpdateRequest) {


        Optional<User> userOptional = userRepository.findById(userId);
        //Checking if userId exists in DB
        if (userOptional.isEmpty()) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.USER_NOT_FOUND_WITH_ID)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }


        User userToBeUpdated = userOptional.get();

        Optional<String> authenticatedUserRoleOptional = SecurityHelper.getCurrentUserRole();

        //Checking if user is authorized to update user (Employee type can only update member type)
        if (authenticatedUserRoleOptional.isPresent()) {
            String authenticatedUserRole = authenticatedUserRoleOptional.get();

            if (authenticatedUserRole.equals(String.valueOf(RoleName.EMPLOYEE)) &&
                    !userToBeUpdated.getRole().getRoleName().equals(RoleName.MEMBER)) {
                return ResponseMessage.<BaseUserResponse>builder()
                        .message(ErrorMessages.UNAUTHORIZED_USER_UPDATE)
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build();
            }
        }
        //Checking if user has built-in role
        if (userToBeUpdated.getBuiltIn()) {
            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.BUILTIN_USER_UPDATE)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }


        //If updateRequest has email and email is different from userToBeUpdated's current email and this new email
        //is already taken by another user in the database
        if (StringUtils.hasText(userUpdateRequest.getEmail()) &&
                !userUpdateRequest.getEmail().equals(userToBeUpdated.getEmail()) &&
                !uniquePropertyValidator.isEmailUniqueForUpdate(userUpdateRequest.getEmail(), userId)) {

            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.DUPLICATE_EMAIL)
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }

        //Same as above but for phone
        if (StringUtils.hasText(userUpdateRequest.getPhoneNumber()) &&
                !userUpdateRequest.getPhoneNumber().equals(userToBeUpdated.getPhoneNumber()) &&
                !uniquePropertyValidator.isPhoneNumberUniqueForUpdate(userUpdateRequest.getPhoneNumber(), userId)) {

            return ResponseMessage.<BaseUserResponse>builder()
                    .message(ErrorMessages.DUPLICATE_PHONE_NUMBER)
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }

        //Update for fields that changes been made
        User user = updateUserHelper.updateUserIfUpdatesExistInRequest(userUpdateRequest, userToBeUpdated);

        //Save it to DB
        User userUpdated = userRepository.save(user);

        BaseUserResponse userResponse = userMapper.mapUserToBaseUserResponse(userUpdated);


        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_UPDATE)
                .httpStatus(HttpStatus.OK)
                .object(userResponse)
                .build();
    }

    public ResponseMessage<BaseUserResponse> getAuthenticatedUserDetails() {

        User user = securityService.getCurrentUser();

        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_FOUND)
                .httpStatus(HttpStatus.OK)
                .object(userMapper.mapUserToBaseUserResponse(user))
                .build();
    }
}
