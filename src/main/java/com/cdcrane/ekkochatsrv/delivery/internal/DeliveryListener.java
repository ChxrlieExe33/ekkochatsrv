package com.cdcrane.ekkochatsrv.delivery.internal;

import com.cdcrane.ekkochatsrv.users.events.AccountRegisteredEvent;
import com.cdcrane.ekkochatsrv.users.events.EmailVerificationFailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableAsync
@RequiredArgsConstructor
public class DeliveryListener {

    private final EmailUseCase emailUseCase;

    @ApplicationModuleListener
    public void sendVerificationEmailOnSignup(AccountRegisteredEvent e) {

        emailUseCase.sendVerificationEmail(e.email(), e.username(), e.generatedVerificationCode());

        log.info("Verification email sent to {}.", e.email());
    }

    @ApplicationModuleListener
    public void sendEmailAgainOnVerificationFail(EmailVerificationFailEvent e) {

        emailUseCase.sendVerificationEmail(e.email(), e.username(), e.newVerificationCode());

        log.info("New verification email sent to {}.", e.email());

    }
}
