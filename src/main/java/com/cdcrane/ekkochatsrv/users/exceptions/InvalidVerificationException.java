package com.cdcrane.ekkochatsrv.users.exceptions;

public class InvalidVerificationException extends RuntimeException {
    public InvalidVerificationException(String message) {
        super(message);
    }
}
