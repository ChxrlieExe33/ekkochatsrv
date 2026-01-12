package com.cdcrane.ekkochatsrv.delivery.internal;

public interface EmailUseCase {

    void sendVerificationEmail(String email, String firstName, Integer verificationCode);
}
