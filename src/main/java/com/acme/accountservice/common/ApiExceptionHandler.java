package com.acme.accountservice.common;

import java.time.Instant;
import java.util.Map;

import com.acme.accountservice.auth.UserExistsException;
import com.acme.accountservice.auth.PasswordPolicyException;
import com.acme.accountservice.auth.PasswordSameException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        return badRequest(request, null);
    }

    @ExceptionHandler({UserExistsException.class, DuplicateKeyException.class})
    public ResponseEntity<Map<String, Object>> handleUserExists(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return badRequest(request, "User exist!");
    }

    @ExceptionHandler({PasswordPolicyException.class, PasswordSameException.class})
    public ResponseEntity<Map<String, Object>> handlePasswordError(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return badRequest(request, exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> badRequest(
            HttpServletRequest request,
            String message
    ) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        if (message != null) {
            body.put("message", message);
        }
        body.put("path", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }
}
