package com.cdcrane.ekkochatsrv.auth.dto;

import java.util.Date;

public record JwtData(String jwt, String username, Date expiration) {
}
