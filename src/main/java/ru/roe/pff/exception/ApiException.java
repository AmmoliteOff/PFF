package ru.roe.pff.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
