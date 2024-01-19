package com.finalproject.Tiket.Pesawat.payload.dto.auth;

import com.finalproject.Tiket.Pesawat.dto.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginDto {
    @NotEmpty(message = "Email is required.")
    @NotBlank(message = "Email cannot be blank.")
    @Email
    private String emailAddress;

    @NotEmpty(message = "Password is required.")
    @NotBlank(message = "Password cannot be blank.")
    @ValidPassword
    private String password;
}
