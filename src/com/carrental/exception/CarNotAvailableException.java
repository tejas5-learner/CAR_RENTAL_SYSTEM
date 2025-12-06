package com.carrental.exception;

/**
 * Custom exception to indicate car is not available.
 */
public class CarNotAvailableException extends Exception {
    public CarNotAvailableException(String msg) {
        super(msg);
    }
}

