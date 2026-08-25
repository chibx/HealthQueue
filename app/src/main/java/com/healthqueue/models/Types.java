package com.healthqueue.models;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Builder;
import com.healthqueue.db.enums.AppointmentStatus;

public class Types {
    public static class DatabaseException extends Exception {
        public DatabaseException(Throwable cause) {
            super(cause);
        }
    }

    @Builder
    public static class CreatePatientParams {
        public UUID id;
        public String firstName;
        public String lastName;
        public String email;
        public String passwordHash;
        public double latitude;
        public double longitude;
    }

    @Builder
    public static class CreateOrgParams {
        public UUID id;
        public String name;
        public String email;
        public String passwordHash;
    }

    @Builder
    public static class CreatePatientSessionParams {
        public UUID id;
        public String ipAddr;
        public String token;
        public Instant expiryDate;
    }

    @Builder
    public static class CreateOrgSessionParams {
        public UUID id;
        public String ipAddr;
        public String token;
        public Instant expiryDate;
    }

    @Builder
    public static class CreateStaffParams {
        public long id;
        public String firstName;
        public String lastName;
        public String specialty;
        public long branchId;
    }

    @Builder
    public static class CreateAppointmentParams {
        public long id;
        public UUID userId;
        public long branchId;
        public long doctorId;
        public ZonedDateTime scheduledStartTime;
        public ZonedDateTime scheduledEndTime;
        public AppointmentStatus status;
    }

    @Builder
    public static class UpdateAppointmentParams {
        public long appointmentId;
        public AppointmentStatus status;
        public ZonedDateTime actualStartTime;
        public ZonedDateTime actualEndTime;
    }

    @Builder
    public static class ReassignDoctorParams {
        public long appointmentId;
        public long newDoctorId;
    }

    @Builder
    public static class RecordClinicVisitParams {
        public long id;
        public long appointmentId;
        public UUID userId;
        public long doctorId;
        public String visitNotes;
        public String prescriptionDetails;
    }
}