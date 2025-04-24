package com.Cinetime.payload.dto.request.user;

import com.Cinetime.enums.Gender;
import com.Cinetime.enums.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserRequest extends AbstractUserRequest {


    private boolean builtIn = false;
    private RoleName role = RoleName.MEMBER;

    //This class will have all the properties of its parent but will include constraint annotations
    @Override
    @NotBlank(message = "Please provide your name")
    public String getFirstname() {
        return super.getFirstname();
    }

    @Override
    @NotBlank(message = "Please provide your surname")
    public String getLastname() {
        return super.getLastname();
    }

    @Override
    @NotBlank(message = "Please provide your email")
    public String getEmail() {
        return super.getEmail();
    }

    @Override
    @NotBlank(message = "Please provide your phone number")
    public String getPhoneNumber() {
        return super.getPhoneNumber();
    }

    @Override
    @NotBlank(message = "Please provide your password")
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    @NotNull(message = "Please provide your gender")
    public Gender getGender() {
        return super.getGender();
    }

    @Override
    @NotNull(message = "Please provide your birth date")
    public LocalDate getDateOfBirth() {
        return super.getDateOfBirth();
    }

}
