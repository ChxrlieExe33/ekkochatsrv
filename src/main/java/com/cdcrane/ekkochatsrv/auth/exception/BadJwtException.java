package com.cdcrane.ekkochatsrv.auth.exception;

public class BadJwtException extends RuntimeException {

    public BadJwtException(String message) {
        super(message);
    }
}
