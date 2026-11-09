package com.streamcart.order.exception;

public class UserServiceUnavailableException extends RuntimeException {

    public UserServiceUnavailableException(Throwable cause) {
        super("User service is currently unavailable, please try again later", cause);
    }
}
