package com.healthqueue.utils;

import java.util.ArrayList;

import com.healthqueue.utils.ServerResponse.ValidationError;

public class ServerError extends RuntimeException {
    private final int status;
    private ArrayList<ValidationError> errors = new ArrayList<>(0);

    public ServerError(int statusCode, String message) {
        super(message);
        status = statusCode;
    }

    public ServerError(int statusCode, String message, ArrayList<ValidationError> errors) {
        this(statusCode, message);
        this.errors = errors;
    }

    public ServerError(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.status = statusCode;
    }

    public int getStatusCode() {
        return status;
    }

    public ArrayList<ValidationError> getValidationErrors() {
        return errors;
    }
}
