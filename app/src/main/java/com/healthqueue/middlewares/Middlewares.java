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
        String customerCookie = req.cookie(Constants.PATIENT_ACCESS_COOKIE);
        String orgCookie = req.cookie(Constants.ORG_ACCESS_COOKIE);

        if (orgCookie != null) {
            GoReturn<Claims> ret = Auth.verifyJWT(orgCookie, null);
            if (ret.error != null) {
                try {
                    String errorBody = Utils.structuredResponse(Constants.STATUS_UNAUTHORIZED,
                            Constants.UNAUTHORIZED);
                    Spark.halt(Constants.STATUS_UNAUTHORIZED, errorBody);
                } catch (Exception e) {
                    Spark.halt(Constants.STATUS_INTERNAL_ERROR, Constants.INTERNAL_ERROR);
                }
            }
            final UserCtx userCtx = AuthContext.getUserCtxFromClaims(ret.value);
            if (userCtx != null) {
                req.attribute(Constants.ORG_CTX, userCtx);
            }
        }

        if (customerCookie != null) {
            GoReturn<Claims> ret = Auth.verifyJWT(customerCookie, null);
            if (ret.error != null) {
                try {
                    String errorBody = Utils.structuredResponse(Constants.STATUS_UNAUTHORIZED,
                            Constants.UNAUTHORIZED);
                    Spark.halt(Constants.STATUS_UNAUTHORIZED, errorBody);
                } catch (Exception e) {
                    Spark.halt(Constants.STATUS_INTERNAL_ERROR, Constants.INTERNAL_ERROR);
                }
            }
            final UserCtx orgCtx = AuthContext.getUserCtxFromClaims(ret.value);
            if (orgCtx != null) {
                req.attribute(Constants.USER_CTX, orgCtx);
            }

        }
    }

    public static void enableCORS(final String origin, final String methods, final String headers) {
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
        });

        Spark.options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });
    }
}
