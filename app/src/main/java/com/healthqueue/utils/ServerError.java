package com.healthqueue.utils;

public class ServerError extends Exception {
    public final int status;

    public ServerError(int statusCode, String message) {
        super(message);
        status = statusCode;
    }
}
