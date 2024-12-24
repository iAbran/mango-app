package com.motete.mango.ecommerce_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {UserAlreadyExistsException.class})
    public ResponseEntity<Object> handleApiException(UserAlreadyExistsException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        ApiException apiException = new ApiException(
                "User already exists: " + e.getMessage(),
                httpStatus,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException, httpStatus);
    }

    @ExceptionHandler(value = {UserNotVerifiedException.class})
    public ResponseEntity<Object> handleApiException(UserNotVerifiedException e) {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        ApiException apiException = new ApiException(
                "User not verified: " + e.getMessage(),
                httpStatus,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException, httpStatus);
    }

    @ExceptionHandler(value = {EmailFailureException.class})
    public ResponseEntity<Object> handleApiException(EmailFailureException e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiException apiException = new ApiException(
                "Email delivery failed: " + e.getMessage(),
                httpStatus,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException, httpStatus);
    }

    @ExceptionHandler(value = {TokenNotFoundException.class})
    public ResponseEntity<Object> handleApiException(TokenNotFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ApiException apiException = new ApiException(
                "Token not found: " + e.getMessage(),
                httpStatus,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException, httpStatus);
    }

    @ExceptionHandler(value = {EmailAlreadyVerifiedException.class})
    public ResponseEntity<Object> handleApiException(EmailAlreadyVerifiedException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ApiException apiException = new ApiException(
                "Email already verified: " + e.getMessage(),
                httpStatus,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException, httpStatus);
    }

    @ExceptionHandler(value = {UserNotFoundException.class})
    public ResponseEntity<Object> handleApiException(UserNotFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ApiException apiException = new ApiException(
                "User not found: " + e.getMessage(),
                httpStatus,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException, httpStatus);
    }

}
