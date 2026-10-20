package com.vavedin.euder.feature.user.exception;

public class EmailAlreadyExistsException extends IllegalArgumentException {
    public EmailAlreadyExistsException(String email) {
        super("Email " + email + " is already used");
    }
}
