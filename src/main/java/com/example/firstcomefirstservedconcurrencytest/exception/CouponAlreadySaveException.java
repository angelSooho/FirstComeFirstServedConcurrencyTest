package com.example.firstcomefirstservedconcurrencytest.exception;

public class CouponAlreadySaveException extends RuntimeException {
    public CouponAlreadySaveException(String message) {
        super(message);
    }
}
