package com.motete.mango.ecommerce_backend.exception;

public class EmailFailureException extends Exception{

    public EmailFailureException(String message) {
        super(message);
    }

    public EmailFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
