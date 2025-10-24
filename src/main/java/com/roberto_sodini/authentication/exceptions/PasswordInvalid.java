package com.roberto_sodini.authentication.exceptions;

public class PasswordInvalid extends RuntimeException {
    public PasswordInvalid(String message) {
        super(message);
    }
}
