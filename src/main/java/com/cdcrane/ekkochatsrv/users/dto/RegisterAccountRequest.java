package com.cdcrane.ekkochatsrv.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RegisterAccountRequest(@NotBlank @Max(150) String username,
                                     @Max(150) @NotBlank String firstName,
                                     @Max(150) @NotBlank String lastName,
                                     @Max(150) @NotBlank @Email String email,
                                     @Max(100) @Min(value = 8, message = "Password must be between 8 and 100 characters long.") String password) {
}
