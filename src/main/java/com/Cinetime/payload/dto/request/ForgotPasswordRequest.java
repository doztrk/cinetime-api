package com.Cinetime.payload.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ForgotPasswordRequest {


    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email format is not valid")
    private String email;

}
