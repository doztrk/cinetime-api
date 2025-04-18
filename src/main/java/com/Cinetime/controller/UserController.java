package com.Cinetime.controller;

import com.Cinetime.payload.authentication.LoginRequest;
import com.Cinetime.payload.dto.*;
import com.Cinetime.payload.dto.request.user.AbstractUserRequest;
import com.Cinetime.payload.dto.request.user.UserRequest;
import com.Cinetime.payload.dto.request.user.UserRequestWithPasswordOnly;
import com.Cinetime.payload.dto.request.user.UserUpdateRequest;
import com.Cinetime.payload.response.AuthResponse;
import com.Cinetime.payload.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.passwordbusiness.PasswordResetService;
import com.Cinetime.service.UserService;
import com.Cinetime.service.authentication.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    //U02
    @PostMapping("/register")
    public ResponseMessage<BaseUserResponse> register(@RequestBody @Valid AbstractUserRequest userRegisterDTO) {
        return userService.register(userRegisterDTO);
    }

    //U01
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        return authenticationService.authenticateUser(loginRequest);
    }


    //U03
    @PostMapping("/forgot-password")
    public ResponseMessage<?> generateResetPasswordCode(@RequestBody @Valid ForgotPasswordRequest request) {
        return passwordResetService.generateResetPasswordCode(request);
    }

    //U04 -- After validateResetPasswordCode, this code should run
    //Dokumantasyonda mantik hatasi var. Eger kullanici forgot-passworddan buraya gelecekse zaten old passwordu bilmiyordur.
    //TODO:Onun icin change-password isimli bir endpoint yazacagiz.
    @PutMapping("/reset-password")
    public ResponseMessage<?> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordDTO) {
        return passwordResetService.resetPassword(resetPasswordDTO);
    }

    //U04 -- After Forgot Password, this endpoint should run
    @PostMapping("/validate-reset-password-code")
    public ResponseMessage<?> validateResetPasswordCode(@RequestBody @Valid ResetCodeRequest resetCodeDTO) {
        return passwordResetService.validateResetPasswordCode(resetCodeDTO);
    }

    //U05
    @PostMapping("/users/auth")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MEMBER')")
    public ResponseMessage<BaseUserResponse> createUser(@RequestBody @Valid UserRequest userCreateDTO) {
        return userService.createUser(userCreateDTO);
    }

    //U06
    @PutMapping("/users/auth")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MEMBER')")
    public ResponseMessage<BaseUserResponse> updateUser(@RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        return userService.updateUser(userUpdateRequest);
    }

    //U07
    @DeleteMapping("/users/auth")
    @PreAuthorize("hasAnyRole('MEMBER')")
    public ResponseMessage<BaseUserResponse> deleteUser(@RequestBody @Valid UserRequestWithPasswordOnly request) {
        return userService.deleteUser(request);
    }

    //U08
    @GetMapping("/users/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public Page<BaseUserResponse> getUserWithParam(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "asc") String type) {
        return userService.getUserWithParam(q, page, size, sort, type);
    }


    @GetMapping("/debug-auth")
    public String debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "User: " + auth.getName() +
                "\nAuthenticated: " + auth.isAuthenticated() +
                "\nAuthorities: " + auth.getAuthorities();
    }

    @GetMapping("/debug-token")
    public String debugToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return "Token header: " + token;
    }
}
