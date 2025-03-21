package com.Cinetime.controller;

import com.Cinetime.payload.authentication.LoginRequest;
import com.Cinetime.payload.dto.UserRequest;
import com.Cinetime.payload.response.AuthResponse;
import com.Cinetime.payload.response.BaseUserResponse;
import com.Cinetime.payload.response.ResponseMessage;
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

    @PostMapping("/register")
    public ResponseEntity<ResponseMessage<BaseUserResponse>> register(@RequestBody @Valid UserRequest userRegisterDTO) {
        return ResponseEntity.ok(userService.register(userRegisterDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        return authenticationService.authenticateUser(loginRequest);
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
