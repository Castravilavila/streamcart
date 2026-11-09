package com.streamcart.order.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String email) {
        super("Customer not found: " + email);
    }
}
