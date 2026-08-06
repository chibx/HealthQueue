package com.healthqueue.middlewares;

import com.healthqueue.utils.Constants;
import spark.Request;
import spark.Response;

public class Middlewares {

    public static Object InjectAuthCtx(Request req, Response res) {
        String customerCookie = req.cookie(Constants.CUSTOMER_ACCESS_COOKIE);
        String orgCookie = req.cookie(Constants.ORG_ACCESS_COOKIE);

        if (orgCookie != null) {

        }

        return 2;
    }
}
