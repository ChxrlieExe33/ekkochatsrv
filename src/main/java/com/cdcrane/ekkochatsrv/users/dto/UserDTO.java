package com.cdcrane.ekkochatsrv.users.dto;

import java.util.Set;
import java.util.UUID;

public record UserDTO(UUID userId, String username,
                      String fName, String lName,
                      String email, Set<String> authorities) {
}
