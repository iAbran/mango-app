package com.motete.mango.ecommerce_backend.api.model;

public class LoginResponse {

    private String jwt;

    public String getJwt() { return jwt; }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
