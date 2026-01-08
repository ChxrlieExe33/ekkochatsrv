package com.cdcrane.ekkochatsrv.auth.dto;

import java.util.Date;

public record AccessJwtData(String jwt, String username, Date expiration) {
}
