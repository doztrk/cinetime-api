package com.Cinetime.service.authentication;

import com.Cinetime.payload.authentication.LoginRequest;
import com.Cinetime.payload.dto.response.AuthResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.security.JwtUtils;
import com.Cinetime.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    public ResponseMessage<AuthResponse> authenticateUser(LoginRequest loginRequest) {

        try {
            String phoneNumber = loginRequest.getPhoneNumber();
            String password = loginRequest.getPassword();

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(phoneNumber, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = "Bearer " + jwtUtils.generateToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            Set<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))//burada string turune ceviren getAuthority methodu ile yapiyoruz
                    .collect(Collectors.toSet());

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .name(userDetails.getUser().getFirstname())
                    .phone(userDetails.getUser().getPhoneNumber())
                    .roles(roles)
                    .build();
            return ResponseMessage.<AuthResponse>builder()
                    .message("Authentication successful")
                    .httpStatus(HttpStatus.OK)
                    .object(authResponse)
                    .build();


        } catch (BadCredentialsException e) {
            return ResponseMessage.<AuthResponse>builder()
                    .message(ErrorMessages.BAD_CREDENTIALS)
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .build();

        } catch (Exception e) {
            return ResponseMessage.<AuthResponse>builder()
                    .message("Authentication failed")
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
