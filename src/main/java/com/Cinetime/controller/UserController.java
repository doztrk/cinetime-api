package com.Cinetime.controller;

import com.Cinetime.payload.authentication.LoginRequest;
import com.Cinetime.payload.dto.request.ForgotPasswordRequest;
import com.Cinetime.payload.dto.request.ResetCodeRequest;
import com.Cinetime.payload.dto.request.ResetPasswordRequest;
import com.Cinetime.payload.dto.request.user.UserCreateRequest;
import com.Cinetime.payload.dto.request.user.UserRegisterRequest;
import com.Cinetime.payload.dto.request.user.UserRequestWithPasswordOnly;
import com.Cinetime.payload.dto.request.user.UserUpdateRequest;
import com.Cinetime.payload.dto.response.AuthResponse;
import com.Cinetime.payload.dto.response.BaseUserResponse;
import com.Cinetime.payload.dto.response.PasswordResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.passwordbusiness.PasswordResetService;
import com.Cinetime.service.UserService;
import com.Cinetime.service.authentication.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Management", description = "APIs for user registration, authentication, and management")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    //U02
    @Operation(
            summary = "Register User {U02}",
            description = "Register a new user in the system with default MEMBER role"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate user properties"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseMessage<BaseUserResponse> register(
            @Parameter(description = "User registration details", required = true)
            @RequestBody @Valid UserRegisterRequest userRegisterDTO) {
        return userService.register(userRegisterDTO);
    }

    //U01
    @Operation(
            summary = "Authenticate User {U01}",
            description = "Authenticate a user with phone number and password"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed - Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseMessage<AuthResponse> authenticateUser(
            @Parameter(description = "Login credentials", required = true)
            @RequestBody @Valid LoginRequest loginRequest) {
        return authenticationService.authenticateUser(loginRequest);
    }

    //U03
    @Operation(
            summary = "Generate Password Reset Code {U03}",
            description = "Generates a password reset code and sends it to the user's email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset code successfully generated and sent"),
            @ApiResponse(responseCode = "404", description = "Email not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/forgot-password")
    public ResponseMessage<PasswordResponse> generateResetPasswordCode(
            @Parameter(description = "Email for password reset", required = true)
            @RequestBody @Valid ForgotPasswordRequest request) {
        return passwordResetService.generateResetPasswordCode(request);
    }

    //U04
    @Operation(
            summary = "Reset Password {U04}",
            description = "Resets user password using the validated reset code"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully reset"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired reset code"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/reset-password")
    public ResponseMessage<PasswordResponse> resetPassword(
            @Parameter(description = "New password and reset code", required = true)
            @RequestBody @Valid ResetPasswordRequest resetPasswordDTO) {
        return passwordResetService.resetPassword(resetPasswordDTO);
    }

    @Operation(
            summary = "Validate Reset Password Code",
            description = "Validates the password reset code provided by the user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset code is valid"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired reset code"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/validate-reset-password-code")
    public ResponseMessage<PasswordResponse> validateResetPasswordCode(
            @Parameter(description = "Reset code to validate", required = true)
            @RequestBody @Valid ResetCodeRequest resetCodeDTO) {
        return passwordResetService.validateResetPasswordCode(resetCodeDTO);
    }

    //U05
    @Operation(
            summary = "Create User {U05}",
            description = "Creates a new user (Admin, Employee, Member roles can access)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate user properties"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires proper role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/users/auth")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseMessage<BaseUserResponse> createUser(
            @Parameter(description = "User details", required = true)
            @RequestBody @Valid UserCreateRequest userCreateDTO) {
        return userService.createUser(userCreateDTO);
    }

    //U06
    @Operation(
            summary = "Update User {U06}",
            description = "Updates an existing user (Admin, Employee, Member roles can access their own data)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate user properties"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires proper role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/users/auth")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MEMBER')")
    public ResponseMessage<BaseUserResponse> updateUser(
            @Parameter(description = "Updated user details", required = true)
            @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        return userService.updateUser(userUpdateRequest);
    }

    //U07
    @Operation(
            summary = "Delete User {U07}",
            description = "Deletes the authenticated user's account (requires MEMBER role)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Cannot delete - user has active tickets"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires MEMBER role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/users/auth")
    @PreAuthorize("hasAnyRole('MEMBER')")
    public ResponseMessage<BaseUserResponse> deleteUser(
            @Parameter(description = "Password confirmation", required = true)
            @RequestBody @Valid UserRequestWithPasswordOnly request) {
        return userService.deleteUser(request);
    }

    //U08
    @Operation(
            summary = "Search Users {U08}",
            description = "Search users with optional query parameter (Admin, Employee roles can access)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or EMPLOYEE role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/users/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseMessage<Page<BaseUserResponse>> getUserWithParam(
            @Parameter(description = "Search query (searches firstname, surname, email, phone)")
            @RequestParam(required = false) String q,
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String type) {
        return userService.getUserWithParam(q, page, size, sort, type);
    }

    @Operation(
            summary = "Debug Authentication",
            description = "Debug endpoint to check current authentication status",
            hidden = true
    )
    @GetMapping("/debug-auth")
    public String debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "User: " + auth.getName() +
                "\nAuthenticated: " + auth.isAuthenticated() +
                "\nAuthorities: " + auth.getAuthorities();
    }

    @Operation(
            summary = "Debug Token",
            description = "Debug endpoint to check Authorization header",
            hidden = true
    )
    @GetMapping("/debug-token")
    public String debugToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return "Token header: " + token;
    }


    //U09
    @Operation(
            summary = "Get User by ID {U09}",
            description = "Retrieve a specific user by their ID (Admin, Employee roles can access)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user details"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or EMPLOYEE role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/users/{userId}/admin")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseMessage<BaseUserResponse> getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    //U10
    @Operation(
            summary = "Update User by ID {U10}",
            description = "Update a specific user by their ID. Admin can update any user, while Employee can only update users with Member role. Built-in users cannot be updated.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated user"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors or built-in user update attempt"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or EMPLOYEE role or Employee attempting to update non-Member user"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - email or phone number already in use"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/users/{userId}/admin")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public ResponseMessage<BaseUserResponse> updateUserById(@PathVariable Long userId, @RequestBody UserUpdateRequest updateUserRequest) {
        return userService.updateUser(userId, updateUserRequest);
    }


    @Operation(
            summary = "Get Authenticated User Details",
            description = "Retrieves details of the currently authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved authenticated user details",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - No valid authentication token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/users/auth")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MEMBER')")
    public ResponseMessage<BaseUserResponse> getAuthenticatedUserDetails() {
        return userService.getAuthenticatedUserDetails();
    }
}