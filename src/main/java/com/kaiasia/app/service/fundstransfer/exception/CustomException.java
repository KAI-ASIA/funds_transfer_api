package com.kaiasia.app.service.fundstransfer.exception;

public class CustomException extends RuntimeException {
    public CustomException(String message, Throwable cause) {
        super(cause);
    }

    public CustomException(Throwable cause) {
        super(cause);
    }
}
