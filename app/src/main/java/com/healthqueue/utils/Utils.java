// Utils.java
package com.healthqueue.utils;

import static com.healthqueue.utils.Constants.*;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.exception.InvalidUuidException;
import com.healthqueue.utils.ServerResponse.StructuredResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

public class Utils {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final Logger Logger = LoggerFactory.getLogger(Constants.APP_NAME);
    private static DSLContext dsl;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static SecureRandom SECURE_RANDOM;
    private final static Dotenv dotenv;

    // --- snowflake id: 41-bit timestamp (Discord epoch) + 10-bit node id + 12-bit
    // sequence ---
    private static final long DISCORD_EPOCH = 1420070400000L; // 2015-01-01T00:00:00.000Z
    private static final long NODE_ID = 1L; // bump per instance if you ever run more than one node
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    private final static Validator validator;

    static {
        dotenv = Dotenv.configure().load();
    }

    static {
        try {
            SECURE_RANDOM = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException _) {
            Logger.warn("Couldn't claim strong secure random instance");
            SECURE_RANDOM = new SecureRandom();
        }
    }

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(JDBC_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);

        // Pool settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        HikariDataSource dataSource = new HikariDataSource(config);

        dsl = DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static DSLContext getDSL() {
        return dsl;
    }

    public static class GoReturn<T> {
        public final T value;
        public final Throwable error;

        GoReturn(T value, Throwable error) {
            this.value = value;
            this.error = error;
        }
    }

    public static String cryptoRandomString(int length) {
        if (length <= 0)
            throw new IllegalArgumentException("length must be positive");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }
        return sb.toString();
    }

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
        String value = dotenv.get(key);
        if (value == null || value.isEmpty())
            value = defaultValue;
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Environment Variable `" + key + "` not set!");
        }
        return value;
    }

    public Connection getDBConn() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
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

    public static String structuredResponse(int status, String message) throws Exception {
        return structuredResponse(status, message, null);
    }

    public static <T> String structuredResponse(int status, String message, T data)
            throws Exception {
        // res.type("application/json");
        StructuredResponse<T> resp = new StructuredResponse<>(status, message, data);
        return MAPPER.writeValueAsString(resp);
    }

    public static <T> ArrayList<ServerResponse.ValidationError> toValidationErrors(Set<ConstraintViolation<T>> ex) {
        ArrayList<ServerResponse.ValidationError> errors = new ArrayList<>();
        for (ConstraintViolation<T> v : ex) {
            errors.add(new ServerResponse.ValidationError(v.getPropertyPath().toString(), v.getMessage()));
        }
        return errors;
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static boolean isValidUUID(String uuid) {
        try {
            UuidCreator.fromString(uuid);
            return true;
        } catch (InvalidUuidException e) {
            return false;
        }
    }

    /**
     * Wraps an async function so its CompletableFuture never fails outright —
     * it always resolves to GoReturn(value, null) or GoReturn(null, error).
     */
    public static <A, T> Function<A, CompletableFuture<GoReturn<@Nullable T>>> tryGoErrFuture(
            Function<A, CompletableFuture<T>> fn) {
        return arg -> fn.apply(arg).handle((result, error) -> error == null
                ? new GoReturn<>(result, null)
                : new GoReturn<>(null, unwrap(error)));
    }

    /**
     * Converts a GoReturn-shaped future back into a normal future that fails on
     * error.
     */
    public static <T> CompletableFuture<T> throwGoError(CompletableFuture<GoReturn<T>> future) {
        return future.thenApply(r -> {
            if (r.error != null) {
                throw (r.error instanceof RuntimeException re) ? re : new CompletionException(r.error);
            }
            return r.value;
        });
    }

    public static <T> GoReturn<@Nullable T> tryGo(java.util.concurrent.Callable<T> fn) {
        try {
            return new GoReturn<>(fn.call(), null);
        } catch (Exception e) {
            return new GoReturn<>(null, e);
        }
    }

    // CompletableFuture wraps thrown exceptions in CompletionException;
    // unwrap so callers see the real cause, like a plain `catch (error)` would in
    // JS.
    private static Throwable unwrap(Throwable t) {
        return (t instanceof CompletionException && t.getCause() != null) ? t.getCause() : t;
    }

    public static <T> T fromJSON(String body, Class<T> class1) throws JsonProcessingException {
        return MAPPER.readValue(body, class1);
    }

    public static <T> Set<ConstraintViolation<T>> validate(T obj) {
        return validator.validate(obj);
    }
}