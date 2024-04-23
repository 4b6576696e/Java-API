package com.API.JWT.advice;


public class InvalidRefreshToken extends RuntimeException{

    public InvalidRefreshToken(String message) {
        super(message);
    }
}
