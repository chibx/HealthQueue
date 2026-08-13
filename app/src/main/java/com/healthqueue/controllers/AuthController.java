package com.healthqueue.controllers;

import static com.healthqueue.utils.Constants.*;
import static com.healthqueue.db.Tables.ORGANIZATIONS;
import static com.healthqueue.db.Tables.ORGANIZATION_REQUESTS;
import static com.healthqueue.db.Tables.ORG_REFRESH_TOKENS;
import static com.healthqueue.db.Tables.USERS;
import static com.healthqueue.db.Tables.USER_REFRESH_TOKENS;

import com.healthqueue.db.tables.pojos.Organizations;
import com.healthqueue.models.Interfaces.HealthQueueDatabase;
import com.healthqueue.models.Types.CreateOrgParams;
import com.healthqueue.models.Types.CreateOrgSessionParams;
import com.healthqueue.models.Types.CreatePatientParams;
import com.healthqueue.models.Types.CreatePatientSessionParams;
import com.healthqueue.models.Types.DatabaseException;
import com.healthqueue.models.Values.CreateOrg;
import com.healthqueue.models.Values.CreatePatient;
import com.healthqueue.models.Values.GetOrgLogin;
import com.healthqueue.models.Values.GetPatientLogin;
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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jspecify.annotations.Nullable;

import spark.Request;
import spark.Response;
import javax.crypto.spec.SecretKeySpec;

public class AuthController {

    private static final SecretKey HS256_SECRET = new SecretKeySpec(Auth.HS256_SECRET_STRING.getBytes(),
            Auth.HMAC256_ALGORITHM);

    public static Object RegisterPatient(Request request, Response response) throws Exception {
        final HealthQueueDatabase db = Utils.getDB();

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
        CreatePatient newUser;

        try {
            CreatePatientParams params = CreatePatientParams.builder()
                    .id(newUserId)
                    .firstName(body.firstName())
                    .lastName(body.lastName())
                    .email(body.email())
                    .passwordHash(passwordHash).build();
            newUser = db.patients().createAndReturn(params);
        } catch (DatabaseException dae) {
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
                "uuid", newUser.id.toString(),
                "type", PATIENT);

        GoReturn<String> accessJwtResult = Auth.signJWT(
                claims,
                HS256_SECRET,
                MINUTES_30);
        if (accessJwtResult.error != null) {
            Utils.Logger.error("Error signing access JWT: {}", accessJwtResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        String refreshToken = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);

        CreatePatientSessionParams params = CreatePatientSessionParams.builder()
                .id(newUser.id)
                .token(refreshToken).ipAddr(ipAddr)
                .expiryDate(Utils.addToDate(DAYS_7))
                .build();

        db.patients().createSession(params);

        response.cookie(ORG_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000, true, false);
        response.cookie(PATIENT_REFRESH_COOKIE, refreshToken, DAYS_7 / 1000, true, false);

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object LoginPatient(Request request, Response response) throws Exception {
        final HealthQueueDatabase db = Utils.getDB();

        String ipAddr = request.ip();
        PatientLoginRequest body = Utils.fromJSON(request.body(), PatientLoginRequest.class);
        Set<ConstraintViolation<PatientLoginRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        @Nullable
        GetPatientLogin user = db.patients().getPatientLogin(body.email());

        if (user == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        GoReturn<Boolean> verifyResult = Auth.verifyHash(user.passwordHash, body.password());
        if (verifyResult.error != null || !verifyResult.value) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        Map<String, Object> claims = Map.of(
                "uuid", user.id.toString(),
                "type", PATIENT);

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

        CreatePatientSessionParams params = CreatePatientSessionParams.builder()
                .id(user.id)
                .token(refreshToken).ipAddr(ipAddr)
                .expiryDate(Utils.addToDate(DAYS_7))
                .build();

        db.patients().createSession(params);

        response.cookie(PATIENT_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000, true, false);
        response.cookie(PATIENT_REFRESH_COOKIE, refreshToken, DAYS_7 / 1000, true, false);

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object RegisterOrganization(Request request, Response response) throws Exception {
        final HealthQueueDatabase db = Utils.getDB();

        String ipAddr = request.ip();
        OrganizationSignupRequest body = Utils.fromJSON(request.body(), OrganizationSignupRequest.class);
        Set<ConstraintViolation<OrganizationSignupRequest>> violations = Utils.validate(body);

        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        boolean doesCodeExist = db.organizations().hasRegistrationCode(body.email());
        if (doesCodeExist == false) {
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
        CreateOrg newOrg;

        try {
            CreateOrgParams params = CreateOrgParams.builder()
                    .id(newOrgId).name(body.name())
                    .email(body.email())
                    .passwordHash(passwordHash).build();
            newOrg = db.organizations().createAndReturn(params);
        } catch (DatabaseException dae) {
            if (dae.getMessage() != null && dae.getMessage()
                    .contains("duplicate key value violates unique constraint \"organizations_email_key\"")) {
                throw new ServerError(STATUS_BAD_REQUEST, "Account with this email already exists");

            }
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR, dae);
        }

        if (newOrg == null) {
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        Map<String, Object> claims = Map.of(
                "uuid", newOrg.id.toString(),
                "type", ORG_KEY);

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

        CreateOrgSessionParams params = CreateOrgSessionParams.builder()
                .id(newOrg.id)
                .token(refreshToken)
                .ipAddr(ipAddr)
                .expiryDate(Utils.addToDate(DAYS_7))
                .build();
        db.organizations().createSession(params);

        response.cookie(ORG_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000, true, false);
        response.cookie(ORG_REFRESH_COOKIE, refreshToken, DAYS_7 / 1000, true, false);

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object LoginOrganization(Request request, Response response) throws Exception {
        final HealthQueueDatabase db = Utils.getDB();

        String ipAddr = request.ip();
        OrganizationLoginRequest body = Utils.fromJSON(request.body(), OrganizationLoginRequest.class);
        Set<ConstraintViolation<OrganizationLoginRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        GetOrgLogin org = db.organizations().getOrgLogin(body.email());

        if (org == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        GoReturn<Boolean> verifyResult = Auth.verifyHash(org.passwordHash, body.password());
        if (verifyResult.error != null || !verifyResult.value) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        Map<String, Object> claims = Map.of(
                "uuid", org.id.toString(),
                "type", ORG_KEY);

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

        CreateOrgSessionParams params = CreateOrgSessionParams.builder()
                .id(org.id)
                .token(refreshToken)
                .ipAddr(ipAddr)
                .expiryDate(Utils.addToDate(DAYS_7))
                .build();
        db.organizations().createSession(params);

        response.cookie(ORG_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000, true, false);
        response.cookie(ORG_REFRESH_COOKIE, refreshToken, DAYS_7 / 1000, true, false);

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object RefreshPatient(Request request, Response response) throws Exception {
        DSLContext dsl = Utils.getDSL();
        final HealthQueueDatabase db = Utils.getDB();

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

        return Utils.DEFAULT_OK_RESP;
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

            // innerDSL.update(ORG_REFRESH_TOKENS).set(ORG_REFRESH_TOKENS.USED, true)
            // .where(ORG_REFRESH_TOKENS.ORGANIZATION_ID.eq(orgCtx.uuid()),
            // ORG_REFRESH_TOKENS.TOKEN.eq(refreshToken))
            // .execute();

            // Delete token instead
            innerDSL.deleteFrom(ORG_REFRESH_TOKENS)
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
        return Utils.DEFAULT_OK_RESP;
    }

    public static Object LogoutPatient(Request request, Response response) throws Exception {
        DSLContext dsl = Utils.getDSL();

        @Nullable
        String refreshToken = request.cookie(PATIENT_REFRESH_COOKIE);
        @Nullable
        UserCtx userCtx = request.attribute(USER_CTX);

        response.removeCookie(PATIENT_REFRESH_COOKIE);
        response.removeCookie(PATIENT_ACCESS_COOKIE);

        if (refreshToken == null || userCtx == null)
            return Utils.DEFAULT_OK_RESP;

        try {
            dsl.deleteFrom(USER_REFRESH_TOKENS)
                    .where(USER_REFRESH_TOKENS.USER_ID.eq(userCtx.uuid()),
                            USER_REFRESH_TOKENS.TOKEN.eq(refreshToken))
                    .execute();
        } catch (Exception e) {
        }

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object LogoutOrganization(Request request, Response response) throws Exception {
        DSLContext dsl = Utils.getDSL();

        @Nullable
        String refreshToken = request.cookie(ORG_REFRESH_COOKIE);
        @Nullable
        UserCtx orgCtx = request.attribute(ORG_CTX);

        response.removeCookie(ORG_REFRESH_COOKIE);
        response.removeCookie(ORG_ACCESS_COOKIE);

        if (refreshToken == null || orgCtx == null)
            return Utils.DEFAULT_OK_RESP;

        try {
            dsl.deleteFrom(ORG_REFRESH_TOKENS)
                    .where(ORG_REFRESH_TOKENS.ORGANIZATION_ID.eq(orgCtx.uuid()),
                            ORG_REFRESH_TOKENS.TOKEN.eq(refreshToken))
                    .execute();
        } catch (Exception e) {
        }

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object Whoami(Request request, Response response) throws JsonProcessingException {
        UserCtx tmpUser = request.attribute(USER_CTX);
        UserCtx tmpOrg = request.attribute(ORG_CTX);

        UUID user = tmpUser != null ? tmpUser.uuid() : null;
        UUID org = tmpOrg != null ? tmpOrg.uuid() : null;

        return Utils.MAPPER.writeValueAsString(new ServerResponse.WhoamiResponse(user, org));
    }
}
