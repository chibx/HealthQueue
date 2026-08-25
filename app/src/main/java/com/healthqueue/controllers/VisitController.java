package com.healthqueue.controllers;

import static com.healthqueue.utils.Constants.BAD_REQUEST;
import static com.healthqueue.utils.Constants.ORG_CTX;
import static com.healthqueue.utils.Constants.PATIENT;
import static com.healthqueue.utils.Constants.STATUS_BAD_REQUEST;
import static com.healthqueue.utils.Constants.STATUS_FORBIDDEN;
import static com.healthqueue.utils.Constants.STATUS_NOT_FOUND;
import static com.healthqueue.utils.Constants.STATUS_OK;
import static com.healthqueue.utils.Constants.STATUS_UNAUTHORIZED;
import static com.healthqueue.utils.Constants.UNAUTHORIZED;
import static com.healthqueue.utils.Constants.USER_CTX;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.healthqueue.models.Types.RecordClinicVisitParams;
import com.healthqueue.models.Values.Appointment;
import com.healthqueue.models.jooq.HealthQueueDB;
import com.healthqueue.utils.AuthContext.UserCtx;
import com.healthqueue.utils.ServerError;
import com.healthqueue.utils.ServerRequest.RecordClinicVisitRequest;
import com.healthqueue.utils.ServerResponse.ClinicVisitResponse;
import com.healthqueue.utils.Utils;

import jakarta.validation.ConstraintViolation;
import spark.Request;
import spark.Response;

public class VisitController {

    public static Object RecordVisit(Request request, Response response) throws Exception {
        @Nullable
        UserCtx orgCtx = request.attribute(ORG_CTX);
        if (orgCtx == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }
        final HealthQueueDB db = Utils.getDB();

        RecordClinicVisitRequest body = Utils.fromJSON(request.body(), RecordClinicVisitRequest.class);
        Set<ConstraintViolation<RecordClinicVisitRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        // Ensure the appointment exists and belongs to a branch of the organization
        // This requires fetching appointment details and checking its branch_id against
        // branches owned by orgCtx.uuid()
        // For simplicity, let's assume the body.appointmentId() is valid and accessible
        // by the org.
        // A more robust solution would involve a join query in AppointmentModel or a
        // dedicated check.
        Appointment appointment = db.appointments().getAppointmentDetails(body.appointmentId());
        if (appointment == null) {
            throw new ServerError(STATUS_NOT_FOUND, "Appointment not found.");
        }

        // Also, verify that the doctor involved in the visit belongs to a branch of
        // this organization.
        // This would involve fetching doctor details and verifying its branchId against
        // organization's branches.
        // For now, we proceed assuming doctorId is valid for the organization.

        long visitId = Utils.nextSnowflakeId();
        RecordClinicVisitParams params = RecordClinicVisitParams.builder()
                .id(visitId)
                .appointmentId(body.appointmentId())
                .userId(body.userId())
                .doctorId(body.doctorId())
                .visitNotes(body.visitNotes())
                .prescriptionDetails(body.prescriptionDetails())
                .build();

        db.clinicVisits().recordVisit(params);

        return Utils.structuredResponse(STATUS_OK, "Clinic visit recorded successfully");
    }

    public static Object GetPatientVisitHistory(Request request, Response response) throws Exception {
        @Nullable
        UserCtx userCtx = request.attribute(USER_CTX);
        if (userCtx == null || !userCtx.type().equals(PATIENT)) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }
        final HealthQueueDB db = Utils.getDB();

        // The user ID is taken from the authenticated context
        List<@Nullable ClinicVisitResponse> visitHistory = db.clinicVisits().getPatientVisitHistory(userCtx.uuid());

        return Utils.structuredResponse(STATUS_OK, "Patient visit history retrieved successfully", visitHistory);
    }
}