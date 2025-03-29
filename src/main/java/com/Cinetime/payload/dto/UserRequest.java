package com.Cinetime.payload.dto;


import com.Cinetime.enums.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public  class UserRequest {


    @NotBlank(message = "Please provide your name")
    @Size(min = 3, max = 20)
    private String firstname;

    @NotBlank(message = "Please provide your lastname")
    @Size(min = 3, max = 20)
    private String lastname;


    @NotBlank(message = "Please provide your email")
    @Email(message = "Please enter valid email")
    private String email;

    @NotBlank(message = "Please provide your phone number")
    @Pattern(regexp = "^\\(\\d{3}\\)\\s\\d{3}-\\d{4}$", message = "Phone number must be in (XXX) XXX-XXXX format")
    private String phoneNumber;

    @NotBlank(message = "Please provide your password")
    @Size(min = 8, max = 60, message = "Your password should be at least 8 chars or maximum 60 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;

    @NotNull(message = "Please provide your gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull
    @Past
    private LocalDate dateOfBirth;
    //UserRole will be set at Service level

}
