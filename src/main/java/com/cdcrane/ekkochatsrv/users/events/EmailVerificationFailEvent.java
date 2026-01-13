package com.cdcrane.ekkochatsrv.users.events;

public record EmailVerificationFailEvent(String email, String username, Integer newVerificationCode) {
}
