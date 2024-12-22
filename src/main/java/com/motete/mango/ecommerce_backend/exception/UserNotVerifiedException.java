package com.motete.mango.ecommerce_backend.exception;

public class UserNotVerifiedException extends Exception{

    private Boolean newEmailSent;

    public UserNotVerifiedException(Boolean newEmailSent) {
        this.newEmailSent = newEmailSent;
    }

    public Boolean isNewEmailSent() {
        return newEmailSent;
    }
}
