package com.Cinetime.controller;

import com.Cinetime.payload.authentication.LoginRequest;
import com.Cinetime.payload.dto.ResetCodeRequest;
import com.Cinetime.payload.dto.ForgotPasswordRequest;
import com.Cinetime.payload.dto.ResetPasswordRequest;
import com.Cinetime.payload.dto.UserRequest;
import com.Cinetime.payload.response.AuthResponse;
import com.Cinetime.payload.response.BaseUserResponse;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.service.passwordbusiness.PasswordResetService;
import com.Cinetime.service.UserService;
import com.Cinetime.service.authentication.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseMessage<BaseUserResponse> register(@RequestBody @Valid UserRequest userRegisterDTO) {
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
    public ResponseMessage<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return passwordResetService.resetPassword(request);
    }

    //U04 -- After Forgot Password, this endpoint should run
    @PostMapping("/validate-reset-password-code")
    public ResponseMessage<?> validateResetPasswordCode(@RequestBody @Valid ResetCodeRequest request) {
        return passwordResetService.validateResetPasswordCode(request);
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
