package com.Cinetime.payload.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestInfoRequest {

    @NotNull(message = "Full name cannot be null")
    private String fullName;
    @NotNull(message = "Email cannot be null")
    private String email;
    @NotNull(message = "Phone number cannot be null")
    private String phoneNumber;

}
