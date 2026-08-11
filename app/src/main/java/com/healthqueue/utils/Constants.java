package com.healthqueue.utils;

import org.eclipse.jetty.http.HttpStatus;

public class Constants {

    public static final String APP_NAME = "HealthQueue";
    public static final String USER_CTX = "USER_CTX";
    public static final String ORG_CTX = "ORG_CTX";
    public static final String CUSTOMER_ACCESS_COOKIE = "cust_access_tk";
    public static final String CUSTOMER_REFRESH_COOKIE = "cust_refresh_tk";
    public static final String ORG_ACCESS_COOKIE = "org_access_tk";
    public static final String ORG_REFRESH_COOKIE = "org_refresh_tk";

    public static final int STATUS_OK = 200;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_INTERNAL_ERROR = 500;

    public static final String RESPONSE_OK = "Success";
    public static final String NOT_FOUND = HttpStatus.getMessage(STATUS_NOT_FOUND);
    public static final String UNAUTHORIZED = HttpStatus.getMessage(STATUS_UNAUTHORIZED);
    public static final String BAD_REQUEST = HttpStatus.getMessage(STATUS_BAD_REQUEST);
    public static final String FORBIDDEN_ERROR = HttpStatus.getMessage(STATUS_FORBIDDEN);
    public static final String INTERNAL_ERROR = HttpStatus.getMessage(STATUS_INTERNAL_ERROR);

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 40;
    public static final int REFRESH_TOKEN_LENGTH = 32;

    public static final int MINUTE_1 = 60 * 1000;
    public static final int MINUTES_5 = 5 * MINUTE_1;
    public static final int MINUTES_15 = 15 * MINUTE_1;
    public static final int MINUTES_30 = 30 * MINUTE_1;
    public static final int HOURS_1 = 60 * MINUTE_1;
    public static final int DAY_1 = 24 * HOURS_1;
    public static final int DAYS_7 = 7 * DAY_1;

    public static final String DB_PASSWORD = Utils.getEnv("DB_PASS");
    public static final String DB_USER = Utils.getEnv("DB_USER");
    public static final String JDBC_URL = Utils.getEnv("JDBC_URL");
}
