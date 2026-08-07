package com.healthqueue.middlewares;

import com.healthqueue.utils.Auth;
import com.healthqueue.utils.AuthContext;
import com.healthqueue.utils.Constants;
import com.healthqueue.utils.Utils;
import com.healthqueue.utils.AuthContext.UserCtx;
import com.healthqueue.utils.Utils.GoReturn;

import io.jsonwebtoken.Claims;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Middlewares {

    public static void InjectAuthCtx(Request req, Response res) {
        String customerCookie = req.cookie(Constants.CUSTOMER_ACCESS_COOKIE);
        String orgCookie = req.cookie(Constants.ORG_ACCESS_COOKIE);

        if (orgCookie != null) {
            GoReturn<Claims> ret = Auth.verifyJWT(orgCookie, null);
            if (ret.error() != null) {
                try {
                    String errorBody = Utils.structuredResponse(res, Constants.STATUS_UNAUTHORIZED,
                            Constants.UNAUTHORIZED);
                    Spark.halt(Constants.STATUS_UNAUTHORIZED, errorBody);
                } catch (Exception e) {
                    Spark.halt(Constants.STATUS_INTERNAL_ERROR, Constants.INTERNAL_ERROR);
                }
            }
            final UserCtx userCtx = AuthContext.getUserCtxFromClaims(ret.value());
            if (userCtx != null) {
                req.attribute(Constants.ORG_CTX, userCtx);
            }
        }

        if (customerCookie != null) {
            GoReturn<Claims> ret = Auth.verifyJWT(customerCookie, null);
            if (ret.error() != null) {
                try {
                    String errorBody = Utils.structuredResponse(res, Constants.STATUS_UNAUTHORIZED,
                            Constants.UNAUTHORIZED);
                    Spark.halt(Constants.STATUS_UNAUTHORIZED, errorBody);
                } catch (Exception e) {
                    Spark.halt(Constants.STATUS_INTERNAL_ERROR, Constants.INTERNAL_ERROR);
                }
            }
            final UserCtx userCtx = AuthContext.getUserCtxFromClaims(ret.value());
            if (userCtx != null) {
                req.attribute(Constants.USER_CTX, userCtx);
            }

        }
    }
}
