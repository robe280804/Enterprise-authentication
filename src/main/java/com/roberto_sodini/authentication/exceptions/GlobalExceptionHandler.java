package com.roberto_sodini.authentication.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> illegalStateException(IllegalStateException ex, WebRequest request){
        return generateResponse("ILLEGAL_STATE", HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }

    @ExceptionHandler(PasswordInvalid.class)
     public ResponseEntity<Object> passwordInvalid(PasswordInvalid ex, WebRequest request){
        return generateResponse("PASSWORD_NOT_EQUALS", HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(WrongAuthProvider.class)
    public ResponseEntity<Object> wrongAuthProvider(WrongAuthProvider ex, WebRequest request){
        return generateResponse("WRONG_AUTH_PROVIDER", HttpStatus.UNAUTHORIZED, ex, request);
    }

    @ExceptionHandler(EmailNotRegister.class)
    public ResponseEntity<Object> emailNotRegister(EmailNotRegister ex, WebRequest request){
        return generateResponse("EMAIL_NOT_REGISTERED", HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(EmailAlredyRegistered.class)
    public ResponseEntity<Object> emailAlredyRegistered(EmailAlredyRegistered ex, WebRequest request){
        return generateResponse("EMAIL_ALREDY_REGISTERED", HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(TokenNotFound.class)
    public ResponseEntity<Object> tokenNotFound(TokenNotFound ex, WebRequest request){
        return generateResponse("TOKEN_NOT_FOUND", HttpStatus.UNAUTHORIZED, ex, request);
    }

    @ExceptionHandler(TokenExpired.class)
    public ResponseEntity<Object> tokenExpired(TokenExpired ex, WebRequest request){
        return generateResponse("TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED, ex, request);
    }

    @ExceptionHandler(RateLimitExceed.class)
    public ResponseEntity<Object> rateLimitExceed(RateLimitExceed ex, WebRequest request){
        return generateResponse("RATELIMIT_EXCEED", HttpStatus.TOO_MANY_REQUESTS, ex, request);
    }


    private static ResponseEntity<Object> generateResponse(
            String error, HttpStatus status, Exception ex, WebRequest request
    ){
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status);
        response.put("error", error);
        response.put("message", ex.getMessage());
        response.put("path", extractPath(request));

        return new ResponseEntity<>(response, status);
    }

    private static String extractPath(WebRequest request){
        return request.getDescription(false).replace("uri=", "");
    }
}
