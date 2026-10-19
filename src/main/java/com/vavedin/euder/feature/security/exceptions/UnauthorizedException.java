package com.vavedin.euder.feature.security.exceptions;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Invalid credentials");
    }
}
