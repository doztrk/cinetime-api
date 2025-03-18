package com.Cinetime.service.authentication;

import com.Cinetime.payload.authentication.LoginRequest;
import com.Cinetime.payload.response.AuthResponse;
import com.Cinetime.security.JwtUtils;
import com.Cinetime.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {


    public final JwtUtils jwtUtils;
    public final AuthenticationManager authenticationManager;

    public ResponseEntity<AuthResponse> authenticateUser(LoginRequest loginRequest) {

        String phoneNumber = loginRequest.getPhoneNumber();
        String password = loginRequest.getPassword();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(phoneNumber, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = "Bearer " + jwtUtils.generateToken(authentication);


        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .build());
    }
}
