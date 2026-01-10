package com.cdcrane.ekkochatsrv.auth.exceptions;

public class BadJwtException extends RuntimeException {

    public BadJwtException(String message) {
        super(message);
    }
}
