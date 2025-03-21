package com.Cinetime.service;

import com.Cinetime.entity.Role;
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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniquePropertyValidator uniquePropertyValidator;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
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

}
