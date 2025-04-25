package com.Cinetime.payload.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ResetCodeRequest {


    @NotEmpty(message = "Reset code cannot be empty")
    private String resetCode;


}
