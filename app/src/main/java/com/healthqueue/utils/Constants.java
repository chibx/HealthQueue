package com.healthqueue.utils;

public class Constants {

    public static final String APP_NAME = "HealthQueue";
    public static final String USER_CTX = "USER_CTX";
    public static final String ORG_CTX = "ORG_CTX";
    public static final String CUSTOMER_ACCESS_COOKIE = "cust_access_tk";
    public static final String CUSTOMER_REFRESH_COOKIE = "cust_refresh_tk";
    public static final String ORG_ACCESS_COOKIE = "org_access_tk";
    public static final String ORG_REFRESH_COOKIE = "org_refresh_tk";

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 40;

    public static final int MINUTE_1 = 60 * 1000;
    public static final int MINUTES_5 = 5 * MINUTE_1;
    public static final int MINUTES_15 = 15 * MINUTE_1;
    public static final int MINUTES_30 = 30 * MINUTE_1;
    public static final int HOURS_1 = 60 * MINUTE_1;
    public static final int DAY_1 = 24 * HOURS_1;
    public static final int DAYS_7 = 7 * DAY_1;
}
