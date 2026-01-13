package com.cdcrane.ekkochatsrv.auth.exceptions;

public class BadAuthenticationException extends RuntimeException {
    public BadAuthenticationException(String message) {
        super(message);
    }
}
