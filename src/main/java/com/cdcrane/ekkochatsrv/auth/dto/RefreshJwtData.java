package com.cdcrane.ekkochatsrv.auth.dto;

import java.util.Date;
import java.util.UUID;

public record RefreshJwtData(String refreshJwt, UUID userId, Date expiration, UUID jti) {
}
