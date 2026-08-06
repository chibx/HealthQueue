// Utils.java
package com.healthqueue.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static final Logger logger = LoggerFactory.getLogger(Constants.APP_NAME);

    // --- snowflake id: 41-bit timestamp (Discord epoch) + 10-bit node id + 12-bit
    // sequence ---
    private static final long DISCORD_EPOCH = 1420070400000L; // 2015-01-01T00:00:00.000Z
    private static final long NODE_ID = 1L; // bump per instance if you ever run more than one node
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    public static synchronized long nextSnowflakeId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & 0xFFF; // wraps at 4096/ms
            if (sequence == 0) {
                while (timestamp <= lastTimestamp)
                    timestamp = System.currentTimeMillis();
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        return ((timestamp - DISCORD_EPOCH) << 22) | ((NODE_ID & 0x3FF) << 12) | sequence;
    }

    public static String getEnv(String key) {
        return getEnv(key, null);
    }

    public static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty())
            value = defaultValue;
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Environment Variable `" + key + "` not set!");
        }
        return value;
    }

    public static Instant addToDate(long ms) {
        return addToDate(ms, null);
    }

    public static Instant addToDate(long ms, Instant date) {
        return (date != null ? date : Instant.now()).plusMillis(ms);
    }

    // Instant#toString() already returns ISO-8601 — same format .toISOString()
    // gives you
    public static String dateToString(Instant date) {
        return date != null ? date.toString() : null;
    }

    public static <T> ServerResponse.StructuredResponse<T> structuredResponse(int status, String message) {
        return new ServerResponse.StructuredResponse<>(status, message, (T) null);
    }

    public static <T> ServerResponse.StructuredResponse<T> structuredResponse(int status, String message, T data) {
        return new ServerResponse.StructuredResponse<>(status, message, data);
    }

    public static List<ServerResponse.ValidationError> toValidationErrors(ConstraintViolationException ex) {
        List<ServerResponse.ValidationError> errors = new ArrayList<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            errors.add(new ServerResponse.ValidationError(v.getPropertyPath().toString(), v.getMessage()));
        }
        return errors;
    }
}