package com.API.JWT.advice;

import com.API.JWT.dto.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private Map<String, String> handleInvalidArgument(MethodArgumentNotValidException ex) {
            Map<String, String> errorMap = new HashMap<>();

            ex.getBindingResult()
                    .getFieldErrors()
                    .forEach(error -> {
                        errorMap.put(error.getField(), error.getDefaultMessage());
                    });
            
            return errorMap;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(InvalidRefreshToken.class)
     private Map<String, String> handleInvalidRefreshToken(InvalidRefreshToken ex) {
        Map<String, String> errorMap = new HashMap<>();

        errorMap.put("error", ex.getMessage());

        return errorMap;
    }
}
