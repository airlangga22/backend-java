package com.finalproject.Tiket.Pesawat.dto.auth.request;

import com.finalproject.Tiket.Pesawat.dto.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotEmpty(message = "Email is required.")
    @NotBlank(message = "Email cannot be blank.")
    private String email;

    @NotEmpty(message = "Password is required.")
    @NotBlank(message = "Password cannot be blank.")
    @ValidPassword
    private String password;
}
