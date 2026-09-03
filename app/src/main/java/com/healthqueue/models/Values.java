package com.healthqueue.models;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.healthqueue.db.enums.AppointmentStatus;

import lombok.Builder;

public class Values {
    public static class CreatePatient {
        public final UUID id;

        public CreatePatient(UUID uuid) {
            this.id = uuid;
        }
    }

    public static class CreateOrg {
        public final UUID id;

        public CreateOrg(UUID uuid) {
            this.id = uuid;
        }
    }

    @Builder
    public static class Appointment {
        public long id;
        public UUID userId;
        public long branchId;
        public long doctorId;
        public AppointmentStatus status;
        public ZonedDateTime scheduledStartTime;
        public ZonedDateTime scheduledEndTime;
        public ZonedDateTime actualStartTime;
        public ZonedDateTime actualEndTime;
    }

    @Builder
    public static class Doctor {
        public long id;
        public long branchId;
        public String firstName;
        public String lastName;
        public String specialty;
        public boolean isAvailable;
    }

    public static class GetPatientLogin {
        public final UUID id;
        public final String passwordHash;

        public GetPatientLogin(UUID id, String password) {
            this.id = id;
            this.passwordHash = password;
        }
    }

    public static class GetOrgLogin {
        public final UUID id;
        public final String passwordHash;

        public GetOrgLogin(UUID id, String password) {
            this.id = id;
            this.passwordHash = password;
        }
    }

    public static class GetDoctorLogin {
        public final long id;
        public final String passwordHash;

        public GetDoctorLogin(long id, String passwordHash) {
            this.id = id;
            this.passwordHash = passwordHash;
        }
    }

    public static class GetNearestBranches {
        public final long id;
        public final String name;
        public final String address;
        public final double distanceInMeters;

        public GetNearestBranches(long id, String name, String address, double distance) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.distanceInMeters = distance;
        }
    }
}
