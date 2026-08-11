package com.healthqueue.controllers;

import static com.healthqueue.utils.Constants.INTERNAL_ERROR;
import static com.healthqueue.utils.Constants.STATUS_INTERNAL_ERROR;
import static com.healthqueue.utils.ServerRequest.*;
import static com.healthqueue.db.Tables.USERS;

import com.healthqueue.db.tables.pojos.Users;
import com.healthqueue.utils.Auth;
import com.healthqueue.utils.AuthContext.UserCtx;
import com.healthqueue.utils.Constants;
import com.healthqueue.utils.ServerError;
import com.healthqueue.utils.ServerResponse;
import com.healthqueue.utils.Utils;
import com.healthqueue.utils.Utils.GoReturn;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jspecify.annotations.Nullable;

import spark.Request;
import spark.Response;
import javax.crypto.spec.SecretKeySpec;

public class AuthController {

    private static final SecretKey HS256_SECRET = new SecretKeySpec(Auth.HS256_SECRET_STRING.getBytes(), "");
    private static final SecretKey AES_SECRET = new SecretKeySpec(Auth.AES_SECRET_BYTE, Auth.CIPHER_ALGORITHM);

    public static Object RegisterPatient(Request req, Response res) throws Exception {
        try {
            final DSLContext dsl = Utils.getDSL();
            PatientSignupRequest body = Utils.fromJSON(req.body(), PatientSignupRequest.class);
            Utils.validator.validate(body);

            GoReturn<String> hashResult = Auth.hashText(body.password());
            if (hashResult.error != null) {
                Utils.Logger.error("Error hashing password: {}", hashResult.error.getMessage());
                throw new ServerError(
                        Constants.STATUS_INTERNAL_ERROR,
                        Utils.structuredResponse(
                                Constants.STATUS_INTERNAL_ERROR,
                                Constants.INTERNAL_ERROR));
            }
            String passwordHash = hashResult.value;

            UUID newUserId = UuidCreator.getTimeOrderedEpoch();

            @Nullable
            Users newUser = dsl.insertInto(USERS)
                    .set(USERS.ID, newUserId)
                    .set(USERS.FIRST_NAME, body.firstName())
                    .set(USERS.LAST_NAME, body.lastName())
                    .set(USERS.EMAIL, body.email())
                    .set(USERS.PASSWORD_HASH, passwordHash)
                    .set(USERS.LATITUDE, BigDecimal.valueOf(body.latitude()))
                    .set(USERS.LONGITUDE, BigDecimal.valueOf(body.longitude()))
                    .returningResult(USERS.ID, USERS.FIRST_NAME, USERS.LAST_NAME, USERS.EMAIL)
                    .fetchOne()
                    .into(Users.class);

            if (newUser == null) {
                throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
            }

            Map<String, Object> claims = Map.of(
                    "uuid", newUser.getId().toString(),
                    "type", "patient");

            GoReturn<String> accessJwtResult = Auth.signJWT(
                    claims,
                    HS256_SECRET,
                    Constants.MINUTES_30);
            if (accessJwtResult.error != null) {
                Utils.Logger.error("Error signing access JWT: {}",
                        accessJwtResult.error.getMessage());
                throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
            }

            GoReturn<String> refreshJwtResult = Auth.signJWT(
                    claims,
                    HS256_SECRET,
                    Constants.DAYS_7);
            if (refreshJwtResult.error != null) {
                Utils.Logger.error("Error signing refresh JWT: {}",
                        refreshJwtResult.error.getMessage());
                throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);

            }

            GoReturn<String> encryptedRefreshCookie = Auth.encrypt(
                    refreshJwtResult.value,
                    AES_SECRET);
            if (encryptedRefreshCookie.error != null) {
                Utils.Logger.error(
                        "Error encrypting refresh cookie: {}",
                        encryptedRefreshCookie.error.getMessage());
                throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
            }

            res.cookie(
                    Constants.CUSTOMER_REFRESH_COOKIE,
                    encryptedRefreshCookie.value,
                    Constants.DAYS_7 / 1000,
                    true,
                    false);

            return Utils.structuredResponse(
                    Constants.STATUS_OK,
                    Constants.RESPONSE_OK,
                    new ServerResponse.AuthResponse(
                            accessJwtResult.value,
                            newUser.getId().toString(),
                            "patient"));
        } catch (ConstraintViolationException cve) {
            res.status(Constants.STATUS_BAD_REQUEST);
            return Utils.MAPPER.writeValueAsString(new ServerResponse.StructuredError400(
                    Constants.STATUS_BAD_REQUEST,
                    Constants.BAD_REQUEST,
                    Utils.toValidationErrors(cve)));
        } catch (DataAccessException dae) {
            if (dae.getMessage() != null && dae.getMessage()
                    .contains("duplicate key value violates unique constraint \"users_email_key\"")) {
                throw new ServerError(Constants.STATUS_BAD_REQUEST,
                        Utils.structuredResponse(Constants.STATUS_BAD_REQUEST,
                                "Account with this email already exists"));

            }
            throw dae;
        }
    }

    public static Object LoginPatient(Request req, Response res) throws Exception {
        try {
            PatientLoginRequest body = Utils.fromJSON(req.body(), PatientLoginRequest.class);
            Utils.validator.validate(body);

            final DSLContext dsl = Utils.getDSL();
            @Nullable
            Users user = dsl.selectFrom(USERS)
                    .where(USERS.EMAIL.eq(body.email()))
                    .fetchOneInto(Users.class);

            if (user == null) {
                throw new ServerError(Constants.STATUS_UNAUTHORIZED, Utils.structuredResponse(
                        Constants.STATUS_UNAUTHORIZED, Constants.UNAUTHORIZED));
            }

            GoReturn<Boolean> verifyResult = Auth.verifyHash(user.getPasswordHash(), body.password());
            if (verifyResult.error != null || !verifyResult.value) {
                throw new ServerError(Constants.STATUS_UNAUTHORIZED, Utils.structuredResponse(
                        Constants.STATUS_UNAUTHORIZED, Constants.UNAUTHORIZED));
            }

            Map<String, Object> claims = Map.of(
                    "uuid", user.getId().toString(),
                    "type", "patient");

            GoReturn<String> accessJwtResult = Auth.signJWT(
                    claims,
                    HS256_SECRET,
                    Constants.MINUTES_30);
            if (accessJwtResult.error != null) {
                Utils.Logger.error("Error signing access JWT: {}",
                        accessJwtResult.error.getMessage());
                throw new ServerError(
                        Constants.STATUS_INTERNAL_ERROR,
                        Utils.structuredResponse(
                                Constants.STATUS_INTERNAL_ERROR,
                                Constants.INTERNAL_ERROR));
            }

            GoReturn<String> refreshJwtResult = Auth.signJWT(
                    claims,
                    HS256_SECRET,
                    Constants.DAYS_7);
            if (refreshJwtResult.error != null) {
                Utils.Logger.error("Error signing refresh JWT: {}",
                        refreshJwtResult.error.getMessage());
                throw new ServerError(
                        Constants.STATUS_INTERNAL_ERROR,
                        Utils.structuredResponse(
                                Constants.STATUS_INTERNAL_ERROR,
                                Constants.INTERNAL_ERROR));
            }

            GoReturn<String> encryptedRefreshCookie = Auth.encrypt(
                    refreshJwtResult.value,
                    AES_SECRET);
            if (encryptedRefreshCookie.error != null) {
                Utils.Logger.error(
                        "Error encrypting refresh cookie: {}",
                        encryptedRefreshCookie.error.getMessage());
                throw new ServerError(
                        Constants.STATUS_INTERNAL_ERROR,
                        Utils.structuredResponse(
                                Constants.STATUS_INTERNAL_ERROR,
                                Constants.INTERNAL_ERROR));
            }

            res.cookie(
                    Constants.CUSTOMER_REFRESH_COOKIE,
                    encryptedRefreshCookie.value,
                    Constants.DAYS_7 / 1000,
                    true,
                    false);

            return Utils.structuredResponse(
                    Constants.STATUS_OK,
                    Constants.RESPONSE_OK,
                    new ServerResponse.AuthResponse(
                            accessJwtResult.value,
                            user.getId().toString(),
                            "patient"));
        } catch (ConstraintViolationException cve) {
            res.status(Constants.STATUS_BAD_REQUEST);
            return Utils.MAPPER.writeValueAsString(new ServerResponse.StructuredError400(
                    Constants.STATUS_BAD_REQUEST,
                    Constants.BAD_REQUEST,
                    Utils.toValidationErrors(cve)));
        } catch (Exception e) {
            Utils.Logger.error("LoginPatient error: {}", e.getMessage());
            throw new ServerError(Constants.STATUS_INTERNAL_ERROR, Utils
                    .structuredResponse(Constants.STATUS_INTERNAL_ERROR, Constants.INTERNAL_ERROR));
        }
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
