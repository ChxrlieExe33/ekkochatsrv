package com.cdcrane.ekkochatsrv.auth.dto;

public record TokenPairResponse(AccessJwtData accessData, RefreshJwtData refreshData) {
}
