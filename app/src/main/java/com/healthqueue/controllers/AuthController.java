package com.healthqueue.controllers;

import static com.healthqueue.utils.Constants.*;
import com.healthqueue.models.Types.CreateOrgParams;
import com.healthqueue.models.Types.CreateOrgSessionParams;
import com.healthqueue.models.Types.CreatePatientParams;
import com.healthqueue.models.Types.CreatePatientSessionParams;
import com.healthqueue.models.Types.CreateDoctorSessionParams;
import com.healthqueue.models.Types.DatabaseException;
import com.healthqueue.models.Values.CreateOrg;
import com.healthqueue.models.Values.CreatePatient;
import com.healthqueue.models.Values.GetOrgLogin;
import com.healthqueue.models.Values.GetPatientLogin;
import com.healthqueue.models.Values.GetDoctorLogin;
import com.healthqueue.models.jooq.HealthQueueDB;
import com.healthqueue.utils.Auth;
import com.healthqueue.utils.AuthContext.UserCtx;
import com.healthqueue.utils.AuthContext.DoctorCtx;
import com.healthqueue.utils.ServerError;
import com.healthqueue.utils.ServerRequest.OrganizationLoginRequest;
import com.healthqueue.utils.ServerRequest.OrganizationSignupRequest;
import com.healthqueue.utils.ServerRequest.PatientLoginRequest;
import com.healthqueue.utils.ServerRequest.PatientSignupRequest;
import com.healthqueue.utils.ServerRequest.DoctorLoginRequest;
import com.healthqueue.utils.ServerResponse;
import com.healthqueue.utils.Utils;
import com.healthqueue.utils.Utils.GoReturn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.validation.ConstraintViolation;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import spark.Request;
import spark.Response;

public class AuthController {

    public static Object RegisterPatient(Request request, Response response) throws Exception {
        final HealthQueueDB db = Utils.getDB();

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
                Auth.HS256_SECRET,
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

        response.cookie("/", PATIENT_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000, false, false);
        response.cookie("/", PATIENT_REFRESH_COOKIE, refreshToken, DAYS_7 / 1000, false, false);

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object LoginPatient(Request request, Response response) throws Exception {
        final HealthQueueDB db = Utils.getDB();

        String ipAddr = request.ip();
        PatientLoginRequest body = Utils.fromJSON(request.body(), PatientLoginRequest.class);

        Set<ConstraintViolation<PatientLoginRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        @Nullable
        GetPatientLogin user;

        try {
            user = db.patients().getPatientLogin(body.email());
        } catch (Exception e) {
            Utils.Logger.error("Error getting patient login details: {}", e.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR, e);
        }

        if (user == null) {
            throw new ServerError(STATUS_NOT_FOUND, NOT_FOUND);
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
                Auth.HS256_SECRET,
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

        try {
            db.patients().createSession(params);
        } catch (Exception e) {
            Utils.Logger.error("Error creating patient: {}",
                    accessJwtResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR, e);
        }

        response.cookie("/", PATIENT_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000, false, false);
        response.cookie("/", PATIENT_REFRESH_COOKIE, refreshToken, DAYS_7 / 1000, false, false);

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object RegisterOrganization(Request request, Response response) throws Exception {
        final HealthQueueDB db = Utils.getDB();

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
                Auth.HS256_SECRET,
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

        response.cookie("/", ORG_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000, false, false);
        response.cookie("/", ORG_REFRESH_COOKIE, refreshToken, DAYS_7 / 1000, false, false);

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object LoginOrganization(Request request, Response response) throws Exception {
        final HealthQueueDB db = Utils.getDB();

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
                Auth.HS256_SECRET,
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

        response.cookie("/", ORG_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000, false, false);
        response.cookie("/", ORG_REFRESH_COOKIE, refreshToken, DAYS_7 / 1000, false, false);

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object RefreshPatient(Request request, Response response) throws Exception {
        final HealthQueueDB db = Utils.getDB();

        String ipAddr = request.ip();
        @Nullable
        String refreshToken = request.cookie(PATIENT_REFRESH_COOKIE);
        if (refreshToken == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        UUID userId = db.patients().getSessionUserId(refreshToken);
        if (userId == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        String newRefreshToken = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);

        try {
            db.runTx((tx) -> {
                CreatePatientSessionParams params = CreatePatientSessionParams.builder()
                        .id(userId)
                        .token(newRefreshToken).ipAddr(ipAddr)
                        .expiryDate(Utils.addToDate(DAYS_7))
                        .build();

                tx.patients().deleteSession(userId, refreshToken);
                tx.patients().createSession(params);
            });
        } catch (Exception e) {
            Utils.Logger.error("Error occurred when refreshing patient session", e);
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        Map<String, Object> claims = Map.of(
                "uuid", userId.toString(),
                "type", PATIENT);

        GoReturn<String> accessJwtResult = Auth.signJWT(
                claims,
                Auth.HS256_SECRET,
                MINUTES_30);
        if (accessJwtResult.error != null) {
            Utils.Logger.error("Error signing access JWT: {}",
                    accessJwtResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        response.cookie("/", PATIENT_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000, false, false);
        response.cookie("/", PATIENT_REFRESH_COOKIE, newRefreshToken, DAYS_7 / 1000, false, false);

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object LoginDoctor(Request request, Response response) throws Exception {
        final HealthQueueDB db = Utils.getDB();
        DoctorLoginRequest body = Utils.fromJSON(request.body(), DoctorLoginRequest.class);
        Set<ConstraintViolation<DoctorLoginRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }
        GetDoctorLogin doctor = db.doctors().getDoctorLogin(body.organizationName(), body.email());
        if (doctor == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }
        GoReturn<Boolean> verify = Auth.verifyHash(doctor.passwordHash, body.password());
        if (verify.error != null || !verify.value)
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        GoReturn<String> access = Auth.signJWT(Map.of("doctorId", doctor.id, "type", DOCTOR), Auth.HS256_SECRET);
        if (access.error != null)
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        String refresh = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);
        db.doctors().createSession(CreateDoctorSessionParams.builder().id(doctor.id).token(refresh)
                .ipAddr(request.ip()).expiryDate(Utils.addToDate(DAYS_7)).build());
        response.cookie("/", DOCTOR_ACCESS_COOKIE, access.value, MINUTES_30 / 1000, false, false);
        response.cookie("/", DOCTOR_REFRESH_COOKIE, refresh, DAYS_7 / 1000, false, false);
        return Utils.DEFAULT_OK_RESP;
    }

    public static Object RefreshDoctor(Request request, Response response) throws Exception {
        final HealthQueueDB db = Utils.getDB();
        @Nullable
        String refresh = request.cookie(DOCTOR_REFRESH_COOKIE);
        if (refresh == null)
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        long doctorId = db.doctors().getSessionDoctorId(refresh);
        if (doctorId < 1)
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        String newRefresh = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);
        db.runTx(tx -> {
            tx.doctors().deleteSession(doctorId, refresh);
            tx.doctors().createSession(CreateDoctorSessionParams.builder().id(doctorId).token(newRefresh)
                    .ipAddr(request.ip()).expiryDate(Utils.addToDate(DAYS_7)).build());
        });
        GoReturn<String> access = Auth.signJWT(Map.of("doctorId", doctorId, "type", DOCTOR), Auth.HS256_SECRET);
        if (access.error != null)
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        response.cookie("/", DOCTOR_ACCESS_COOKIE, access.value, MINUTES_30 / 1000, false, false);
        response.cookie("/", DOCTOR_REFRESH_COOKIE, newRefresh, DAYS_7 / 1000, false, false);
        return Utils.DEFAULT_OK_RESP;
    }

    public static Object LogoutDoctor(Request request, Response response) throws Exception {
        @Nullable
        String refresh = request.cookie(DOCTOR_REFRESH_COOKIE);
        @Nullable
        DoctorCtx doctor = request.attribute(DOCTOR_CTX);
        response.removeCookie(DOCTOR_REFRESH_COOKIE);
        response.removeCookie(DOCTOR_ACCESS_COOKIE);
        if (refresh != null && doctor != null) {
            Utils.getDB().doctors().deleteSession(doctor.doctorId(), refresh);
        }
        return Utils.DEFAULT_OK_RESP;
    }

    public static Object RefreshOrganization(Request request, Response response) throws Exception {
        final HealthQueueDB db = Utils.getDB();
        String ipAddr = request.ip();

        @Nullable
        String refreshToken = request.cookie(ORG_REFRESH_COOKIE);
        if (refreshToken == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        UUID orgId = db.organizations().getSessionOrgId(refreshToken);
        if (orgId == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        String newRefreshToken = Utils.cryptoRandomString(REFRESH_TOKEN_LENGTH);

        try {
            db.runTx((tx) -> {
                CreateOrgSessionParams params = CreateOrgSessionParams.builder()
                        .id(orgId)
                        .token(newRefreshToken).ipAddr(ipAddr)
                        .expiryDate(Utils.addToDate(DAYS_7))
                        .build();

                tx.organizations().deleteSession(orgId, refreshToken);
                tx.organizations().createSession(params);
            });
        } catch (Exception e) {
            Utils.Logger.error("Error occurred when refreshing organizations session", e);
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        Map<String, Object> claims = Map.of(
                "uuid", orgId.toString(),
                "type", ORG_KEY);

        GoReturn<String> accessJwtResult = Auth.signJWT(
                claims,
                Auth.HS256_SECRET,
                MINUTES_30);
        if (accessJwtResult.error != null) {
            Utils.Logger.error("Error signing access JWT: {}", accessJwtResult.error.getMessage());
            throw new ServerError(STATUS_INTERNAL_ERROR, INTERNAL_ERROR);
        }

        response.cookie("/", ORG_ACCESS_COOKIE, accessJwtResult.value, MINUTES_30 / 1000, false, false);
        response.cookie("/", ORG_REFRESH_COOKIE, newRefreshToken, DAYS_7 / 1000, false, false);

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object LogoutPatient(Request request, Response response) throws Exception {
        final HealthQueueDB db = Utils.getDB();

        @Nullable
        String refreshToken = request.cookie(PATIENT_REFRESH_COOKIE);
        @Nullable
        UserCtx userCtx = request.attribute(USER_CTX);

        response.removeCookie(PATIENT_REFRESH_COOKIE);
        response.removeCookie(PATIENT_ACCESS_COOKIE);

        if (refreshToken == null || userCtx == null)
            return Utils.DEFAULT_OK_RESP;

        try {
            db.patients().deleteSession(userCtx.uuid(), refreshToken);
        } catch (Exception e) {
            Utils.Logger.error("Error logging user out:", e);
        }

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object LogoutOrganization(Request request, Response response) throws Exception {
        final HealthQueueDB db = Utils.getDB();

        @Nullable
        String refreshToken = request.cookie(ORG_REFRESH_COOKIE);
        @Nullable
        UserCtx orgCtx = request.attribute(ORG_CTX);

        response.removeCookie(ORG_REFRESH_COOKIE);
        response.removeCookie(ORG_ACCESS_COOKIE);

        if (refreshToken == null || orgCtx == null) {
            return Utils.DEFAULT_OK_RESP;
        }

        try {
            db.organizations().deleteSession(orgCtx.uuid(), refreshToken);
        } catch (Exception e) {
            Utils.Logger.error("Error logging user out:", e);
        }

        return Utils.DEFAULT_OK_RESP;
    }

    public static Object Whoami(Request request, Response response) throws JsonProcessingException {
        UserCtx tmpUser = request.attribute(USER_CTX);
        UserCtx tmpOrg = request.attribute(ORG_CTX);

        UUID user = tmpUser != null ? tmpUser.uuid() : null;
        UUID org = tmpOrg != null ? tmpOrg.uuid() : null;
        @Nullable
        DoctorCtx tmpDoctor = request.attribute(DOCTOR_CTX);
        Long doctor = tmpDoctor != null ? tmpDoctor.doctorId() : null;

        return Utils.MAPPER.writeValueAsString(new ServerResponse.WhoamiResponse(user, org, doctor));
    }
}
