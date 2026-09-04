package com.healthqueue.controllers;

import static com.healthqueue.utils.Constants.*;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

import com.healthqueue.models.Values.DoctorProfile;
import com.healthqueue.models.Values.OrgProfile;
import com.healthqueue.models.Values.PatientProfile;
import com.healthqueue.models.jooq.HealthQueueDB;
import com.healthqueue.utils.AuthContext.DoctorCtx;
import com.healthqueue.utils.AuthContext.UserCtx;
import com.healthqueue.utils.ServerError;
import com.healthqueue.utils.ServerResponse.DoctorProfileResponse;
import com.healthqueue.utils.ServerResponse.OrganizationProfileResponse;
import com.healthqueue.utils.ServerResponse.PatientProfileResponse;
import com.healthqueue.utils.Utils;

import spark.Request;
import spark.Response;

public class ProfileController {

    public static Object GetPatientProfile(Request request, Response response) throws Exception {
        @Nullable
        UserCtx userCtx = request.attribute(USER_CTX);
        if (userCtx == null || !PATIENT.equals(userCtx.type())) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        final HealthQueueDB db = Utils.getDB();
        PatientProfile profile = db.patients().getProfile(userCtx.uuid());
        if (profile == null) {
            throw new ServerError(STATUS_NOT_FOUND, NOT_FOUND);
        }

        PatientProfileResponse resp = new PatientProfileResponse(
                profile.firstName,
                profile.lastName,
                profile.fullName,
                profile.email
        );
        return Utils.structuredResponse(STATUS_OK, "Patient profile retrieved successfully", resp);
    }

    public static Object GetOrganizationProfile(Request request, Response response) throws Exception {
        @Nullable
        UserCtx orgCtx = request.attribute(ORG_CTX);
        if (orgCtx == null || !ORG_KEY.equals(orgCtx.type())) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        final HealthQueueDB db = Utils.getDB();
        OrgProfile profile = db.organizations().getProfile(orgCtx.uuid());
        if (profile == null) {
            throw new ServerError(STATUS_NOT_FOUND, NOT_FOUND);
        }

        OrganizationProfileResponse resp = new OrganizationProfileResponse(
                profile.name,
                profile.email
        );
        return Utils.structuredResponse(STATUS_OK, "Organization profile retrieved successfully", resp);
    }

    public static Object GetDoctorProfile(Request request, Response response) throws Exception {
        @Nullable
        DoctorCtx doctorCtx = request.attribute(DOCTOR_CTX);
        if (doctorCtx == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }

        final HealthQueueDB db = Utils.getDB();
        DoctorProfile profile = db.doctors().getProfile(doctorCtx.doctorId());
        if (profile == null) {
            throw new ServerError(STATUS_NOT_FOUND, NOT_FOUND);
        }

        DoctorProfileResponse resp = new DoctorProfileResponse(
                profile.id,
                profile.firstName,
                profile.lastName,
                profile.fullName,
                profile.specialty,
                profile.email,
                profile.organizationName
        );
        return Utils.structuredResponse(STATUS_OK, "Doctor profile retrieved successfully", resp);
    }
}
