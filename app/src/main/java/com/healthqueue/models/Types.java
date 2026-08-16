package com.healthqueue.models;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

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
        // public UUID orgId;
        public String firstName;
        public String lastName;
        public String specialty;
        public long branchId;
    }
}
