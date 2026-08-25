package com.healthqueue.controllers;

import static com.healthqueue.utils.Constants.BAD_REQUEST;
import static com.healthqueue.utils.Constants.ORG_CTX;
import static com.healthqueue.utils.Constants.PATIENT;
import static com.healthqueue.utils.Constants.STATUS_BAD_REQUEST;
import static com.healthqueue.utils.Constants.STATUS_OK;
import static com.healthqueue.utils.Constants.STATUS_UNAUTHORIZED;
import static com.healthqueue.utils.Constants.UNAUTHORIZED;
import static com.healthqueue.utils.Constants.USER_CTX;

import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.healthqueue.db.enums.AppointmentStatus;
import com.healthqueue.models.Values;
import com.healthqueue.models.Types.CreateAppointmentParams;
import com.healthqueue.models.Types.UpdateAppointmentParams;
import com.healthqueue.models.jooq.HealthQueueDB;
import com.healthqueue.utils.AuthContext.UserCtx;
import com.healthqueue.utils.ServerError;
import com.healthqueue.utils.ServerRequest.CreateAppointmentRequest;
import com.healthqueue.utils.ServerRequest.UpdateAppointmentStatusRequest;
import com.healthqueue.utils.ServerResponse.AppointmentResponse;
import com.healthqueue.utils.Utils;

import jakarta.validation.ConstraintViolation;
import spark.Request;
import spark.Response;

public class AppointmentController {
    public static Object CreateAppointment(Request request, Response response) throws Exception {
        @Nullable
        UserCtx userCtx = request.attribute(USER_CTX);
        if (userCtx == null || !userCtx.type().equals(PATIENT)) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }
        final HealthQueueDB db = Utils.getDB();

        CreateAppointmentRequest body = Utils.fromJSON(request.body(), CreateAppointmentRequest.class);
        Set<ConstraintViolation<CreateAppointmentRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        // Ensure the appointment is for the authenticated user
        if (!userCtx.uuid().equals(body.userId())) {
            throw new ServerError(STATUS_UNAUTHORIZED, "Cannot create appointments for other users");
        }

        // Check doctor availability
        boolean isDoctorAvailable = db.doctors().checkDoctorAvailability(
                body.doctorId(), body.scheduledStartTime(), body.scheduledEndTime());

        if (!isDoctorAvailable) {
            throw new ServerError(STATUS_BAD_REQUEST, "Doctor is not available at the scheduled time.");
        }

        long appointmentId = Utils.nextSnowflakeId();
        CreateAppointmentParams params = CreateAppointmentParams.builder()
                .id(appointmentId)
                .userId(body.userId())
                .branchId(body.branchId())
                .doctorId(body.doctorId())
                .scheduledStartTime(body.scheduledStartTime())
                .scheduledEndTime(body.scheduledEndTime())
                .status(AppointmentStatus.SCHEDULED) // Default status
                .build();

        Values.Appointment newAppointment = db.appointments().createAndReturn(params);

        AppointmentResponse respData = new AppointmentResponse(
                newAppointment.id,
                newAppointment.userId,
                newAppointment.branchId,
                newAppointment.doctorId,
                newAppointment.status.toString(),
                newAppointment.scheduledStartTime,
                newAppointment.scheduledEndTime,
                newAppointment.actualStartTime,
                newAppointment.actualEndTime);

        return Utils.structuredResponse(STATUS_OK, "Appointment created successfully", respData);
    }

    public static Object UpdateStatus(Request request, Response response) throws Exception {
        @Nullable
        UserCtx orgCtx = request.attribute(ORG_CTX);
        if (orgCtx == null) {
            throw new ServerError(STATUS_UNAUTHORIZED, UNAUTHORIZED);
        }
        final HealthQueueDB db = Utils.getDB();

        UpdateAppointmentStatusRequest body = Utils.fromJSON(request.body(), UpdateAppointmentStatusRequest.class);
        Set<ConstraintViolation<UpdateAppointmentStatusRequest>> violations = Utils.validate(body);
        if (!violations.isEmpty()) {
            throw new ServerError(STATUS_BAD_REQUEST, BAD_REQUEST, Utils.toValidationErrors(violations));
        }

        AppointmentStatus newStatus = AppointmentStatus.valueOf(body.status().toUpperCase());

        UpdateAppointmentParams params = UpdateAppointmentParams.builder()
                .appointmentId(body.appointmentId())
                .status(newStatus)
                .actualStartTime(body.actualStartTime())
                .actualEndTime(body.actualEndTime())
                .build();

        db.appointments().updateStatus(params);

        return Utils.structuredResponse(STATUS_OK, "Appointment status updated successfully");
    }

    public static Object ReassignDoctor(Request req, Response res) {

        return 2;
    }
}
