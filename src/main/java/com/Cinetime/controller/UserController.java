package com.Cinetime.controller;

import com.Cinetime.payload.dto.UserRequest;
import com.Cinetime.payload.response.BaseUserResponse;
import com.Cinetime.payload.response.ResponseMessage;
import com.Cinetime.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ResponseMessage<BaseUserResponse>> register(@RequestBody @Valid UserRequest userRegisterDTO) {
        return ResponseEntity.ok(userService.register(userRegisterDTO));
    }
}
