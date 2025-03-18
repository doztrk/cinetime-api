package com.Cinetime.payload.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Please provide a phone number")
    @Pattern(regexp = "^\\(\\d{3}\\)\\s\\d{3}-\\d{4}$", message = "Phone number must be in (XXX) XXX-XXXX format")
    private String phoneNumber;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 8, max = 60, message = "Your password should be at least 8 chars or maximum 60 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;

}
