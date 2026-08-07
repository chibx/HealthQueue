package com.healthqueue.utils;

public class ServerResponse {
    public record ValidationError(String field, String message) {
    }

    public record StructuredResponse<T>(int status, String message, T data) {
    }

    public record StructuredError400(int status, String message, ValidationError[] errors) {
    }

    public record StructuredErrorAny(int status, String message) {
    }
}
