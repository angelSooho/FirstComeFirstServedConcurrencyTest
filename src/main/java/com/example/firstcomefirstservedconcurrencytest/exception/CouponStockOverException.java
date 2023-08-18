package com.example.firstcomefirstservedconcurrencytest.exception;

public class CouponStockOverException extends RuntimeException {
    public CouponStockOverException(String message) {
        super(message);
    }
}
