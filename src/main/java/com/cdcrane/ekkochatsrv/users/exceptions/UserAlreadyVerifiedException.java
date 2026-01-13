package com.cdcrane.ekkochatsrv.users.exceptions;

public class UserAlreadyVerifiedException extends RuntimeException {
    public UserAlreadyVerifiedException(String message) {
        super(message);
    }
}
