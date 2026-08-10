package com.healthqueue.controllers;

import static com.healthqueue.utils.ServerRequest.*;

import com.healthqueue.utils.Constants;
import com.healthqueue.utils.ServerResponse;

import com.healthqueue.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.healthqueue.utils.AuthContext.UserCtx;
import spark.Request;
import spark.Response;

public class AuthController {
    public static Object RegisterPatient(Request req, Response res) throws JsonProcessingException {
        PatientSignupRequest body = Utils.fromJSON(req.body(), PatientSignupRequest.class);

        System.out.println("Email: " + body.email());

        return 0;
    }

    public static Object LoginPatient(Request req, Response res) {

        return 2;
    }

    public static Object RegisterOrganization(Request req, Response res) {

        return 2;
    }

    public static Object LoginOrganization(Request req, Response res) {

        return 2;
    }

    public static Object Refresh(Request req, Response res) {

        return 2;
    }

    public static Object Logout(Request req, Response res) {

        return 2;
    }

    public static Object Whoami(Request req, Response res) throws JsonProcessingException {
        UserCtx tmpUser = req.attribute(Constants.USER_CTX);
        UserCtx tmpOrg = req.attribute(Constants.ORG_CTX);

        String user = tmpUser != null ? tmpUser.uuid() : null;
        String org = tmpOrg != null ? tmpOrg.uuid() : null;

        return Utils.MAPPER.writeValueAsString(new ServerResponse.WhoamiResponse(user, org));
    }
}