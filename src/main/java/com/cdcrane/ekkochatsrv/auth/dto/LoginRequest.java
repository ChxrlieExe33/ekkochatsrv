package com.cdcrane.ekkochatsrv.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String usernameOrEmail,
                           @NotBlank String password) {
}
