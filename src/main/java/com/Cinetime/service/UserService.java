package com.Cinetime.service;

import com.Cinetime.entity.User;
import com.Cinetime.enums.RoleName;
import com.Cinetime.helpers.UniquePropertyValidator;
import com.Cinetime.payload.dto.UserRequest;
import com.Cinetime.payload.mappers.UserMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.repo.RoleRepository;
import com.Cinetime.repo.UserRepository;
import com.Cinetime.payload.response.BaseUserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniquePropertyValidator uniquePropertyValidator;
    private final UserMapper userMapper;
    private final RoleService roleService;


    @Transactional
    public ResponseMessage<BaseUserResponse> register(UserRequest userRequest) {

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

        User savedUser = userRepository.save(user);


        //Entity -> DTO


        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_CREATE)
                .httpStatus(HttpStatus.CREATED)
                .object(userMapper.mapUserToBaseUserResponse(savedUser))
                .build();
    }


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

        userRepository.save(user);

        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_CREATE)
                .httpStatus(HttpStatus.OK)
                .object(userMapper.mapUserToBaseUserResponse(user))
                .build();
    }
}
