package com.cdcrane.ekkochatsrv.users.events;

import java.util.UUID;

public record AccountRegisteredEvent(UUID userId, String username, String email, Integer generatedVerificationCode) {
}
