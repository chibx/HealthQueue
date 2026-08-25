package com.healthqueue.controllers;

import static com.healthqueue.utils.Constants.BAD_REQUEST;
import static com.healthqueue.utils.Constants.ORG_CTX;
import static com.healthqueue.utils.Constants.STATUS_BAD_REQUEST;
import static com.healthqueue.utils.Constants.STATUS_OK;
import static com.healthqueue.utils.Constants.STATUS_UNAUTHORIZED;
import static com.healthqueue.utils.Constants.STATUS_NOT_FOUND;
import static com.healthqueue.utils.Constants.UNAUTHORIZED;

import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.healthqueue.models.Types.CreateStaffParams;
import com.healthqueue.models.jooq.HealthQueueDB;
import com.healthqueue.utils.Utils;

import jakarta.validation.ConstraintViolation;

import com.healthqueue.utils.AuthContext.UserCtx;
import com.healthqueue.utils.ServerError;
import com.healthqueue.utils.ServerRequest.*;

import spark.Request;
import spark.Response;

public class DoctorController {
    public static Object CreateDoctor(Request request, Response response) throws Exception {
        @Nullable
        UserCtx orgCtx = request.attribute(ORG_CTX);
        if (orgCtx == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }
        final HealthQueueDB db = Utils.getDB();

        CreateDoctorRequest body = Utils.fromJSON(request.body(), CreateDoctorRequest.class);
        Set<ConstraintViolation<CreateDoctorRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }
        if (!db.branches().belongsToOrganization(body.branchId(), orgCtx.uuid())) {
            throw new ServerError(STATUS_NOT_FOUND, "Branch not found.");
        }

        CreateStaffParams params = CreateStaffParams.builder()
                .id(Utils.nextSnowflakeId()).branchId(body.branchId()).firstName(body.firstName())
                .lastName(body.lastName()).specialty(body.specialty()).build();

        db.doctors().create(params);

        return Utils.structuredResponse(STATUS_OK, "Staff created successfully");
    }

    public static Object DeleteDoctor(Request request, Response response) throws Exception {
        @Nullable
        UserCtx orgCtx = request.attribute(ORG_CTX);
        if (orgCtx == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }
        final HealthQueueDB db = Utils.getDB();

        DeleteDoctorRequest body = Utils.fromJSON(request.body(), DeleteDoctorRequest.class);
        Set<ConstraintViolation<DeleteDoctorRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        if (!db.doctors().belongsToBranch(body.doctorId(), body.branchId())) {
            throw new ServerError(STATUS_NOT_FOUND, "Doctor not found.");
        }
        db.doctors().delete(orgCtx.uuid(), body.branchId(), body.doctorId());

        return Utils.structuredResponse(STATUS_OK, "Staff removed successfully");
    }

    public static Object UpdateDoctorBranch(Request request, Response response) throws Exception {
        @Nullable
        UserCtx orgCtx = request.attribute(ORG_CTX);
        if (orgCtx == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }
        final HealthQueueDB db = Utils.getDB();

        ChangeDoctorBranchRequest body = Utils.fromJSON(request.body(), ChangeDoctorBranchRequest.class);
        Set<ConstraintViolation<ChangeDoctorBranchRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        if (!db.doctors().belongsToBranch(body.doctorId(), body.branchId())
                || !db.branches().belongsToOrganization(body.newBranchId(), orgCtx.uuid())) {
            throw new ServerError(STATUS_NOT_FOUND, "Doctor or branch not found.");
        }
        db.doctors().updateBranch(orgCtx.uuid(), body.newBranchId(), body.branchId(), body.doctorId());

        return Utils.structuredResponse(STATUS_OK, "Doctor's branch updated successfully");
    }
}
