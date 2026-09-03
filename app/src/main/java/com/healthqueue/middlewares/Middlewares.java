package com.healthqueue.middlewares;

import com.healthqueue.utils.Auth;
import com.healthqueue.utils.AuthContext;
import com.healthqueue.utils.AuthContext.DoctorCtx;
import com.healthqueue.utils.Constants;
import com.healthqueue.utils.AuthContext.UserCtx;
import com.healthqueue.utils.Utils.GoReturn;

import io.jsonwebtoken.Claims;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Middlewares {

    public static void InjectAuthCtx(Request req, Response res) {
        String customerCookie = req.cookie(Constants.PATIENT_ACCESS_COOKIE);
        String doctorCookie = req.cookie(Constants.DOCTOR_ACCESS_COOKIE);
        String orgCookie = req.cookie(Constants.ORG_ACCESS_COOKIE);

        if (orgCookie != null) {
            GoReturn<Claims> ret = Auth.verifyJWT(orgCookie, Auth.HS256_SECRET);
            if (ret.error == null && ret.value != null) {
                final UserCtx orgCtx = AuthContext.getUserCtxFromClaims(ret.value);
                if (orgCtx != null) {
                    req.attribute(Constants.ORG_CTX, orgCtx);
                }
            }
        }

        if (doctorCookie != null) {
            GoReturn<Claims> ret = Auth.verifyJWT(doctorCookie, Auth.HS256_SECRET);
            if (ret.error == null && ret.value != null) {
                final DoctorCtx doctorCtx = AuthContext.getDoctorCtxFromClaims(ret.value);
                if (doctorCtx != null) {
                    req.attribute(Constants.DOCTOR_CTX, doctorCtx);
                }
            }
        }

        if (customerCookie != null) {
            GoReturn<Claims> ret = Auth.verifyJWT(customerCookie, Auth.HS256_SECRET);
            if (ret.error == null && ret.value != null) {
                final UserCtx userCtx = AuthContext.getUserCtxFromClaims(ret.value);
                if (userCtx != null) {
                    req.attribute(Constants.USER_CTX, userCtx);
                }
            }
        }
    }

    public static void enableCORS(final String origin, final String methods, final String headers) {
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Allow-Methods", methods);
            response.header("Access-Control-Allow-Headers", headers);
            response.header("Access-Control-Allow-Credentials", "true");
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
