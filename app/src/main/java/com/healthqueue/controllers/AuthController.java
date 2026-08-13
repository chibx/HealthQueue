package com.healthqueue.controllers;

import static com.healthqueue.utils.Constants.*;
import static com.healthqueue.db.Tables.ORGANIZATIONS;
import static com.healthqueue.db.Tables.ORGANIZATION_REQUESTS;
import static com.healthqueue.db.Tables.ORG_REFRESH_TOKENS;
import static com.healthqueue.db.Tables.USERS;
import static com.healthqueue.db.Tables.USER_REFRESH_TOKENS;

import com.healthqueue.db.tables.pojos.Organizations;
import com.healthqueue.db.tables.pojos.Users;
import com.healthqueue.utils.Auth;
import com.healthqueue.utils.AuthContext.UserCtx;
import com.healthqueue.utils.ServerError;
import com.healthqueue.utils.ServerRequest.OrganizationLoginRequest;
import com.healthqueue.utils.ServerRequest.OrganizationSignupRequest;
import com.healthqueue.utils.ServerRequest.PatientLoginRequest;
import com.healthqueue.utils.ServerRequest.PatientSignupRequest;
import com.healthqueue.utils.ServerResponse;
import com.healthqueue.utils.Utils;
import com.healthqueue.utils.Utils.GoReturn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.exception.DataAccessException;
import org.jspecify.annotations.Nullable;

import spark.Request;
import spark.Response;
import javax.crypto.spec.SecretKeySpec;

public class AuthController {

    private static final SecretKey HS256_SECRET = new SecretKeySpec(Auth.HS256_SECRET_STRING.getBytes(),
            Auth.HMAC256_ALGORITHM);
    // private static final SecretKey AES_SECRET = new
    // SecretKeySpec(Auth.AES_SECRET_BYTE, Auth.CIPHER_ALGORITHM);

    public static Object RegisterPatient(Request request, Response response) throws Exception {
        final DSLContext dsl = Utils.getDSL();

        String ipAddr = request.ip();
        PatientSignupRequest body = Utils.fromJSON(request.body(), PatientSignupRequest.class);
        Set<ConstraintViolation<PatientSignupRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        GoReturn<String> hashResult = Auth.hashText(body.password());
        if (hashResult.error != null) {
            Utils.Logger.error("Error hashing password: {}", hashResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }
        String passwordHash = hashResult.value;

        UUID newUserId = UuidCreator.getTimeOrderedEpoch();

        @Nullable
        Users newUser;

        try {
            newUser = dsl.insertInto(USERS)
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
        } catch (DataAccessException dae) {
            if (dae.getMessage() != null && dae.getMessage()
                    .contains("duplicate key value violates unique constraint \"users_email_key\"")) {
                throw new ServerError(STATUS_BAD_REQUEST, "Account with this email already exists");

            }
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR, dae);
        }

        if (newUser == null) {
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        Map<String, Object> claims = Map.of(
                "uuid", newUser.getId().toString(),
                "type", "patient");

        GoReturn<String> accessJwtResult = Auth.signJWT(
                claims,
                HS256_SECRET,
                MINUTES_30);
        if (accessJwtResult.error != null) {
            Utils.Logger.error("Error signing access JWT: {}", accessJwtResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        String refreshToken = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);

        dsl.insertInto(USER_REFRESH_TOKENS, USER_REFRESH_TOKENS.USER_ID, USER_REFRESH_TOKENS.TOKEN,
                USER_REFRESH_TOKENS.IP, USER_REFRESH_TOKENS.EXPIRES_AT)
                .values(newUser.getId(), refreshToken, ipAddr,
                        OffsetDateTime.ofInstant(Utils.addToDate(DAYS_7),
                                ZoneId.systemDefault()))
                .execute();

        response.cookie(
                ORG_ACCESS_COOKIE,
                accessJwtResult.value,
                MINUTES_30 / 1000,
                true,
                false);
        response.cookie(
                PATIENT_REFRESH_COOKIE,
                refreshToken,
                DAYS_7 / 1000,
                true,
                false);

        return null;
    }

    public static Object LoginPatient(Request request, Response response) throws Exception {
        final DSLContext dsl = Utils.getDSL();

        String ipAddr = request.ip();
        PatientLoginRequest body = Utils.fromJSON(request.body(), PatientLoginRequest.class);
        Set<ConstraintViolation<PatientLoginRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        @Nullable
        Users user = dsl.select(USERS.ID, USERS.PASSWORD_HASH)
                .from(USERS)
                .where(USERS.EMAIL.eq(body.email()))
                .fetchOneInto(Users.class);

        if (user == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        GoReturn<Boolean> verifyResult = Auth.verifyHash(user.getPasswordHash(), body.password());
        if (verifyResult.error != null || !verifyResult.value) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        Map<String, Object> claims = Map.of(
                "uuid", user.getId().toString(),
                "type", "patient");

        GoReturn<String> accessJwtResult = Auth.signJWT(
                claims,
                HS256_SECRET,
                MINUTES_30);
        if (accessJwtResult.error != null) {
            Utils.Logger.error("Error signing access JWT: {}",
                    accessJwtResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        String refreshToken = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);

        dsl.insertInto(USER_REFRESH_TOKENS, USER_REFRESH_TOKENS.USER_ID, USER_REFRESH_TOKENS.TOKEN,
                USER_REFRESH_TOKENS.IP, USER_REFRESH_TOKENS.EXPIRES_AT)
                .values(user.getId(), refreshToken, ipAddr,
                        OffsetDateTime.ofInstant(Utils.addToDate(DAYS_7),
                                ZoneId.systemDefault()))
                .execute();

        response.cookie(PATIENT_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000,
                true,
                false);
        response.cookie(
                PATIENT_REFRESH_COOKIE,
                refreshToken,
                DAYS_7 / 1000,
                true,
                false);

        return null;
    }

    public static Object RegisterOrganization(Request request, Response response) throws Exception {
        final DSLContext dsl = Utils.getDSL();

        String ipAddr = request.ip();
        OrganizationSignupRequest body = Utils.fromJSON(request.body(), OrganizationSignupRequest.class);
        Set<ConstraintViolation<OrganizationSignupRequest>> violations = Utils.validate(body);

        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        Record1<String> orgRecord = dsl.select(ORGANIZATION_REQUESTS.REGISTRATION_CODE)
                .from(ORGANIZATION_REQUESTS)
                .where(ORGANIZATION_REQUESTS.EMAIL.eq(body.email())).fetchOne();
        String orgCode = orgRecord.get(ORGANIZATION_REQUESTS.REGISTRATION_CODE);
        if (orgCode == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, "Invalid Registration Code");
        }

        GoReturn<String> hashResult = Auth.hashText(body.password());
        if (hashResult.error != null) {
            Utils.Logger.error("Error hashing password: {}", hashResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }
        String passwordHash = hashResult.value;

        UUID newOrgId = UuidCreator.getTimeOrderedEpoch();

        @Nullable
        Organizations newOrg;

        try {
            newOrg = dsl.insertInto(ORGANIZATIONS)
                    .set(ORGANIZATIONS.ID, newOrgId)
                    .set(ORGANIZATIONS.NAME, body.name())
                    .set(ORGANIZATIONS.EMAIL, body.email())
                    .set(ORGANIZATIONS.PASSWORD_HASH, passwordHash)
                    .returningResult(ORGANIZATIONS.ID, ORGANIZATIONS.NAME, ORGANIZATIONS.EMAIL)
                    .fetchOneInto(Organizations.class);
        } catch (DataAccessException dae) {
            if (dae.getMessage() != null && dae.getMessage()
                    .contains("duplicate key value violates unique constraint \"organizations_email_key\"")) {
                throw new ServerError(STATUS_BAD_REQUEST, "Account with this email already exists");

            }
            if (dae.getMessage() != null && dae.getMessage()
                    .contains(
                            "duplicate key value violates unique constraint \"organization_requests_registration_code_key\"")) {
                throw new ServerError(STATUS_BAD_REQUEST,
                        "Organization with this registration code already exists");
            }
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR, dae);
        }

        if (newOrg == null) {
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        Map<String, Object> claims = Map.of(
                "uuid", newOrg.getId().toString(),
                "type", "organization");

        GoReturn<String> accessJwtResult = Auth.signJWT(
                claims,
                HS256_SECRET,
                MINUTES_30);
        if (accessJwtResult.error != null) {
            Utils.Logger.error("Error signing access JWT: {}",
                    accessJwtResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        String refreshToken = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);

        dsl.insertInto(ORG_REFRESH_TOKENS, ORG_REFRESH_TOKENS.ORGANIZATION_ID, ORG_REFRESH_TOKENS.TOKEN,
                ORG_REFRESH_TOKENS.IP, ORG_REFRESH_TOKENS.EXPIRES_AT)
                .values(newOrg.getId(), refreshToken, ipAddr,
                        OffsetDateTime.ofInstant(Utils.addToDate(DAYS_7),
                                ZoneId.systemDefault()))
                .execute();

        response.cookie(ORG_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000,
                true,
                false);

        response.cookie(
                ORG_REFRESH_COOKIE,
                refreshToken,
                DAYS_7 / 1000,
                true,
                false);

        return null;
    }

    public static Object LoginOrganization(Request request, Response response) throws Exception {
        final DSLContext dsl = Utils.getDSL();

        String ipAddr = request.ip();
        OrganizationLoginRequest body = Utils.fromJSON(request.body(), OrganizationLoginRequest.class);
        Set<ConstraintViolation<OrganizationLoginRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        @Nullable
        Organizations org = dsl.select(ORGANIZATIONS.ID, ORGANIZATIONS.PASSWORD_HASH)
                .from(ORGANIZATIONS)
                .where(ORGANIZATIONS.EMAIL.eq(body.email()))
                .fetchOneInto(Organizations.class);

        if (org == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        GoReturn<Boolean> verifyResult = Auth.verifyHash(org.getPasswordHash(), body.password());
        if (verifyResult.error != null || !verifyResult.value) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        Map<String, Object> claims = Map.of(
                "uuid", org.getId().toString(),
                "type", "organization");

        GoReturn<String> accessJwtResult = Auth.signJWT(
                claims,
                HS256_SECRET,
                MINUTES_30);
        if (accessJwtResult.error != null) {
            Utils.Logger.error("Error signing access JWT: {}",
                    accessJwtResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        String refreshToken = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);

        dsl.insertInto(ORG_REFRESH_TOKENS, ORG_REFRESH_TOKENS.ORGANIZATION_ID, ORG_REFRESH_TOKENS.TOKEN,
                ORG_REFRESH_TOKENS.IP, ORG_REFRESH_TOKENS.EXPIRES_AT)
                .values(org.getId(), refreshToken, ipAddr,
                        OffsetDateTime.ofInstant(Utils.addToDate(DAYS_7),
                                ZoneId.systemDefault()))
                .execute();

        response.cookie(ORG_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000,
                true,
                false);

        response.cookie(
                ORG_REFRESH_COOKIE,
                refreshToken,
                DAYS_7 / 1000,
                true,
                false);

        return Utils.structuredResponse(
                STATUS_OK,
                RESPONSE_OK,
                new ServerResponse.AuthResponse(
                        accessJwtResult.value,
                        org.getId().toString(),
                        "organization"));
    }

    public static Object RefreshPatient(Request request, Response response) throws Exception {
        DSLContext dsl = Utils.getDSL();

        String ipAddr = request.ip();
        @Nullable
        UserCtx userCtx = request.attribute(USER_CTX);
        if (userCtx == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        boolean userExists = dsl.fetchExists(dsl.selectOne().from(USERS).where(USERS.ID.eq(userCtx.uuid())));
        if (!userExists) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        @Nullable
        String refreshToken = request.cookie(PATIENT_REFRESH_COOKIE);
        if (refreshToken == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }
        String newRefreshToken = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);
        dsl.transaction((ctx) -> {
            DSLContext innerDSL = ctx.dsl();

            innerDSL.update(USER_REFRESH_TOKENS).set(USER_REFRESH_TOKENS.USED, true)
                    .where(USER_REFRESH_TOKENS.USER_ID.eq(userCtx.uuid()),
                            USER_REFRESH_TOKENS.TOKEN.eq(refreshToken))
                    .execute();
            innerDSL.insertInto(USER_REFRESH_TOKENS, USER_REFRESH_TOKENS.USER_ID, USER_REFRESH_TOKENS.TOKEN,
                    USER_REFRESH_TOKENS.IP, USER_REFRESH_TOKENS.EXPIRES_AT)
                    .values(userCtx.uuid(), newRefreshToken, ipAddr,
                            OffsetDateTime.ofInstant(Utils.addToDate(DAYS_7),
                                    ZoneId.systemDefault()))
                    .execute();
        });

        Map<String, Object> claims = Map.of(
                "uuid", userCtx.uuid(),
                "type", userCtx.type());

        GoReturn<String> accessJwtResult = Auth.signJWT(
                claims,
                HS256_SECRET,
                MINUTES_30);
        if (accessJwtResult.error != null) {
            Utils.Logger.error("Error signing access JWT: {}",
                    accessJwtResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        response.cookie(PATIENT_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000,
                true,
                false);

        response.cookie(PATIENT_REFRESH_COOKIE,
                newRefreshToken,
                DAYS_7 / 1000,
                true,
                false);

        return null;
    }

    public static Object RefreshOrganization(Request request, Response response) {
        DSLContext dsl = Utils.getDSL();

        String ipAddr = request.ip();

        @Nullable
        UserCtx orgCtx = request.attribute(ORG_CTX);
        if (orgCtx == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        boolean orgExists = dsl
                .fetchExists(dsl.selectOne().from(ORGANIZATIONS)
                        .where(ORGANIZATIONS.ID.eq(orgCtx.uuid())));
        if (!orgExists) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        @Nullable
        String refreshToken = request.cookie(ORG_REFRESH_COOKIE);
        if (refreshToken == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }
        String newRefreshToken = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);
        dsl.transaction((ctx) -> {
            DSLContext innerDSL = ctx.dsl();

            innerDSL.update(ORG_REFRESH_TOKENS).set(ORG_REFRESH_TOKENS.USED, true)
                    .where(ORG_REFRESH_TOKENS.ORGANIZATION_ID.eq(orgCtx.uuid()),
                            ORG_REFRESH_TOKENS.TOKEN.eq(refreshToken))
                    .execute();
            innerDSL.insertInto(ORG_REFRESH_TOKENS, ORG_REFRESH_TOKENS.ORGANIZATION_ID,
                    ORG_REFRESH_TOKENS.TOKEN,
                    ORG_REFRESH_TOKENS.IP, ORG_REFRESH_TOKENS.EXPIRES_AT)
                    .values(orgCtx.uuid(), newRefreshToken, ipAddr,
                            OffsetDateTime.ofInstant(Utils.addToDate(DAYS_7),
                                    ZoneId.systemDefault()))
                    .execute();
        });

        Map<String, Object> claims = Map.of(
                "uuid", orgCtx.uuid(),
                "type", orgCtx.type());

        GoReturn<String> accessJwtResult = Auth.signJWT(
                claims,
                HS256_SECRET,
                MINUTES_30);
        if (accessJwtResult.error != null) {
            Utils.Logger.error("Error signing access JWT: {}",
                    accessJwtResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        response.cookie(ORG_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000,
                true,
                false);

        response.cookie(ORG_REFRESH_COOKIE,
                newRefreshToken,
                DAYS_7 / 1000,
                true,
                false);
        return null;
    }

    public static Object Logout(Request request, Response response) throws Exception {
        response.removeCookie(PATIENT_REFRESH_COOKIE);
        response.removeCookie(ORG_REFRESH_COOKIE);

        return Utils.structuredResponse(STATUS_OK, RESPONSE_OK);
    }

    public static Object Whoami(Request request, Response response) throws JsonProcessingException {
        UserCtx tmpUser = request.attribute(USER_CTX);
        UserCtx tmpOrg = request.attribute(ORG_CTX);

        UUID user = tmpUser != null ? tmpUser.uuid() : null;
        UUID org = tmpOrg != null ? tmpOrg.uuid() : null;

        return Utils.MAPPER.writeValueAsString(new ServerResponse.WhoamiResponse(user, org));
    }
}
