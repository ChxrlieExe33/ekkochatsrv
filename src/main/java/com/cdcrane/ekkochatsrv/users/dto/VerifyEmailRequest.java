package com.cdcrane.ekkochatsrv.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyEmailRequest(@NotBlank @Email String email,
                                 @NotNull Integer code) {
}
