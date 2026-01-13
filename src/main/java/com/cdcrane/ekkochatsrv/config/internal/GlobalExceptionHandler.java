package com.cdcrane.ekkochatsrv.config.internal;

import com.cdcrane.ekkochatsrv.auth.exceptions.BadAuthenticationException;
import com.cdcrane.ekkochatsrv.auth.exceptions.BadJwtException;
import com.cdcrane.ekkochatsrv.auth.exceptions.TokenNotFoundException;
import com.cdcrane.ekkochatsrv.config.dto.ExceptionErrorResponse;
import com.cdcrane.ekkochatsrv.config.dto.ValidationErrorResponse;
import com.cdcrane.ekkochatsrv.users.exceptions.IdentityTakenException;
import com.cdcrane.ekkochatsrv.users.exceptions.InvalidVerificationException;
import com.cdcrane.ekkochatsrv.users.exceptions.UserAlreadyVerifiedException;
import com.cdcrane.ekkochatsrv.users.exceptions.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ---------------------------------------------------
    // ------------- GENERIC EXCEPTIONS ------------------
    // ---------------------------------------------------

    // For uncaught exceptions, to not send stack traces in responses.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionErrorResponse> handleException(Exception ex){

        ExceptionErrorResponse res = ExceptionErrorResponse.builder()
                .message("An unexpected error occurred, please contact support.")
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(System.currentTimeMillis())
                .build();

        log.error("Uncaught exception: {}", ex.toString());

        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // For when a non-existent API path is called.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {

        ExceptionErrorResponse res = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(HttpStatus.NOT_FOUND.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);

    }

    /**
     * Handle validation errors from DTOs passed in the RequestBody of a controller method.
     * @param ex Exception thrown. This exception contains a collection of errors, each being any validation errors triggered.
     * @return Response explaining problem.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Map list of validation errors found, provided by exception, into a hashmap.
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Special error response for validation errors since it needs to be a JSON object for the client to use.
        ValidationErrorResponse error = ValidationErrorResponse.builder()
                .errors(errors)
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }


    // ---------------------------------------------------
    // --------------- AUTH EXCEPTIONS -------------------
    // ---------------------------------------------------

    @ExceptionHandler(BadJwtException.class)
    public ResponseEntity<ExceptionErrorResponse> handleBadJwt(BadJwtException ex) {

        ExceptionErrorResponse res = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(HttpStatus.UNAUTHORIZED.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);

    }

    // Refresh token not found in DB, therefore auth was revoked.
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ExceptionErrorResponse> handleTokenNotFound(TokenNotFoundException ex) {

        ExceptionErrorResponse res = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(HttpStatus.UNAUTHORIZED.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);

    }

    @ExceptionHandler(BadAuthenticationException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUserNotFound(BadAuthenticationException ex) {

        ExceptionErrorResponse res = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(HttpStatus.UNAUTHORIZED.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);

    }

    // ---------------------------------------------------
    // --------------- USER EXCEPTIONS -------------------
    // ---------------------------------------------------

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUserNotFound(UserNotFoundException ex) {

        ExceptionErrorResponse res = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(HttpStatus.NOT_FOUND.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(IdentityTakenException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUserNotFound(IdentityTakenException ex) {

        ExceptionErrorResponse res = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(HttpStatus.CONFLICT.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(res, HttpStatus.CONFLICT);

    }

    @ExceptionHandler(UserAlreadyVerifiedException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUserNotFound(UserAlreadyVerifiedException ex) {

        ExceptionErrorResponse res = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(InvalidVerificationException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUserNotFound(InvalidVerificationException ex) {

        ExceptionErrorResponse res = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);

    }

}
